package ua.edu.nupp.kpp.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ViolationServlet extends JsonServlet {

    private final ViolationService service = new ViolationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            writeJson(resp, service.getAll());
        } catch (Exception e) {
            writeError(resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        try {
            String action = text(req, "action");

            if ("add".equals(action)) {
                service.add(
                        longParam(req, "observationId", "ID спостереження"),
                        intParam(req, "severityLevel", "Тяжкість"));

            } else if ("update".equals(action)) {
                service.update(
                        longParam(req, "id", "ID"),
                        longParam(req, "observationId", "ID спостереження"),
                        intParam(req, "severityLevel", "Тяжкість"));

            } else if ("transition".equals(action)) {
                service.changeState(longParam(req, "id", "ID"), parseState(text(req, "state")));

            } else if ("delete".equals(action)) {
                service.delete(longParam(req, "id", "ID"));

            } else {
                throw new IllegalArgumentException("Невідома операція");
            }

            writeJson(resp, service.getAll());
        } catch (Exception e) {
            writeError(resp, e);
        }
    }

    private ViolationState parseState(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Не задано стан порушення");
        }
        try {
            return ViolationState.valueOf(value.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Невідомий стан порушення");
        }
    }
}
