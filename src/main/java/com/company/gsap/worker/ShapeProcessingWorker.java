package com.company.gsap.worker;

import com.company.gsap.pipeline.ShapePipeline;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.params.SetParams;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Consumes shape jobs from Redis (same list the Node API pushes to) and runs {@link ShapePipeline}.
 * Updates MySQL status + append-only logs for ERP observability.
 */
public class ShapeProcessingWorker implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ShapeProcessingWorker.class);

    private static final int MAX_PROCESS_ATTEMPTS = 3;
    private static final String PROCESSING_SUFFIX = ":processing";

    private final WorkerConfig config;
    private final JdbcShapeStore store;
    private final ShapePipeline pipeline;
    private final JedisPool jedisPool;

    public ShapeProcessingWorker(WorkerConfig config, JdbcShapeStore store, ShapePipeline pipeline) {
        this.config = config;
        this.store = store;
        this.pipeline = pipeline;
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(8);
        String password = config.getRedisPassword();
        if (password == null || password.isBlank()) {
            this.jedisPool = new JedisPool(poolConfig, config.getRedisHost(), config.getRedisPort());
        } else {
            this.jedisPool = new JedisPool(
                    poolConfig, config.getRedisHost(), config.getRedisPort(), 2000, password);
        }
    }

    @Override
    public void run() {
        log.info("Shape worker started. Redis BRPOP on [{}], outputDir=[{}]",
                config.getJobListKey(), config.getOutputDir());
        while (!Thread.currentThread().isInterrupted()) {
            try (Jedis jedis = jedisPool.getResource()) {
                List<String> popped = jedis.brpop(0, config.getJobListKey());
                if (popped == null || popped.size() < 2) {
                    continue;
                }
                String payload = popped.get(1);
                handleOneJob(jedis, payload);
            } catch (Exception e) {
                log.error("Worker loop error: {}", e.getMessage(), e);
                sleepQuietly(2000);
            }
        }
        log.info("Shape worker stopped.");
    }

    private void handleOneJob(Jedis jedis, String payload) {
        long shapeId;
        try {
            JsonObject obj = JsonParser.parseString(payload).getAsJsonObject();
            shapeId = obj.get("shapeId").getAsLong();
        } catch (Exception e) {
            log.warn("Malformed job payload (skip): {}", payload);
            return;
        }

        String claimKey = config.getJobListKey() + PROCESSING_SUFFIX + ":" + shapeId;
        String claim = jedis.set(claimKey, "1", SetParams.setParams().nx().ex(3600));
        if (!"OK".equals(claim)) {
            log.debug("Skip duplicate in-flight job for shapeId={}", shapeId);
            return;
        }

        for (int attempt = 1; attempt <= MAX_PROCESS_ATTEMPTS; attempt++) {
            try (Connection conn = store.openConnection()) {
                conn.setAutoCommit(true);
                Optional<JdbcShapeStore.ShapeRecord> row = store.findShape(conn, shapeId);
                if (row.isEmpty()) {
                    log.warn("Shape {} not found in DB; dropping job.", shapeId);
                    jedis.del(claimKey);
                    return;
                }

                if ("completed".equalsIgnoreCase(row.get().previousStatus())) {
                    log.info("Shape {} already completed — skipping.", shapeId);
                    jedis.del(claimKey);
                    return;
                }

                store.updateStatus(conn, shapeId, "processing", null);
                store.appendLog(conn, shapeId, "info", "Processing started", jsonAttempt(attempt));

                List<Path> outputs = pipeline.processAndGenerateFromJsonString(
                        row.get().jsonData(), config.getOutputDir());

                store.updateStatus(conn, shapeId, "completed", null);
                JsonObject meta = new JsonObject();
                meta.addProperty("files", outputs.size());
                store.appendLog(conn, shapeId, "info", "Processing completed", meta);
                jedis.del(claimKey);
                return;
            } catch (Exception ex) {
                log.error("Processing failed for shape {} attempt {}/{}: {}",
                        shapeId, attempt, MAX_PROCESS_ATTEMPTS, ex.getMessage());
                try (Connection conn = store.openConnection()) {
                    store.updateStatus(conn, shapeId, "failed", truncate(ex.getMessage(), 2000));
                    JsonObject meta = new JsonObject();
                    meta.addProperty("attempt", attempt);
                    meta.addProperty("error", ex.getClass().getSimpleName());
                    store.appendLog(conn, shapeId, "error", safeMessage(ex), meta);
                } catch (Exception dbErr) {
                    log.error("Could not persist failure state: {}", dbErr.getMessage());
                }
                if (attempt < MAX_PROCESS_ATTEMPTS) {
                    sleepQuietly(1500L * attempt);
                }
            }
        }
        try (Jedis j = jedisPool.getResource()) {
            j.del(claimKey);
        }
    }

    private static JsonObject jsonAttempt(int attempt) {
        JsonObject o = new JsonObject();
        o.addProperty("attempt", attempt);
        return o;
    }

    private static String safeMessage(Throwable t) {
        String m = t.getMessage();
        return m != null ? m : t.getClass().getSimpleName();
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }

    private static void sleepQuietly(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void close() {
        jedisPool.close();
    }
}
