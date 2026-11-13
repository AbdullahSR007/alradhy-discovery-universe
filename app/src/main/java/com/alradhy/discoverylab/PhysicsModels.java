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
    private static final double[] A_MIN0={30,-80,-5,200,380,700,.1,1,.3,200,.2,250};
    private static final double[] A_MAX0={70,80,5,1500,750,1300,1.0,24,4,1200,3,800};
    private static final double[] B_MIN0={.5,0,.1,5, .1,500,.2,10,0,-120,1.6,1.5};
    private static final double[] B_MAX0={3,24,2,12,1,1500,4,500,.7,120,20,4};
    private static final int[] A_INIT0={50,72,70,25,35,50,50,50,30,50,40,40};
    private static final int[] B_INIT0={40,25,45,40,40,50,40,20,25,50,48,40};

    /** 12 original labs + the catalog labs (index 12 onward). */
    public static final int COUNT = 12 + LabCatalog.COUNT;
    private static double[] join(double[] a, double[] b) { double[] r = new double[a.length + b.length]; System.arraycopy(a, 0, r, 0, a.length); System.arraycopy(b, 0, r, a.length, b.length); return r; }
    private static int[] join(int[] a, int[] b) { int[] r = new int[a.length + b.length]; System.arraycopy(a, 0, r, 0, a.length); System.arraycopy(b, 0, r, a.length, b.length); return r; }
    public static final double[] A_MIN = join(A_MIN0, LabCatalog.nums(0));
    public static final double[] A_MAX = join(A_MAX0, LabCatalog.nums(1));
    public static final double[] B_MIN = join(B_MIN0, LabCatalog.nums(2));
    public static final double[] B_MAX = join(B_MAX0, LabCatalog.nums(3));
    public static final int[] A_INIT = join(A_INIT0, LabCatalog.ints(0));
    public static final int[] B_INIT = join(B_INIT0, LabCatalog.ints(1));
    public static final int[] A_DEC = join(new int[]{2,2,2,1,2,1,2,1,1,1,2,1}, LabCatalog.ints(2));
    public static final int[] B_DEC = join(new int[]{2,1,2,2,2,1,2,1,2,1,2,2}, LabCatalog.ints(3));

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
            default:
                if (i >= 12 && i - 12 < LabCatalog.COUNT) return LabCatalog.D[i - 12].m_(x, y);
                return 0;
        }
    }
}
