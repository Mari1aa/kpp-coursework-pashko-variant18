package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ViolationDAO {

    private static final String COLS =
            "observation_id, severity_level, state, f1, f2, f3, integral_index";

    public long add(Violation v) throws SQLException {
        String sql = "INSERT INTO violation (" + COLS + ") VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, v);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    public List<Violation> getAll() throws SQLException {
        String sql = "SELECT id, " + COLS + " FROM violation ORDER BY id";
        List<Violation> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public Violation getById(long id) throws SQLException {
        String sql = "SELECT id, " + COLS + " FROM violation WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public boolean update(Violation v) throws SQLException {
        String sql = "UPDATE violation SET observation_id = ?, severity_level = ?, state = ?, "
                + "f1 = ?, f2 = ?, f3 = ?, integral_index = ? WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, v);
            ps.setLong(8, v.getId());
            return ps.executeUpdate() > 0;
        }
    }

    /** Змінює лише стан. Допустимість переходу перевіряє Service. */
    public boolean updateState(long id, ViolationState state) throws SQLException {
        String sql = "UPDATE violation SET state = ? WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, state.name());
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(long id) throws SQLException {
        String sql = "DELETE FROM violation WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private void bind(PreparedStatement ps, Violation v) throws SQLException {
        ps.setLong(1, v.getObservationId());
        ps.setInt(2, v.getSeverityLevel());
        ps.setString(3, v.getState().name());
        ps.setDouble(4, v.getF1());
        ps.setDouble(5, v.getF2());
        ps.setDouble(6, v.getF3());
        ps.setDouble(7, v.getIntegralIndex());
    }

    private Violation map(ResultSet rs) throws SQLException {
        return new Violation(
                rs.getLong("id"),
                rs.getLong("observation_id"),
                rs.getInt("severity_level"),
                ViolationState.valueOf(rs.getString("state")),
                rs.getDouble("f1"),
                rs.getDouble("f2"),
                rs.getDouble("f3"),
                rs.getDouble("integral_index"));
    }
}
