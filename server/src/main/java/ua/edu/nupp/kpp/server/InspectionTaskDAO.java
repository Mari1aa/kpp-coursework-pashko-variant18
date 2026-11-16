package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class InspectionTaskDAO {

    private static final String COLS =
            "violation_id, inspector, description, planned_date, is_done";

    public long add(InspectionTask t) throws SQLException {
        String sql = "INSERT INTO inspection_task (" + COLS + ") VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, t);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    public List<InspectionTask> getAll() throws SQLException {
        String sql = "SELECT id, " + COLS + " FROM inspection_task ORDER BY id";
        List<InspectionTask> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public InspectionTask getById(long id) throws SQLException {
        String sql = "SELECT id, " + COLS + " FROM inspection_task WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public boolean update(InspectionTask t) throws SQLException {
        String sql = "UPDATE inspection_task SET violation_id = ?, inspector = ?, "
                + "description = ?, planned_date = ?, is_done = ? WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, t);
            ps.setLong(6, t.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(long id) throws SQLException {
        String sql = "DELETE FROM inspection_task WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private void bind(PreparedStatement ps, InspectionTask t) throws SQLException {
        ps.setLong(1, t.getViolationId());
        ps.setString(2, t.getInspector());
        ps.setString(3, t.getDescription());
        String date = t.getPlannedDate();
        ps.setString(4, (date == null || date.isBlank()) ? null : date);
        ps.setBoolean(5, t.isDone());
    }

    private InspectionTask map(ResultSet rs) throws SQLException {
        return new InspectionTask(
                rs.getLong("id"),
                rs.getLong("violation_id"),
                rs.getString("inspector"),
                rs.getString("description"),
                rs.getString("planned_date"),
                rs.getBoolean("is_done"));
    }
}
