package ua.edu.nupp.kpp.server;

public class Observation {
    private Long id;
    private long plotId;
    private String observedAt;
    private String observer;
    private String category;
    private double affectedAreaHa;
    private String description;

    public Observation() {
    }

    public Observation(Long id,
            long plotId,
            String observedAt,
            String observer,
            String category,
            double affectedAreaHa,
            String description) {
        this.id = id;
        this.plotId = plotId;
        this.observedAt = observedAt;
        this.observer = observer;
        this.category = category;
        this.affectedAreaHa = affectedAreaHa;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getPlotId() {
        return plotId;
    }

    public void setPlotId(long plotId) {
        this.plotId = plotId;
    }

    public String getObservedAt() {
        return observedAt;
    }

    public void setObservedAt(String observedAt) {
        this.observedAt = observedAt;
    }

    public String getObserver() {
        return observer;
    }

    public void setObserver(String observer) {
        this.observer = observer;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getAffectedAreaHa() {
        return affectedAreaHa;
    }

    public void setAffectedAreaHa(double affectedAreaHa) {
        this.affectedAreaHa = affectedAreaHa;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
