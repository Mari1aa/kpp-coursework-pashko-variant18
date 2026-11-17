package ua.edu.nupp.kpp.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class HttpForestClient {

    private static final String BASE =
            System.getProperty("api.base", "http://localhost:8080/forest-inspection-server");

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public List<ForestPlot> getPlots() {
        return Arrays.asList(read(get("/plots"), ForestPlot[].class));
    }

    public void addPlot(ForestPlot p) {
        post("/plots", "action=add&" + plotForm(p));
    }

    public void updatePlot(ForestPlot p) {
        post("/plots", "action=update&id=" + p.getId() + "&" + plotForm(p));
    }

    public void deletePlot(long id) {
        post("/plots", "action=delete&id=" + id);
    }

    public List<Observation> getObservations() {
        return Arrays.asList(read(get("/observations"), Observation[].class));
    }

    public void addObservation(Observation o) {
        post("/observations", "action=add&" + observationForm(o));
    }

    public void updateObservation(Observation o) {
        post("/observations", "action=update&id=" + o.getId() + "&" + observationForm(o));
    }

    public void deleteObservation(long id) {
        post("/observations", "action=delete&id=" + id);
    }

    public List<Violation> getViolations() {
        return Arrays.asList(read(get("/violations"), Violation[].class));
    }

    public void addViolation(long observationId, int severityLevel) {
        post("/violations", "action=add&observationId=" + observationId
                + "&severityLevel=" + severityLevel);
    }

    public void updateViolation(long id, long observationId, int severityLevel) {
        post("/violations", "action=update&id=" + id + "&observationId=" + observationId
                + "&severityLevel=" + severityLevel);
    }

    public void changeViolationState(long id, ViolationState state) {
        post("/violations", "action=transition&id=" + id + "&state=" + state.name());
    }

    public void deleteViolation(long id) {
        post("/violations", "action=delete&id=" + id);
    }

    public List<InspectionTask> getTasks() {
        return Arrays.asList(read(get("/tasks"), InspectionTask[].class));
    }

    public void addTask(InspectionTask t) {
        post("/tasks", "action=add&" + taskForm(t));
    }

    public void updateTask(InspectionTask t) {
        post("/tasks", "action=update&id=" + t.getId() + "&" + taskForm(t));
    }

    public void deleteTask(long id) {
        post("/tasks", "action=delete&id=" + id);
    }

    private String plotForm(ForestPlot p) {
        return "code=" + enc(p.getCode())
                + "&name=" + enc(p.getName())
                + "&forestType=" + enc(p.getForestType())
                + "&areaHa=" + p.getAreaHa()
                + "&sensitivityLevel=" + p.getSensitivityLevel();
    }

    private String observationForm(Observation o) {
        return "plotId=" + o.getPlotId()
                + "&observedAt=" + enc(o.getObservedAt())
                + "&observer=" + enc(o.getObserver())
                + "&category=" + enc(o.getCategory())
                + "&affectedAreaHa=" + o.getAffectedAreaHa()
                + "&description=" + enc(o.getDescription());
    }

    private String taskForm(InspectionTask t) {
        return "violationId=" + t.getViolationId()
                + "&inspector=" + enc(t.getInspector())
                + "&description=" + enc(t.getDescription())
                + "&plannedDate=" + enc(t.getPlannedDate())
                + "&done=" + t.isDone();
    }

    private String get(String path) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE + path))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        return send(request);
    }

    private String post(String path, String form) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE + path))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();
        return send(request);
    }

    private String send(HttpRequest request) {
        HttpResponse<String> response;
        try {
            response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Запит перервано", e);
        } catch (Exception e) {
            throw new RuntimeException("Не вдалося з'єднатися із сервером. Перевірте, що Tomcat запущено.", e);
        }
        if (response.statusCode() >= 400) {
            throw new RuntimeException(errorMessage(response.body()));
        }
        return response.body();
    }

    private String errorMessage(String body) {
        try {
            JsonNode node = mapper.readTree(body);
            if (node.hasNonNull("error")) {
                return node.get("error").asText();
            }
        } catch (Exception ignored) {
            // тіло відповіді не є JSON, показуємо його як є
        }
        return "Помилка сервера";
    }

    private <T> T read(String json, Class<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (Exception e) {
            throw new RuntimeException("Помилка розбору JSON-відповіді сервера", e);
        }
    }

    private String enc(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }
}
