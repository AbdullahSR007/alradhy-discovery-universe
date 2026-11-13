import com.alradhy.discoverylab.LabCatalog;
import com.alradhy.discoverylab.PhysicsModels;

/** Plain-javac checks of the app's real physics code (run in CI, no Android needed). */
public class PhysicsCheck {
    static int passed = 0;
    static void check(String name, double value, double expected, double rel) {
        if (!Double.isFinite(value) || Math.abs(value - expected) > rel * Math.max(1e-9, Math.abs(expected)))
            throw new AssertionError(name + ": " + value + " != " + expected);
        passed++; System.out.println("PASS " + name);
    }
    static void check(String name, double value, double expected) {
        if (!Double.isFinite(value) || Math.abs(value - expected) > 1e-6 * Math.max(1e-9, Math.abs(expected)) + 1e-12)
            throw new AssertionError(name + ": " + value + " != " + expected);
        passed++; System.out.println("PASS " + name);
    }
    static void yes(String name, boolean ok) {
        if (!ok) throw new AssertionError(name);
        passed++; System.out.println("PASS " + name);
    }
    public static void main(String[] a) {
        double G = PhysicsModels.G;
        check("Coulomb 1uC x 2uC at 1 m", PhysicsModels.metric(2, 1, 1, 2), 0.0179751035846);
        check("Coulomb inverse square", PhysicsModels.metric(2, 1, 2, 2), 0.0179751035846 / 4);
        check("Coulomb opposite charges attract", PhysicsModels.metric(2, 1, 1, -2), -0.0179751035846);
        check("Foucault equator", PhysicsModels.metric(1, 0, 0, 0), 0);
        check("Foucault pole rate", PhysicsModels.metric(1, 90, 0, 0), 15.041067);
        check("Young fringe spacing mm", PhysicsModels.metric(4, 500, 0.2, 0), 5);
        check("Archimedes 1 L of water", PhysicsModels.metric(5, 1000, 0, 0), G);
        check("Faraday emf", PhysicsModels.metric(6, 0.5, 2, 0), Math.PI);
        check("Ohm current", PhysicsModels.metric(7, 10, 5, 0), 2);
        check("Kepler 1 AU period", PhysicsModels.metric(8, 1, 0, 0), 1);
        yes("Doppler approach higher", PhysicsModels.metric(9, 500, 50, 0) > 500);
        yes("Doppler recede lower", PhysicsModels.metric(9, 500, -50, 0) < 500);
        check("Pendulum 1 m", PhysicsModels.metric(10, 1, G, 0), 2.006409292589);
        yes("Photoelectric emits at 400 nm (phi 2 eV)", PhysicsModels.metric(11, 400, 2, 0) > 0);
        check("Photoelectric none at 700 nm (phi 2 eV)", PhysicsModels.metric(11, 700, 2, 0), 0);
        double A = Math.toRadians(50);
        double dRed = 2 * Math.asin(PhysicsModels.bk7(650) * Math.sin(A / 2)) - A;
        double dBlue = 2 * Math.asin(PhysicsModels.bk7(450) * Math.sin(A / 2)) - A;
        yes("Prism: blue bends more than red", dBlue > dRed);
        double[] q2s = {-5, -2, -0.5, 0, 0.5, 2, 5};
        for (int i = 0; i < PhysicsModels.COUNT; i++) {
            for (int pa = 0; pa <= 100; pa += 5) for (int pb = 0; pb <= 100; pb += 5) for (double q2 : q2s) {
                double v = PhysicsModels.metric(i, PhysicsModels.value(PhysicsModels.A_MIN[i], PhysicsModels.A_MAX[i], pa),
                    PhysicsModels.value(PhysicsModels.B_MIN[i], PhysicsModels.B_MAX[i], pb), q2);
                if (!Double.isFinite(v)) throw new AssertionError("lab " + i + " not finite at " + pa + "," + pb);
            }
            yes("Lab " + i + " finite over the whole control domain", true);
        }
        check("Newton 2nd: 10 N on 2 kg", PhysicsModels.metric(12, 10, 2, 0), 5);
        check("Projectile 45 deg 20 m/s range", PhysicsModels.metric(13, 45, 20, 0), 400 / G);
        check("Energy drop 5 m", PhysicsModels.metric(14, 5, 3, 0), Math.sqrt(2 * G * 5));
        check("Elastic collision equal-ish m1=2", PhysicsModels.metric(16, 2, 10, 0), 10);
        check("Spring period 1 kg 100 N/m", PhysicsModels.metric(19, 1, 100, 0), 2 * Math.PI / 10);
        check("Total internal reflection returns 90", PhysicsModels.metric(20, 80, 1.5, 0), 90);
        check("Lens u=2f gives v=2f", PhysicsModels.metric(21, 20, 10, 0), 20);
        check("Wien peak is the maximum (rel=1)", LabCatalog.planckRel(5800, 2.897771955e-3 / 5800 * 1e9), 1);
        check("Earth surface gravity", PhysicsModels.metric(23, 1, 1, 0), 9.8199, 1e-3);
        check("Hubble 100 Mpc at H0 70", PhysicsModels.metric(24, 100, 70, 0), 7000);
        check("Kepler 3: a=1 P=1 is one solar mass", PhysicsModels.metric(25, 1, 1, 0), 1);
        check("Sun luminosity from R=1 T=5772", PhysicsModels.metric(26, 1, 5772, 0), 1);
        check("Parallax: Sun-like star at 10 pc has M=m", PhysicsModels.metric(27, 100, 4.83, 0), 4.83);
        check("Jupiter transit depth ~1%", PhysicsModels.metric(28, 11.2, 1, 0), 1.0537, 1e-3);
        check("Earth equilibrium temp ~255 K", PhysicsModels.metric(29, 1, 1, 0), 254.6, 1e-2);
        check("Halley perihelion speed ~54 km/s", PhysicsModels.metric(32, 17.8, 0.967, 0), 54.6, 2e-2);
        check("Balmer H-alpha 656 nm", PhysicsModels.metric(34, 3, 2, 0), 656.1, 1e-3);
        check("FSPL 2.4 GHz at 1 km", PhysicsModels.metric(36, 2.4, 1, 0), 100.05, 1e-3);
        check("Nyquist alias 3000 Hz sampled at 4000 Hz", PhysicsModels.metric(38, 3000, 4000, 0), 1000);
        check("Shannon 20 MHz at 30 dB", PhysicsModels.metric(39, 20, 30, 0), 199.3, 1e-2);
        check("AM efficiency m=1 is one third", PhysicsModels.metric(40, 1, 10, 0), 100.0 / 3);
        check("Carson bandwidth for FM broadcast", PhysicsModels.metric(41, 75, 15, 0), 180);
        check("BPSK BER at 9.6 dB near 1e-5 (-log10 ~ 5)", PhysicsModels.metric(42, 9.6, 1000, 0), 5.0, 2e-2);
        check("GEO one-way delay at zenith", PhysicsModels.metric(44, 35786, 90, 0), 119.37, 1e-3);
        check("Radio horizon 100 m + 1.5 m", PhysicsModels.metric(45, 100, 1.5, 0), 4.12 * (10 + Math.sqrt(1.5)));
        int[] expected = {1, 1, 1, -1, 1, 1, 1, 1, 1, 1, 1, -1};
        for (int i = 0; i < 12; i++) {
            int pb = PhysicsModels.B_INIT[i];
            double prev = PhysicsModels.metric(i, PhysicsModels.A_MIN[i], PhysicsModels.value(PhysicsModels.B_MIN[i], PhysicsModels.B_MAX[i], pb), 2);
            for (int pa = 1; pa <= 100; pa++) {
                double v = PhysicsModels.metric(i, PhysicsModels.value(PhysicsModels.A_MIN[i], PhysicsModels.A_MAX[i], pa),
                    PhysicsModels.value(PhysicsModels.B_MIN[i], PhysicsModels.B_MAX[i], pb), 2);
                if (expected[i] * (v - prev) < -1e-9 * Math.abs(prev) - 1e-12)
                    throw new AssertionError("lab " + i + " not monotone at " + pa);
                prev = v;
            }
            yes("Lab " + i + " monotone in first control", true);
        }
        System.out.println("All " + passed + " checks passed.");
    }
}
