package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ForestPlotDAO {

    private static final String COLS = "code, name, forest_type, area_ha, sensitivity_level";

    public long add(ForestPlot p) throws SQLException {
        String sql = "INSERT INTO forest_plot (" + COLS + ") VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, p);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    public List<ForestPlot> getAll() throws SQLException {
        String sql = "SELECT id, " + COLS + " FROM forest_plot ORDER BY id";
        List<ForestPlot> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public ForestPlot getById(long id) throws SQLException {
        String sql = "SELECT id, " + COLS + " FROM forest_plot WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public boolean update(ForestPlot p) throws SQLException {
        String sql = "UPDATE forest_plot SET code = ?, name = ?, forest_type = ?, "
                + "area_ha = ?, sensitivity_level = ? WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, p);
            ps.setLong(6, p.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(long id) throws SQLException {
        String sql = "DELETE FROM forest_plot WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private void bind(PreparedStatement ps, ForestPlot p) throws SQLException {
        ps.setString(1, p.getCode());
        ps.setString(2, p.getName());
        ps.setString(3, p.getForestType());
        ps.setDouble(4, p.getAreaHa());
        ps.setInt(5, p.getSensitivityLevel());
    }

    private ForestPlot map(ResultSet rs) throws SQLException {
        return new ForestPlot(
                rs.getLong("id"),
                rs.getString("code"),
                rs.getString("name"),
                rs.getString("forest_type"),
                rs.getDouble("area_ha"),
                rs.getInt("sensitivity_level"));
    }
}
