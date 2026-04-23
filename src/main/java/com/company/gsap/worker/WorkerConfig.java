package com.company.gsap.worker;

import java.util.Objects;

/**
 * Environment-driven settings for the Redis + JDBC worker.
 */
public final class WorkerConfig {

    private final String jdbcUrl;
    private final String jdbcUser;
    private final String jdbcPassword;
    private final String redisHost;
    private final int redisPort;
    private final String redisPassword;
    private final String jobListKey;
    private final String outputDir;

    private WorkerConfig(Builder b) {
        this.jdbcUrl = Objects.requireNonNull(b.jdbcUrl, "JDBC_URL or MYSQL_* env required");
        this.jdbcUser = Objects.requireNonNull(b.jdbcUser, "DB user required");
        this.jdbcPassword = b.jdbcPassword != null ? b.jdbcPassword : "";
        this.redisHost = Objects.requireNonNull(b.redisHost, "REDIS_HOST required");
        this.redisPort = b.redisPort;
        this.redisPassword = b.redisPassword;
        this.jobListKey = Objects.requireNonNull(b.jobListKey, "job list key required");
        this.outputDir = Objects.requireNonNull(b.outputDir, "OUTPUT_DIR required");
    }

    public static WorkerConfig fromEnv() {
        Builder b = new Builder();
        String url = System.getenv("JDBC_URL");
        if (url == null || url.isBlank()) {
            String host = firstNonBlank(System.getenv("MYSQL_HOST"), "localhost");
            String port = firstNonBlank(System.getenv("MYSQL_PORT"), "3306");
            String db = firstNonBlank(System.getenv("MYSQL_DATABASE"), "gsap_editor");
            url = "jdbc:mysql://" + host + ":" + port + "/" + db
                    + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        }
        b.jdbcUrl = url;
        b.jdbcUser = firstNonBlank(System.getenv("MYSQL_USER"), "root");
        b.jdbcPassword = System.getenv("MYSQL_PASSWORD");

        b.redisHost = firstNonBlank(System.getenv("REDIS_HOST"), "127.0.0.1");
        b.redisPort = parseIntEnv("REDIS_PORT", 6379);
        b.redisPassword = System.getenv("REDIS_PASSWORD");

        b.jobListKey = firstNonBlank(System.getenv("SHAPE_JOB_LIST_KEY"), "gsap:shape-processing:jobs");
        b.outputDir = firstNonBlank(System.getenv("OUTPUT_DIR"), "shapes/output");
        return new WorkerConfig(b);
    }

    private static String firstNonBlank(String v, String fallback) {
        if (v == null || v.isBlank()) {
            return fallback;
        }
        return v;
    }

    private static int parseIntEnv(String name, int def) {
        String v = System.getenv(name);
        if (v == null || v.isBlank()) {
            return def;
        }
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public String getJdbcUser() {
        return jdbcUser;
    }

    public String getJdbcPassword() {
        return jdbcPassword;
    }

    public String getRedisHost() {
        return redisHost;
    }

    public int getRedisPort() {
        return redisPort;
    }

    public String getRedisPassword() {
        return redisPassword;
    }

    public String getJobListKey() {
        return jobListKey;
    }

    public String getOutputDir() {
        return outputDir;
    }

    private static final class Builder {
        private String jdbcUrl;
        private String jdbcUser;
        private String jdbcPassword;
        private String redisHost;
        private int redisPort = 6379;
        private String redisPassword;
        private String jobListKey;
        private String outputDir;
    }
}
