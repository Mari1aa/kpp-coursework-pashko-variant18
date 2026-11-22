package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ObservationService {

    private static final Logger LOG = Logger.getLogger(ObservationService.class.getName());
    private final ObservationDAO dao = new ObservationDAO();
    private final ForestPlotDAO plotDAO = new ForestPlotDAO();

    public long add(Observation o) throws SQLException {
        validate(o);
        try {
            long id = dao.add(o);
            LOG.info("Додано спостереження id=" + id + ", ділянка=" + o.getPlotId());
            return id;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Помилка додавання спостереження", e);
            throw e;
        }
    }

    public List<Observation> getAll() throws SQLException {
        return dao.getAll();
    }

    public Observation getById(long id) throws SQLException {
        return dao.getById(id);
    }

    public void update(Observation o) throws SQLException {
        if (o.getId() == null || dao.getById(o.getId()) == null) {
            LOG.warning("Оновлення відхилено: спостереження не знайдено");
            throw new IllegalArgumentException("Спостереження не знайдено");
        }
        validate(o);
        try {
            dao.update(o);
            LOG.info("Оновлено спостереження id=" + o.getId());
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Помилка оновлення спостереження id=" + o.getId(), e);
            throw e;
        }
    }

    public void delete(long id) throws SQLException {
        try {
            if (!dao.delete(id)) {
                LOG.warning("Видалення відхилено: спостереження не знайдено, id=" + id);
                throw new IllegalArgumentException("Спостереження не знайдено");
            }
            LOG.info("Видалено спостереження id=" + id);
        } catch (SQLIntegrityConstraintViolationException e) {
            LOG.warning("Видалення відхилено: є пов'язані записи, id=" + id);
            throw new IllegalArgumentException("Неможливо видалити: є пов'язані записи");
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Помилка видалення спостереження id=" + id, e);
            throw e;
        }
    }

    private void validate(Observation o) throws SQLException {
        validateFields(o);
        try {
            if (plotDAO.getById(o.getPlotId()) == null) {
                throw new IllegalArgumentException("Лісову ділянку не знайдено");
            }
        } catch (IllegalArgumentException e) {
            LOG.warning("Валідація спостереження не пройдена: " + e.getMessage());
            throw e;
        }
    }

    /** Перевірка полів без звернення до бази даних (її можна тестувати окремо). */
    static void validateFields(Observation o) {
        try {
            ForestPlotService.require(o.getObserver(), "Спостерігач");
            ForestPlotService.require(o.getCategory(), "Категорія");
            ForestPlotService.require(o.getObservedAt(), "Дата спостереження");
            try {
                LocalDate.parse(o.getObservedAt());
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Дата має бути у форматі РРРР-ММ-ДД");
            }
            if (o.getAffectedAreaHa() < 0) {
                throw new IllegalArgumentException("Площа ураження не може бути від'ємною");
            }
            if (o.getDescription() != null && o.getDescription().length() > 500) {
                throw new IllegalArgumentException("Опис задовгий (максимум 500 символів)");
            }
        } catch (IllegalArgumentException e) {
            LOG.warning("Валідація спостереження не пройдена: " + e.getMessage());
            throw e;
        }
    }
}
