package ua.edu.nupp.kpp.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class InspectionTaskServlet extends JsonServlet {

    private final InspectionTaskService service = new InspectionTaskService();

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
                InspectionTask task = new InspectionTask(
                        "update".equals(action) ? longParam(req, "id", "ID") : null,
                        longParam(req, "violationId", "ID порушення"),
                        text(req, "inspector"),
                        text(req, "description"),
                        text(req, "plannedDate"),
                        Boolean.parseBoolean(text(req, "done")));
                if ("add".equals(action)) {
                    service.add(task);
                } else {
                    service.update(task);
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
