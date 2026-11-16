package ua.edu.nupp.kpp.server;

public class InspectionTask {
    private Long id;
    private long violationId;
    private String inspector;
    private String description;
    private String plannedDate;
    private boolean done;

    public InspectionTask() {
    }

    public InspectionTask(Long id,
            long violationId,
            String inspector,
            String description,
            String plannedDate,
            boolean done) {
        this.id = id;
        this.violationId = violationId;
        this.inspector = inspector;
        this.description = description;
        this.plannedDate = plannedDate;
        this.done = done;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getViolationId() {
        return violationId;
    }

    public void setViolationId(long violationId) {
        this.violationId = violationId;
    }

    public String getInspector() {
        return inspector;
    }

    public void setInspector(String inspector) {
        this.inspector = inspector;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPlannedDate() {
        return plannedDate;
    }

    public void setPlannedDate(String plannedDate) {
        this.plannedDate = plannedDate;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}
