package com.company.gsap.worker;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;

/**
 * Minimal JDBC access for worker status transitions + log append.
 */
public class JdbcShapeStore {

    private static final Gson GSON = new Gson();

    private final WorkerConfig config;

    public JdbcShapeStore(WorkerConfig config) {
        this.config = config;
    }

    public Connection openConnection() throws SQLException {
        return DriverManager.getConnection(
                config.getJdbcUrl(),
                config.getJdbcUser(),
                config.getJdbcPassword());
    }

    public Optional<ShapeRecord> findShape(Connection conn, long shapeId) throws SQLException {
        String sql = "SELECT id, json_data, status FROM shapes WHERE id = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, shapeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new ShapeRecord(
                        rs.getLong("id"),
                        rs.getString("json_data"),
                        rs.getString("status")));
            }
        }
    }

    public void updateStatus(Connection conn, long shapeId, String status, String message)
            throws SQLException {
        String sql = "UPDATE shapes SET status = ?, status_message = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            if (message != null) {
                ps.setString(2, message);
            } else {
                ps.setNull(2, Types.VARCHAR);
            }
            ps.setLong(3, shapeId);
            ps.executeUpdate();
        }
    }

    public void appendLog(Connection conn, long shapeId, String level, String message, JsonObject metadata)
            throws SQLException {
        String sql = "INSERT INTO shape_processing_logs (shape_id, level, message, metadata) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, shapeId);
            ps.setString(2, level);
            ps.setString(3, message);
            if (metadata == null) {
                ps.setNull(4, Types.VARCHAR);
            } else {
                ps.setString(4, GSON.toJson(metadata));
            }
            ps.executeUpdate();
        }
    }

    public record ShapeRecord(long id, String jsonData, String previousStatus) {
    }
}
