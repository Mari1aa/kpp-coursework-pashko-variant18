package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ForestPlotService {

    private static final Logger LOG = Logger.getLogger(ForestPlotService.class.getName());
    private final ForestPlotDAO dao = new ForestPlotDAO();

    public long add(ForestPlot p) throws SQLException {
        validate(p);
        try {
            long id = dao.add(p);
            LOG.info("Додано лісову ділянку id=" + id + ", код=" + p.getCode());
            return id;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Помилка додавання лісової ділянки", e);
            throw e;
        }
    }

    public List<ForestPlot> getAll() throws SQLException {
        return dao.getAll();
    }

    public ForestPlot getById(long id) throws SQLException {
        return dao.getById(id);
    }

    public void update(ForestPlot p) throws SQLException {
        if (p.getId() == null || dao.getById(p.getId()) == null) {
            LOG.warning("Оновлення відхилено: ділянку не знайдено");
            throw new IllegalArgumentException("Лісову ділянку не знайдено");
        }
        validate(p);
        try {
            dao.update(p);
            LOG.info("Оновлено лісову ділянку id=" + p.getId());
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Помилка оновлення лісової ділянки id=" + p.getId(), e);
            throw e;
        }
    }

    public void delete(long id) throws SQLException {
        try {
            if (!dao.delete(id)) {
                LOG.warning("Видалення відхилено: ділянку не знайдено, id=" + id);
                throw new IllegalArgumentException("Лісову ділянку не знайдено");
            }
            LOG.info("Видалено лісову ділянку id=" + id);
        } catch (SQLIntegrityConstraintViolationException e) {
            LOG.warning("Видалення відхилено: є пов'язані записи, id=" + id);
            throw new IllegalArgumentException("Неможливо видалити: є пов'язані записи");
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Помилка видалення лісової ділянки id=" + id, e);
            throw e;
        }
    }

    private void validate(ForestPlot p) {
        try {
            require(p.getCode(), "Код ділянки");
            require(p.getName(), "Назва ділянки");
            require(p.getForestType(), "Тип лісу");
            if (p.getAreaHa() <= 0) {
                throw new IllegalArgumentException("Площа ділянки має бути більшою за 0");
            }
            if (p.getSensitivityLevel() < 1 || p.getSensitivityLevel() > 5) {
                throw new IllegalArgumentException("Чутливість ділянки має бути від 1 до 5");
            }
        } catch (IllegalArgumentException e) {
            LOG.warning("Валідація ділянки не пройдена: " + e.getMessage());
            throw e;
        }
    }

    static void require(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " є обов'язковим");
        }
    }
}
