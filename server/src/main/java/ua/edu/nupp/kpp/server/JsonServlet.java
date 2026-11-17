package ua.edu.nupp.kpp.server;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class JsonServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(JsonServlet.class.getName());
    private final ObjectMapper mapper = new ObjectMapper();

    protected void writeJson(HttpServletResponse resp, Object data) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        mapper.writeValue(resp.getWriter(), data);
    }

    protected void writeError(HttpServletResponse resp, Exception e) throws IOException {
        boolean clientError = e instanceof IllegalArgumentException;
        if (!clientError) {
            LOG.log(Level.SEVERE, "Непередбачена помилка обробки запиту", e);
        }
        int status = clientError
                ? HttpServletResponse.SC_BAD_REQUEST
                : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        String message = clientError
                ? (e.getMessage() == null ? "Некоректний запит" : e.getMessage())
                : "Внутрішня помилка сервера";
        resp.setStatus(status);
        writeJson(resp, Map.of("error", message));
    }

    protected String text(HttpServletRequest req, String name) {
        return req.getParameter(name);
    }

    protected long longParam(HttpServletRequest req, String name, String label) {
        try {
            return Long.parseLong(req.getParameter(name).trim());
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Поле «" + label + "» має бути цілим числом");
        }
    }

    protected int intParam(HttpServletRequest req, String name, String label) {
        try {
            return Integer.parseInt(req.getParameter(name).trim());
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Поле «" + label + "» має бути цілим числом");
        }
    }

    protected double doubleParam(HttpServletRequest req, String name, String label) {
        try {
            return Double.parseDouble(req.getParameter(name).trim().replace(',', '.'));
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Поле «" + label + "» має бути числом");
        }
    }
}
