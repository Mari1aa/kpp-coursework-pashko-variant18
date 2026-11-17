package ua.edu.nupp.kpp.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ObservationServlet extends JsonServlet {

    private final ObservationService service = new ObservationService();

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

            if ("delete".equals(action)) {
                service.delete(longParam(req, "id", "ID"));

            } else if ("add".equals(action) || "update".equals(action)) {
                Observation observation = new Observation(
                        "update".equals(action) ? longParam(req, "id", "ID") : null,
                        longParam(req, "plotId", "ID ділянки"),
                        text(req, "observedAt"),
                        text(req, "observer"),
                        text(req, "category"),
                        doubleParam(req, "affectedAreaHa", "Площа ураження, га"),
                        text(req, "description"));
                if ("add".equals(action)) {
                    service.add(observation);
                } else {
                    service.update(observation);
                }

            } else {
                throw new IllegalArgumentException("Невідома операція");
            }

            writeJson(resp, service.getAll());
        } catch (Exception e) {
            writeError(resp, e);
        }
    }
}
