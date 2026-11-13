import com.alradhy.discoverylab.PhysicsModels;

/** Plain-javac checks of the app's real physics code (run in CI, no Android needed). */
public class PhysicsCheck {
    static int passed = 0;
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
        for (int i = 0; i < 12; i++) {
            for (int pa = 0; pa <= 100; pa += 5) for (int pb = 0; pb <= 100; pb += 5) for (double q2 : q2s) {
                double v = PhysicsModels.metric(i, PhysicsModels.value(PhysicsModels.A_MIN[i], PhysicsModels.A_MAX[i], pa),
                    PhysicsModels.value(PhysicsModels.B_MIN[i], PhysicsModels.B_MAX[i], pb), q2);
                if (!Double.isFinite(v)) throw new AssertionError("lab " + i + " not finite at " + pa + "," + pb);
            }
            yes("Lab " + i + " finite over the whole control domain", true);
        }
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
