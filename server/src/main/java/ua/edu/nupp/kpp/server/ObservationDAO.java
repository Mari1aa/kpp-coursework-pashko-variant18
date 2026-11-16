package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ObservationDAO {

    private static final String COLS =
            "plot_id, observed_at, observer, category, affected_area_ha, description";

    public long add(Observation o) throws SQLException {
        String sql = "INSERT INTO observation (" + COLS + ") VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, o);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    public List<Observation> getAll() throws SQLException {
        String sql = "SELECT id, " + COLS + " FROM observation ORDER BY id";
        List<Observation> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public Observation getById(long id) throws SQLException {
        String sql = "SELECT id, " + COLS + " FROM observation WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public boolean update(Observation o) throws SQLException {
        String sql = "UPDATE observation SET plot_id = ?, observed_at = ?, observer = ?, "
                + "category = ?, affected_area_ha = ?, description = ? WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, o);
            ps.setLong(7, o.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(long id) throws SQLException {
        String sql = "DELETE FROM observation WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private void bind(PreparedStatement ps, Observation o) throws SQLException {
        ps.setLong(1, o.getPlotId());
        ps.setString(2, o.getObservedAt());
        ps.setString(3, o.getObserver());
        ps.setString(4, o.getCategory());
        ps.setDouble(5, o.getAffectedAreaHa());
        ps.setString(6, o.getDescription());
    }

    private Observation map(ResultSet rs) throws SQLException {
        return new Observation(
                rs.getLong("id"),
                rs.getLong("plot_id"),
                rs.getString("observed_at"),
                rs.getString("observer"),
                rs.getString("category"),
                rs.getDouble("affected_area_ha"),
                rs.getString("description"));
    }
}
