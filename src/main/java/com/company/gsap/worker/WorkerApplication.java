package com.company.gsap.worker;

import com.company.gsap.pipeline.ShapePipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Production worker entrypoint: Redis queue consumer + MySQL state machine + {@link ShapePipeline}.
 */
public final class WorkerApplication {

    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    public static void main(String[] args) {
        WorkerConfig config = WorkerConfig.fromEnv();
        JdbcShapeStore store = new JdbcShapeStore(config);
        ShapePipeline pipeline = new ShapePipeline();
        ShapeProcessingWorker worker = new ShapeProcessingWorker(config, store, pipeline);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutdown hook: closing worker resources");
            worker.close();
        }));

        log.info("GSAP Geometry Worker — JDBC [{}] Redis [{}:{}]",
                maskJdbc(config.getJdbcUrl()),
                config.getRedisHost(),
                config.getRedisPort());

        worker.run();
    }

    private static String maskJdbc(String url) {
        if (url == null) {
            return "";
        }
        int at = url.indexOf('@');
        if (at > 0) {
            return "jdbc:mysql://***@" + url.substring(at + 1);
        }
        return url;
    }
}
