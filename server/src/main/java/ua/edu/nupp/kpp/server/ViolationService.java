package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ViolationService {

    private static final Logger LOG = Logger.getLogger(ViolationService.class.getName());
    private final ViolationDAO dao = new ViolationDAO();
    private final ObservationDAO observationDAO = new ObservationDAO();
    private final ForestPlotDAO plotDAO = new ForestPlotDAO();

    public long add(long observationId, int severityLevel) throws SQLException {
        Violation v = build(null, observationId, severityLevel, ViolationState.DETECTED);
        try {
            long id = dao.add(v);
            LOG.info("Створено порушення id=" + id + ", індекс=" + v.getIntegralIndex());
            return id;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Помилка створення порушення", e);
            throw e;
        }
    }

    public List<Violation> getAll() throws SQLException {
        return dao.getAll();
    }

    public Violation getById(long id) throws SQLException {
        return dao.getById(id);
    }

    public void update(long id, long observationId, int severityLevel) throws SQLException {
        Violation current = dao.getById(id);
        if (current == null) {
            LOG.warning("Оновлення відхилено: порушення не знайдено, id=" + id);
            throw new IllegalArgumentException("Порушення не знайдено");
        }
        Violation v = build(id, observationId, severityLevel, current.getState());
        try {
            dao.update(v);
            LOG.info("Оновлено порушення id=" + id + ", індекс=" + v.getIntegralIndex());
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Помилка оновлення порушення id=" + id, e);
            throw e;
        }
    }

    public void delete(long id) throws SQLException {
        try {
            if (!dao.delete(id)) {
                LOG.warning("Видалення відхилено: порушення не знайдено, id=" + id);
                throw new IllegalArgumentException("Порушення не знайдено");
            }
            LOG.info("Видалено порушення id=" + id);
        } catch (SQLIntegrityConstraintViolationException e) {
            LOG.warning("Видалення відхилено: є пов'язані записи, id=" + id);
            throw new IllegalArgumentException("Неможливо видалити: є пов'язані записи");
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Помилка видалення порушення id=" + id, e);
            throw e;
        }
    }

    public static boolean isAllowedTransition(ViolationState from, ViolationState to) {
        return (from == ViolationState.DETECTED && to == ViolationState.VERIFIED)
                || (from == ViolationState.VERIFIED && to == ViolationState.ACTION_PLANNED)
                || (from == ViolationState.ACTION_PLANNED && to == ViolationState.CLOSED);
    }

    public void changeState(long id, ViolationState next) throws SQLException {
        Violation current = dao.getById(id);
        if (current == null) {
            LOG.warning("Зміну стану відхилено: порушення не знайдено, id=" + id);
            throw new IllegalArgumentException("Порушення не знайдено");
        }
        if (next == null || !isAllowedTransition(current.getState(), next)) {
            LOG.warning("Недопустимий перехід стану id=" + id + ": "
                    + current.getState() + " -> " + next);
            throw new IllegalArgumentException("Недопустимий перехід стану");
        }
        try {
            dao.updateState(id, next);
            LOG.info("Стан порушення id=" + id + " змінено: " + current.getState() + " -> " + next);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Помилка зміни стану порушення id=" + id, e);
            throw e;
        }
    }

    private Violation build(Long id, long observationId, int severityLevel,
                            ViolationState state) throws SQLException {
        try {
            Observation obs = observationDAO.getById(observationId);
            if (obs == null) {
                throw new IllegalArgumentException("Спостереження не знайдено");
            }
            ForestPlot plot = plotDAO.getById(obs.getPlotId());
            if (plot == null) {
                throw new IllegalArgumentException("Лісову ділянку не знайдено");
            }
            double f1 = RiskCalculator.factor1(obs.getAffectedAreaHa());
            double f2 = RiskCalculator.factor2(severityLevel);
            double f3 = RiskCalculator.factor3(plot.getSensitivityLevel());
            double index = RiskCalculator.calculate(f1, f2, f3);
            return new Violation(id, observationId, severityLevel, state, f1, f2, f3, index);
        } catch (IllegalArgumentException e) {
            LOG.warning("Валідація порушення не пройдена: " + e.getMessage());
            throw e;
        }
    }
}
