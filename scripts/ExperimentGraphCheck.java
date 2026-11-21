import com.alradhy.discoverylab.ExpansionModels;
import com.alradhy.discoverylab.GraphSweep;
import com.alradhy.discoverylab.MuseumMeasurements;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Exercise the exact graph sweep and live measurement used by the Android UI. */
public final class ExperimentGraphCheck {
    private static void close(double actual, double expected) {
        if (!Double.isFinite(actual) || Math.abs(actual - expected) > Math.max(1e-9, Math.abs(expected) * 2e-5)) {
            throw new AssertionError(actual + " != " + expected);
        }
    }

    private static void known(String id, double expected, double... inputs) {
        close(MuseumMeasurements.measure(id, -1, inputs), expected);
    }

    private static double[][] controls(String fields) {
        return Arrays.stream(fields.split(";")).map(s -> Arrays.stream(s.split(","))
            .mapToDouble(Double::parseDouble).toArray()).toArray(double[][]::new);
    }

    private static void verifyModel(String id, int legacy, double[][] specs, double[] settings, int axis) {
        double[] spec = specs[axis];
        ExpansionModels.Model model = p -> MuseumMeasurements.measure(id, legacy, p);
        GraphSweep.Series series = GraphSweep.sample(spec[0], spec[1], spec[3], axis, settings,
            MuseumMeasurements.breakpoints(id, axis, settings), model);
        double[] sample = settings.clone();
        boolean found = false;
        for (int i = 0; i < series.x.length; i++) {
            if (i > 0 && series.x[i - 1] >= series.x[i]) throw new AssertionError(id + ": unordered x values");
            sample[axis] = series.x[i];
            double expected = model.measure(sample);
            if (Double.isNaN(expected)) {
                if (!(id.equals("light.lens") || id.equals("light.total-reflection")) || !Double.isNaN(series.y[i])) {
                    throw new AssertionError(id + ": unexpected undefined point");
                }
            } else {
                close(series.y[i], expected);
            }
            if (series.x[i] == settings[axis]) {
                if (Double.doubleToLongBits(series.y[i]) != Double.doubleToLongBits(series.markerY))
                    throw new AssertionError(id + ": marker differs from curve");
                found = true;
            }
        }
        if (!found) throw new AssertionError(id + ": missing live marker");
        close(series.x[0], spec[0]);
        close(series.x[series.x.length - 1], spec[1]);
        if (series.markerX != settings[axis]) throw new AssertionError(id + ": marker uses the wrong axis");
        if (spec[3] > 0) for (double value : series.x) {
            double step = (value - spec[0]) / spec[3];
            close(step, Math.rint(step));
        }
    }

    public static void main(String[] args) throws Exception {
        int labs = 0;
        int sweeps = 0;
        Set<String> expansions = new HashSet<>();
        for (String line : Files.readAllLines(Path.of(args[0]))) {
            String[] fields = line.split("\t");
            String id = fields[0];
            int legacy = Integer.parseInt(fields[1]);
            double[][] specs = controls(fields[2]);
            double[] defaults = Arrays.stream(specs).mapToDouble(spec -> spec[2]).toArray();
            double[] lower = Arrays.stream(specs).mapToDouble(spec -> spec[0]).toArray();
            double[] upper = Arrays.stream(specs).mapToDouble(spec -> spec[1]).toArray();
            for (int axis = 0; axis < specs.length; axis++) {
                verifyModel(id, legacy, specs, defaults, axis);
                verifyModel(id, legacy, specs, lower, axis);
                verifyModel(id, legacy, specs, upper, axis);
                sweeps += 3;
            }
            if (ExpansionModels.MODELS.containsKey(id)) expansions.add(id);
            labs++;
        }
        if (labs != 118 || !expansions.equals(ExpansionModels.MODELS.keySet()))
            throw new AssertionError("Incomplete lab coverage " + labs + " " + expansions.size());

        known("scale.power-ratio", 1e3, -6, -9);
        known("scale.light-ruler", 1, 0, 299792458);
        known("mechanics.circular", 5, 10, 20);
        known("mechanics.torricelli", 1, 25 / (2 * 9.80665), 2);
        known("mechanics.poiseuille", Math.PI / 8, 1, 1000, 1, 1);
        known("fields.cap-energy", 5, 10, 100);
        known("fields.solenoid", 2.010619, 800, 1, .5);
        known("fields.transformer", 6, 12, 250, 500);
        known("light.malus", 40, 45, 80);
        known("light.slit", 22.00133, 550, 50, 1);
        known("light.grating", 30, 500, 1000);
        known("astro.escape", 11.186, 1, 1);
        known("astro.solar-flux", 340.25, 2, 1);
        known("astro.angular-diameter", .517805, 3474, 384.4);
        known("matter.diffusion", 2, 20, .1);
        known("matter.cooling", 42, 20, 20, 22 + 20 * Math.E, 22);
        known("matter.dilution", 12.5, 2, 25);
        known("matter.absorbance", 10, 1, 2, .5);
        known("telecom.sensitivity", -90.9647, 20, 5, 5);
        known("telecom.sinr", 16.9897, -80, -100, -100);
        known("telecom.rsrq", -13.0103, -100, -70, 50);
        known("telecom.nr-grid", 290.8829952, 106, 1, 2, 6, 83, 18);
        known("telecom.cell-radius", 10, 140, 110, 0, 3);
        known("telecom.rain", 5, 25, 2, .1, 1);
        known("telecom.optical-budget", 16, 20, .25, 0, 1, 2, -24);
        known("telecom.dispersion", 68, 40, 17, .1);
        known("telecom.vswr", 13.9794, 1.5);
        known("telecom.packet-delay", 5.62, 1500, 100, 100, 5);
        known("telecom.availability", 52.56, 99.99);
        known("telecom.eirp", 54, 40, 3, 17);
        known("telecom.power-sum", -76.9897, -80, -80);
        known("telecom.tilt", 30, 45, 30);
        known("matter.cell-size", .3, 10);
        known("matter.osmosis", 99.1583, 40, 25);
        known("matter.enzyme-curve", 50, 5, 5);
        known("matter.respiration", 6, 2, 50);
        known("matter.selection", 100.0 / 3, 25, 400.0 / 9);
        known("matter.hardy", 50, 50);
        known("matter.drift", 37.5, 1, 2, 50);
        known("matter.founder", 56.25, 1, 25);
        known("matter.clock", 5, 20, 2);
        known("matter.inheritance", 25, 50, 50);
        known("matter.cardiac-output", 5.04, 72, 70);
        known("matter.ventilation", 4.2, 500, 150, 12);
        known("matter.oxygen", 19.9925, 15, 98, 95);
        known("matter.nerve-delay", 14, .7, 50);
        known("matter.accommodation", 2.5, 40);
        known("matter.turnover", 25, 12, 6);
        known("telecom.cp-guard", 2.2, 4.7, 2, .5);
        known("telecom.rf-doppler", 0, 120, 3.5, 90);
        known("telecom.rf-doppler", 389.158, 120, 3.5, 0);
        known("telecom.coherence", 2.115, 200);
        known("telecom.tdd-capacity", 336, 600, 70, 80);
        known("telecom.bler-goodput", 360, 400, 10);
        known("telecom.mimo-modes", 40, 20, 0, 0);
        known("telecom.noise-rise", 10, 9, -100, -100);
        known("telecom.pon-split", 16.0515, 5, 1);
        known("telecom.otdr", 10.2109148, 100, 1.468);
        known("telecom.queue", 3, 75, 1000);
        known("telecom.packet-efficiency", 95, 950, 50);
        known("telecom.time-asymmetry", 10, 120, 100);
        known("telecom.dual-route", .5256, 99.9, 99.9);
        known("telecom.a3-margin", 3, -95, -103, 3, 2);
        close(MuseumMeasurements.measure("mechanics.newton2", 12, new double[] {20,4}), 5);
        close(MuseumMeasurements.measure("mechanics.newton2", 12, new double[] {20,6}), 10d/3);
        close(MuseumMeasurements.measure("light.lens", 21, new double[] {21,20}), 420);
        if (!Double.isNaN(MuseumMeasurements.measure("light.lens", 21, new double[] {20,20})))
            throw new AssertionError("Lens at focus requires a gap");
        if (!Double.isNaN(MuseumMeasurements.measure("light.total-reflection", 20, new double[] {60,1.5})))
            throw new AssertionError("Total internal reflection requires a gap");
        System.out.println("PASS " + labs + " lab graphs, " + sweeps +
            " axis/configuration sweeps and 68 independent measurement checks.");
    }
}
