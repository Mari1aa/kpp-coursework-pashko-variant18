package ua.edu.nupp.kpp.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ForestPlotServlet extends JsonServlet {

    private final ForestPlotService service = new ForestPlotService();

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
                ForestPlot plot = new ForestPlot(
                        "update".equals(action) ? longParam(req, "id", "ID") : null,
                        text(req, "code"),
                        text(req, "name"),
                        text(req, "forestType"),
                        doubleParam(req, "areaHa", "Площа, га"),
                        intParam(req, "sensitivityLevel", "Чутливість"));
                if ("add".equals(action)) {
                    service.add(plot);
                } else {
                    service.update(plot);
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
