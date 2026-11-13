package com.alradhy.discoverylab;

/**
 * Pure-Java physics relations used by the twelve museum labs (no Android
 * dependencies, so CI can compile and test them with plain javac).
 * Index i matches the lab order in ScienceLabsActivity.
 */
public final class PhysicsModels {
    private PhysicsModels() {}

    public static final double G = 9.80665;

    /** Control ranges (slider 0..100 maps linearly) and start positions per lab. */
    public static final double[] A_MIN={30,-80,-5,200,380,700,.1,1,.3,200,.2,250};
    public static final double[] A_MAX={70,80,5,1500,750,1300,1.0,24,4,1200,3,800};
    public static final double[] B_MIN={.5,0,.1,5, .1,500,.2,10,0,-120,1.6,1.5};
    public static final double[] B_MAX={3,24,2,12,1,1500,4,500,.7,120,20,4};
    public static final int[] A_INIT={50,72,70,25,35,50,50,50,30,50,40,40};
    public static final int[] B_INIT={40,25,45,40,40,50,40,20,25,50,48,40};

    public static double rad(double degrees) { return Math.toRadians(degrees); }

    /** BK7 glass refractive index (Sellmeier), wavelength in nm. */
    public static double bk7(double lambdaNm) {
        double l = lambdaNm / 1000.0, l2 = l * l;
        return Math.sqrt(1 + 1.03961212 * l2 / (l2 - .00600069867)
            + .231792344 * l2 / (l2 - .0200179144)
            + 1.01046945 * l2 / (l2 - 103.560653));
    }

    /** Linear map of a 0..100 slider position onto [min,max]. */
    public static double value(double min, double max, int progress) {
        return min + (max - min) * progress / 100.0;
    }

    /** Headline observable of lab i for control values x (first) and y (second). */
    public static double metric(int i, double x, double y, double q2) {
        switch (i) {
            case 0: {
                double A = rad(x), red = 2 * Math.asin(bk7(650) * Math.sin(A / 2)) - A,
                    blue = 2 * Math.asin(bk7(450) * Math.sin(A / 2)) - A;
                return y * (Math.tan(blue) - Math.tan(red)) * 100;
            }
            case 1: return 15.041067 * Math.sin(rad(x));
            case 2: return 8.9875517923e9 * (x * 1e-6) * (q2 * 1e-6) / (y * y);
            case 3: return Math.sqrt(3.986004418e14 / ((6371 + x) * 1000.0)) / 1000.0;
            case 4: return x * 1e-9 * 2.0 / (y * 1e-3) * 1000.0;
            case 5: return x * G * .001;
            case 6: return 50 * .01 * 2 * Math.PI * y * x;
            case 7: return x / y;
            case 8: return Math.sqrt(x * x * x);
            case 9: return x * 343 / (343 - y);
            case 10: return 2 * Math.PI * Math.sqrt(x / y);
            case 11: return Math.max(0, 1239.841984 / x - y);
            default: return 0;
        }
    }
}
