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
        models.put("matter.cell-size", p -> 3 / p[0]);
        models.put("matter.osmosis", p -> 8.314462618 * (p[1] + 273.15) * p[0] / 1000);
        models.put("matter.enzyme-curve", p -> 100 * p[0] / (p[0] + p[1]));
        models.put("matter.respiration", p -> 6 * p[0] * p[1] / 100);
        models.put("matter.selection", p -> p[0] / (1 - p[1] / 100 * pow(1 - p[0] / 100, 2)));
        models.put("matter.hardy", p -> 2 * p[0] * (1 - p[0] / 100));
        models.put("matter.drift", p -> 2 * p[2] * (1 - p[2] / 100) * pow(1 - 1 / (2 * p[1]), p[0]));
        models.put("matter.founder", p -> 100 * pow(1 - p[1] / 100, 2 * p[0]));
        models.put("matter.clock", p -> p[0] / (2 * p[1]));
        models.put("matter.inheritance", p -> p[0] * p[1] / 100);
        models.put("matter.cardiac-output", p -> p[0] * p[1] / 1000);
        models.put("matter.ventilation", p -> (p[0] - p[1]) * p[2] / 1000);
        models.put("matter.oxygen", p -> 1.34 * p[0] * p[1] / 100 + .0031 * p[2]);
        models.put("matter.nerve-delay", p -> 1000 * p[0] / p[1]);
        models.put("matter.accommodation", p -> 100 / p[0]);
        models.put("matter.turnover", p -> 100 * pow(2, -p[0] / p[1]));

        models.put("telecom.cp-guard", p -> p[0] - p[1] - p[2]);
        models.put("telecom.rf-doppler", p -> p[0] / 3.6 * p[1] * 1e9 / C * cos(toRadians(p[2])));
        models.put("telecom.coherence", p -> 423 / p[0]);
        models.put("telecom.tdd-capacity", p -> p[0] * p[1] * p[2] / 10000);
        models.put("telecom.bler-goodput", p -> p[0] * (1 - p[1] / 100));
        models.put("telecom.mimo-modes", p -> p[0] * (spectralCapacity(p[1]) + spectralCapacity(p[2])));
        models.put("telecom.noise-rise", p -> 10 * log10(1 + p[0] * pow(10, (p[1] - p[2]) / 10)));
        models.put("telecom.pon-split", p -> 10 * p[0] * log10(2) + p[1]);
        models.put("telecom.otdr", p -> C * p[0] * 1e-9 / (2 * p[1]));
        models.put("telecom.queue", p -> 1000 * p[0] / (p[1] * (100 - p[0])));
        models.put("telecom.packet-efficiency", p -> 100 * p[0] / (p[0] + p[1]));
        models.put("telecom.time-asymmetry", p -> (p[0] - p[1]) / 2);
        models.put("telecom.dual-route", p -> 525600 * (1 - p[0] / 100) * (1 - p[1] / 100));
        models.put("telecom.a3-margin", p -> p[0] - p[1] - p[2] - p[3]);
        return Collections.unmodifiableMap(models);
    }

    private static double spectralCapacity(double snrDb) {
        return log1p(pow(10, snrDb / 10)) / log(2);
    }

    private static double dbmSum(double first, double second) {
        double high = max(first, second);
        return high + 10 * log10(1 + pow(10, (min(first, second) - high) / 10));
    }
}
