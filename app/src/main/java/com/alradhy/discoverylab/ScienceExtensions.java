package com.alradhy.discoverylab;

/** Additional observatory models. Inputs use the units displayed in the workbench. */
public final class ScienceExtensions {
    private ScienceExtensions() {}

    public static final double LIGHT_SPEED = 299_792_458.0;
    public static final double AU = 149_597_870_700.0;
    private static final double EARTH_RADIUS = 6_371_000.0;

    public static double linkLoss(double[] p) {
        return LabCatalog.fspl(1000 * p[1], p[0]);
    }

    /** Each step is an absolute power level in dBm. */
    public static double[] linkStages(double[] p) {
        double afterTxCable = p[2] - p[3];
        double radiated = afterTxCable + p[4];
        double afterSpace = radiated - linkLoss(p);
        double afterRain = afterSpace - p[7];
        double afterRxGain = afterRain + p[5];
        return new double[] {p[2], afterTxCable, radiated, afterSpace, afterRain, afterRxGain, afterRxGain - p[6]};
    }

    public static double earthBulge(double pathKm, double k, double fraction) {
        double path = 1000 * pathKm;
        return path * path * fraction * (1 - fraction) / (2 * k * EARTH_RADIUS);
    }

    public static double fresnelRadius(double pathKm, double frequencyGhz, double fraction) {
        double wavelength = LIGHT_SPEED / (frequencyGhz * 1e9);
        return Math.sqrt(wavelength * pathKm * 1000 * fraction * (1 - fraction));
    }

    public static double clearanceMetres(double[] p) {
        double fraction = p[3] / 100;
        double lineOfSight = p[4] + (p[5] - p[4]) * fraction;
        return lineOfSight - p[2] - earthBulge(p[0], p[6], fraction);
    }

    public static double clearancePercent(double[] p) {
        return 100 * clearanceMetres(p) / fresnelRadius(p[0], p[1], p[3] / 100);
    }

    public static int qamBits(double orderIndex) {
        return 2 + 2 * Math.max(0, Math.min(3, (int) Math.round(orderIndex)));
    }

    public static double symbolError(double orderIndex, double snrDb) {
        double side = Math.sqrt(1 << qamBits(orderIndex));
        double snr = Math.pow(10, snrDb / 10);
        double q = .5 * LabCatalog.erfc(Math.sqrt(3 * snr / (2 * (side * side - 1))));
        double axisError = 2 * (1 - 1 / side) * q;
        return axisError * (2 - axisError);
    }

    public static double evmPercent(double snrDb) {
        return 100 * Math.pow(10, -snrDb / 20);
    }

    public static double satelliteRtt(double altitudeKm, double overheadMs) {
        return 4e6 * altitudeKm / LIGHT_SPEED + overheadMs;
    }

    public static double galileoMicroseconds(double distanceMetres) {
        return 2e6 * distanceMetres / LIGHT_SPEED;
    }

    public static double roemerDelay(double position) {
        return 2 * AU * position / (8 * LIGHT_SPEED);
    }

    public static double fizeauBrightness(double rpm, double distance) {
        double cycles = rpm / 60 * (2 * distance / LIGHT_SPEED) * 720;
        double periodFraction = cycles - Math.floor(cycles);
        return 100 * Math.max(0, 1 - 2 * Math.min(periodFraction, 1 - periodFraction));
    }

    public static double fizeauExtinction(double distance) {
        return 60 * LIGHT_SPEED / (4 * 720 * distance);
    }
}
