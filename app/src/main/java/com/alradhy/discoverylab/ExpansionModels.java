package com.alradhy.discoverylab;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.lang.Math.*;

/** SI conversions are applied at the boundary to the units shown by the lab controls. */
public final class ExpansionModels {
    private ExpansionModels() {}
    private static final double C = ScienceExtensions.LIGHT_SPEED;
    private static final double G = PhysicsModels.G;

    @FunctionalInterface
    public interface Model {
        double measure(double[] values);
    }

    public static final Map<String, Model> MODELS = models();

    private static Map<String, Model> models() {
        Map<String, Model> models = new LinkedHashMap<>();
        models.put("scale.power-ratio", p -> pow(10, p[0] - p[1]));
        models.put("scale.light-ruler", p -> pow(10, p[0]) / (C / p[1]));
        models.put("mechanics.circular", p -> p[0] * p[0] / p[1]);
        models.put("mechanics.torricelli", p -> sqrt(2 * G * p[0]) * p[1] * .1);
        models.put("mechanics.poiseuille", p -> PI * pow(p[0] / 1000, 4) * p[1] / (8 * p[2] / 1000 * p[3]) * 1e6);
        models.put("fields.cap-energy", p -> .5 * p[0] * p[0] * p[1] / 1000);
        models.put("fields.solenoid", p -> 4 * PI * 1e-4 * p[0] * p[1] / p[2]);
        models.put("fields.transformer", p -> p[0] * p[1] / p[2]);
        models.put("light.malus", p -> p[1] * pow(cos(toRadians(p[0])), 2));
        models.put("light.slit", p -> 2 * p[2] * tan(asin(p[0] / (p[1] * 1000))) * 1000);
        models.put("light.grating", p -> toDegrees(asin(p[0] * p[1] / 1e6)));
        models.put("astro.escape", p -> sqrt(2 * 3.986004418e14 * p[1] / (6.371e6 * p[0])) / 1000);
        models.put("astro.solar-flux", p -> 1361 * p[1] / (p[0] * p[0]));
        models.put("astro.angular-diameter", p -> toDegrees(2 * atan(p[0] / (2 * p[1] * 1000))));
        models.put("matter.diffusion", p -> sqrt(2 * p[0] * p[1]));
        models.put("matter.cooling", p -> p[3] + (p[2] - p[3]) * exp(-p[0] / p[1]));
        models.put("matter.dilution", p -> p[1] / p[0]);
        models.put("matter.absorbance", p -> 100 * pow(10, -p[0] * p[1] * p[2]));
        models.put("telecom.sensitivity", p -> -173.975 + 10 * log10(p[0] * 1e6) + p[1] + p[2]);
        models.put("telecom.sinr", p -> p[0] - dbmSum(p[1], p[2]));
        models.put("telecom.rsrq", p -> p[0] - p[1] + 10 * log10(p[2]));
        models.put("telecom.nr-grid", p -> p[0] * 12 * 14 * pow(2, p[1]) * p[2] * p[3] * p[4] / 100 * (1 - p[5] / 100) / 1000);
        models.put("telecom.cell-radius", p -> pow(10, (p[0] - p[1] - p[2]) / (10 * p[3])));
        models.put("telecom.rain", p -> p[1] * p[2] * pow(p[0], p[3]));
        models.put("telecom.optical-budget", p -> p[2] - p[0] * p[1] - p[3] - p[4] - p[5]);
        models.put("telecom.dispersion", p -> p[0] * p[1] * p[2]);
        models.put("telecom.vswr", p -> -20 * log10((p[0] - 1) / (p[0] + 1)));
        models.put("telecom.packet-delay", p -> p[0] * .008 / p[1] + p[2] / 200 + p[3]);
        models.put("telecom.availability", p -> (1 - p[0] / 100) * 365 * 24 * 60);
        models.put("telecom.eirp", p -> p[0] - p[1] + p[2]);
        models.put("telecom.power-sum", p -> dbmSum(p[0], p[1]));
        models.put("telecom.tilt", p -> p[1] / tan(toRadians(p[0])));
        return Collections.unmodifiableMap(models);
    }

    private static double dbmSum(double first, double second) {
        double high = max(first, second);
        return high + 10 * log10(1 + pow(10, (min(first, second) - high) / 10));
    }
}
