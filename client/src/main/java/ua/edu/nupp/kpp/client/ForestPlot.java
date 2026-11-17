package ua.edu.nupp.kpp.client;

public class ForestPlot {
    private Long id;
    private String code;
    private String name;
    private String forestType;
    private double areaHa;
    private int sensitivityLevel;

    public ForestPlot() {
    }

    public ForestPlot(Long id,
            String code,
            String name,
            String forestType,
            double areaHa,
            int sensitivityLevel) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.forestType = forestType;
        this.areaHa = areaHa;
        this.sensitivityLevel = sensitivityLevel;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getForestType() {
        return forestType;
    }

    public void setForestType(String forestType) {
        this.forestType = forestType;
    }

    public double getAreaHa() {
        return areaHa;
    }

    public void setAreaHa(double areaHa) {
        this.areaHa = areaHa;
    }

    public int getSensitivityLevel() {
        return sensitivityLevel;
    }

    public void setSensitivityLevel(int sensitivityLevel) {
        this.sensitivityLevel = sensitivityLevel;
    }
}
