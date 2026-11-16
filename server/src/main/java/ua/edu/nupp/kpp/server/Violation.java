package ua.edu.nupp.kpp.server;

public class Violation {
    private Long id;
    private long observationId;
    private int severityLevel;
    private ViolationState state;
    private double f1;
    private double f2;
    private double f3;
    private double integralIndex;

    public Violation() {
    }

    public Violation(Long id,
            long observationId,
            int severityLevel,
            ViolationState state,
            double f1,
            double f2,
            double f3,
            double integralIndex) {
        this.id = id;
        this.observationId = observationId;
        this.severityLevel = severityLevel;
        this.state = state;
        this.f1 = f1;
        this.f2 = f2;
        this.f3 = f3;
        this.integralIndex = integralIndex;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getObservationId() {
        return observationId;
    }

    public void setObservationId(long observationId) {
        this.observationId = observationId;
    }

    public int getSeverityLevel() {
        return severityLevel;
    }

    public void setSeverityLevel(int severityLevel) {
        this.severityLevel = severityLevel;
    }

    public ViolationState getState() {
        return state;
    }

    public void setState(ViolationState state) {
        this.state = state;
    }

    public double getF1() {
        return f1;
    }

    public void setF1(double f1) {
        this.f1 = f1;
    }

    public double getF2() {
        return f2;
    }

    public void setF2(double f2) {
        this.f2 = f2;
    }

    public double getF3() {
        return f3;
    }

    public void setF3(double f3) {
        this.f3 = f3;
    }

    public double getIntegralIndex() {
        return integralIndex;
    }

    public void setIntegralIndex(double integralIndex) {
        this.integralIndex = integralIndex;
    }
}
