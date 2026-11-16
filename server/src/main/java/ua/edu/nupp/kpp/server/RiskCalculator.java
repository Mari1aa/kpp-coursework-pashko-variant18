package ua.edu.nupp.kpp.server;

public final class RiskCalculator {

    private RiskCalculator() { }

    public static double factor1(double affectedAreaHa) {
        if (affectedAreaHa < 0) {
            throw new IllegalArgumentException("Площа ураження не може бути від'ємною");
        }
        return Math.min(100.0, affectedAreaHa * 10.0);
    }

    public static double factor2(int severityLevel) {
        if (severityLevel < 1 || severityLevel > 5) {
            throw new IllegalArgumentException("Тяжкість порушення має бути від 1 до 5");
        }
        return severityLevel * 20.0;
    }

    public static double factor3(int sensitivityLevel) {
        if (sensitivityLevel < 1 || sensitivityLevel > 5) {
            throw new IllegalArgumentException("Чутливість ділянки має бути від 1 до 5");
        }
        return sensitivityLevel * 20.0;
    }

    public static double calculate(double f1, double f2, double f3) {
        checkFactor(f1, "F1");
        checkFactor(f2, "F2");
        checkFactor(f3, "F3");
        double result = 0.40 * f1 + 0.35 * f2 + 0.25 * f3;
        return Math.round(result * 100.0) / 100.0;
    }

    private static void checkFactor(double value, String name) {
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException("Фактор " + name + " має бути в межах 0..100");
        }
    }
}
