package com.alradhy.discoverylab;

import static java.lang.Math.*;

/** Shared headline and sweep readings for all museum experiments. */
public final class MuseumMeasurements {
    private MuseumMeasurements() {}

    public static double measure(String id, int legacy, double[] p) {
        ExpansionModels.Model expansion = ExpansionModels.MODELS.get(id);
        if (expansion != null) return expansion.measure(p);
        switch (id) {
            case "light.lens": {
                double separation = p[0] - p[1];
                return abs(separation) < .3 ? Double.NaN : p[0] * p[1] / separation;
            }
            case "light.total-reflection": {
                double sine = p[1] * sin(toRadians(p[0]));
                return sine > 1 + 1e-12 ? Double.NaN : toDegrees(asin(min(sine, 1)));
            }
            case "telecom.link-budget": return ScienceExtensions.linkStages(p)[6] - p[8];
            case "telecom.fresnel": return ScienceExtensions.clearancePercent(p);
            case "telecom.qam":
                return p[2] * ScienceExtensions.qamBits(p[0]) * (1 - ScienceExtensions.symbolError(p[0], p[1]));
            case "telecom.orbit-latency": return ScienceExtensions.satelliteRtt(p[0], p[1]);
            case "light.galileo": return ScienceExtensions.galileoMicroseconds(p[0]);
            case "light.roemer": return ScienceExtensions.roemerDelay(p[0]);
            case "light.fizeau": return ScienceExtensions.fizeauBrightness(p[0], p[1]);
            case "fields.rc": return 5 * -expm1(-p[0] / p[1]);
            case "fields.wire": return 20 * p[1] / p[0];
            default:
                if (legacy >= 0 && legacy < PhysicsModels.COUNT) {
                    return PhysicsModels.metric(legacy, p[0], p[1], p.length > 2 ? p[2] : 2);
                }
                throw new IllegalArgumentException("Missing measurement model for " + id);
        }
    }

    public static double[] breakpoints(String id, int axis, double[] configuration) {
        if (id.equals("light.lens")) {
            double focus = configuration[1 - axis];
            return new double[] {focus - .3, focus, focus + .3};
        }
        if (id.equals("light.total-reflection")) {
            double critical = axis == 0 ? toDegrees(asin(1 / configuration[1]))
                : 1 / sin(toRadians(configuration[0]));
            return new double[] {critical};
        }
        return new double[0];
    }
}
