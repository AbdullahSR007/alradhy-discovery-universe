package com.alradhy.discoverylab;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import java.util.Locale;
import java.util.Random;

/** Animated drawings for the 35 catalog labs. Canvas space is 1000 x 440. */
final class NewLabArt {
    private NewLabArt() {}
    private static final Paint P = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final int WHITE = Color.rgb(235, 245, 255), GOLD = Color.rgb(246, 197, 99), MUTED = Color.rgb(158, 186, 212);
    private static final int CYAN = Color.rgb(81, 219, 237), ORANGE = Color.rgb(255, 143, 92), GREEN = Color.rgb(113, 221, 163);
    private static final int BG = Color.rgb(7, 15, 29), CARD = Color.rgb(17, 34, 57), RED = Color.rgb(255, 99, 99);

    static boolean animated(int k) { return true; }

    private static String n(double v, int d) { return String.format(Locale.US, "%." + d + "f", v); }
    private static void ln(Canvas c, double x1, double y1, double x2, double y2, int col, float w) {
        P.setColor(col); P.setStrokeWidth(w); P.setStyle(Paint.Style.STROKE); c.drawLine((float) x1, (float) y1, (float) x2, (float) y2, P); P.setStyle(Paint.Style.FILL);
    }
    private static void fc(Canvas c, double x, double y, double r, int col) { P.setColor(col); P.setStyle(Paint.Style.FILL); c.drawCircle((float) x, (float) y, (float) r, P); }
    private static void sc(Canvas c, double x, double y, double r, int col, float w) { P.setColor(col); P.setStrokeWidth(w); P.setStyle(Paint.Style.STROKE); c.drawCircle((float) x, (float) y, (float) r, P); P.setStyle(Paint.Style.FILL); }
    private static void bx(Canvas c, double x, double y, double w, double h, int col) { P.setColor(col); P.setStyle(Paint.Style.FILL); c.drawRoundRect(new RectF((float) x, (float) y, (float) (x + w), (float) (y + h)), 8, 8, P); }
    private static void tx(Canvas c, String s, double x, double y, int col, float size) { P.setColor(col); P.setStyle(Paint.Style.FILL); P.setTextSize(size); P.setFakeBoldText(true); c.drawText(s, (float) x, (float) y, P); P.setFakeBoldText(false); }
    private static void ar(Canvas c, double x1, double y1, double x2, double y2, int col, float w) {
        ln(c, x1, y1, x2, y2, col, w);
        double dx = x2 - x1, dy = y2 - y1, l = Math.sqrt(dx * dx + dy * dy); if (l < 1) return;
        double ux = dx / l, uy = dy / l;
        ln(c, x2, y2, x2 - ux * 16 - uy * 9, y2 - uy * 16 + ux * 9, col, w); ln(c, x2, y2, x2 - ux * 16 + uy * 9, y2 - uy * 16 - ux * 9, col, w);
    }
    private static void ov(Canvas c, double x, double y, double rx, double ry, int col, float w) {
        P.setColor(col); P.setStrokeWidth(w); P.setStyle(Paint.Style.STROKE); c.drawOval(new RectF((float) (x - rx), (float) (y - ry), (float) (x + rx), (float) (y + ry)), P); P.setStyle(Paint.Style.FILL);
    }
    private static double tri(double v) { double f = v - Math.floor(v); return f < .5 ? f * 2 : 2 - f * 2; }
    private static double clamp(double v, double lo, double hi) { return Math.max(lo, Math.min(hi, v)); }
    static int starColor(double t) {
        return t < 3700 ? Color.rgb(255, 110, 60) : t < 5200 ? Color.rgb(255, 170, 90) : t < 6000 ? Color.rgb(255, 215, 130)
            : t < 7500 ? Color.rgb(255, 240, 190) : t < 10000 ? Color.rgb(235, 240, 255) : t < 30000 ? Color.rgb(180, 210, 255) : Color.rgb(150, 180, 255);
    }
    static int wavelengthColor(double nm) {
        double r = 0, g = 0, b = 0;
        if (nm >= 380 && nm < 440) { r = -(nm - 440) / 60; b = 1; } else if (nm < 490) { g = (nm - 440) / 50; b = 1; }
        else if (nm < 510) { g = 1; b = -(nm - 510) / 20; } else if (nm < 580) { r = (nm - 510) / 70; g = 1; }
        else if (nm < 645) { r = 1; g = -(nm - 645) / 65; } else if (nm <= 780) { r = 1; }
        else { return Color.rgb(120, 40, 40); }
        if (nm < 380) return Color.rgb(150, 90, 200);
        return Color.rgb((int) (255 * r), (int) (255 * g), (int) (255 * b));
    }
    private static void cell(Canvas c, double x, double y, double w, double h, int col) { bx(c, x, y, w, h, col); }

    /** u,v are slider fractions 0..1; x,y real values; t seconds. */
    static void draw(Canvas c, int k, double x, double y, double u, double v, double t) {
        switch (k) {
            case 0: { // Newton 2nd law cart
                double a = x / y, d = 0.5 * a * Math.pow(t % 4, 2) * 6;
                double w = 60 + y * 1.2, cx = 260 + (d % 560);
                ln(c, 40, 330, 960, 330, MUTED, 4);
                bx(c, cx, 330 - 70, w, 62, ORANGE); fc(c, cx + 14, 330, 12, WHITE); fc(c, cx + w - 14, 330, 12, WHITE);
                tx(c, n(y, 0) + "kg", cx + 6, 295, BG, 18);
                ar(c, cx - 8 - (30 + x * 2.2), 295, cx - 8, 295, GOLD, 7);
                tx(c, "F=" + n(x, 0) + "N", 70, 90, GOLD, 28); tx(c, "a=" + n(a, 2) + " m/s2", 70, 135, CYAN, 28); break;
            }
            case 1: { // projectile
                double th = Math.toRadians(x), vx = y * Math.cos(th), vy = y * Math.sin(th), T = 2 * vy / 9.80665, rng = Math.max(1, vx * T), hmx = Math.max(1, vy * vy / (2 * 9.80665)), s = Math.min(Math.min(880 / rng, 330 / hmx), 14);
                ln(c, 30, 400, 970, 400, MUTED, 4);
                for (int i = 0; i <= 40; i++) { double tt = T * i / 40; fc(c, 60 + vx * tt * s, 400 - (vy * tt - 4.903 * tt * tt) * s, 3, CYAN); }
                double tt = Math.min(T, (t % (T + 1.0)));
                fc(c, 60 + vx * tt * s, 400 - (vy * tt - 4.903 * tt * tt) * s, 14, ORANGE);
                ln(c, 60, 400, 60 + 70 * Math.cos(th), 400 - 70 * Math.sin(th), GOLD, 5);
                tx(c, "R=" + n(y * y * Math.sin(2 * th) / 9.80665, 0) + " m", 740, 60, WHITE, 28); break;
            }
            case 2: { // energy ramp
                double hpx = 40 + x * 6.4; Path p = new Path(); p.moveTo(80, (float) (400 - hpx));
                for (int i = 1; i <= 40; i++) { double s = i / 40.0; p.lineTo((float) (80 + 520 * s), (float) (400 - hpx * (1 - s) * (1 - s))); }
                p.lineTo(960, 400); P.setStyle(Paint.Style.STROKE); P.setStrokeWidth(6); P.setColor(CYAN); c.drawPath(p, P); P.setStyle(Paint.Style.FILL);
                double s = Math.min(1, Math.pow((t % 4) / 2.2, 2)); double bxp = 80 + 520 * s, byp = 400 - hpx * (1 - s) * (1 - s) - 20;
                double extra = (t % 4) > 2.2 ? ((t % 4) - 2.2) * Math.sqrt(2 * 9.80665 * x) * 12 : 0;
                fc(c, bxp + Math.min(extra, 350), byp + (extra > 0 ? 0 : 0), 20, ORANGE);
                ln(c, 70, 400 - hpx, 70, 400, GOLD, 4); tx(c, "h=" + n(x, 0) + " m", 85, 400 - hpx - 10, GOLD, 24);
                tx(c, "v=" + n(Math.sqrt(2 * 9.80665 * x), 1) + " m/s", 640, 80, WHITE, 28); break;
            }
            case 3: { // incline with friction
                double th = Math.toRadians(x), L = 560, x0 = 120, y0 = 380;
                Path tri = new Path();
                tri.moveTo((float) x0, (float) y0); tri.lineTo((float) (x0 + L * Math.cos(th)), (float) y0); tri.lineTo((float) (x0 + L * Math.cos(th)), (float) (y0 - L * Math.sin(th))); tri.close();
                P.setColor(Color.rgb(36, 62, 88)); c.drawPath(tri, P);
                ln(c, x0, y0, x0 + L * Math.cos(th), y0 - L * Math.sin(th), CYAN, 6);
                double a = Math.max(0, 9.80665 * (Math.sin(th) - y * Math.cos(th)));
                double s = a > 0 ? 0.92 * L - Math.min(0.85 * L, 0.5 * a * Math.pow(t % 4, 2) * 28) : 0.62 * L;
                double bx0 = x0 + s * Math.cos(th), by0 = y0 - s * Math.sin(th);
                c.save(); c.rotate((float) -x, (float) bx0, (float) by0); bx(c, bx0 - 40, by0 - 44, 80, 44, a > 0 ? ORANGE : GREEN); c.restore();
                tx(c, a > 0 ? "SLIDING" : "STATIC", 700, 80, a > 0 ? ORANGE : GREEN, 30); tx(c, "mu=" + n(y, 2), 700, 125, WHITE, 26); break;
            }
            case 4: { // elastic collision
                double m1 = x, v1 = y, v1a = (m1 - 2) / (m1 + 2) * v1, v2a = 2 * m1 / (m1 + 2) * v1, vs = 20;
                double w1 = 40 + m1 * 8, w2 = 56, p = t % 4.5, start = 700 - w1 - 1.5 * v1 * vs;
                double x1, x2;
                if (p < 1.5) { x1 = start + v1 * vs * p; x2 = 700; } else { double dt = p - 1.5; x1 = 700 - w1 + v1a * vs * dt; x2 = 700 + v2a * vs * dt; }
                x1 = clamp(x1, 5, 930); x2 = clamp(x2, 5, 930);
                ln(c, 20, 330, 980, 330, MUTED, 4); bx(c, x1, 330 - 60, w1, 60, ORANGE); bx(c, x2, 330 - 50, w2, 50, CYAN);
                tx(c, "m1=" + n(m1, 1), 70, 70, ORANGE, 26); tx(c, "m2=2", 70, 110, CYAN, 26);
                tx(c, "v2'=" + n(v2a, 1) + " m/s", 640, 70, WHITE, 28); break;
            }
            case 5: { // ideal gas
                double w = 150 + y * 60, left = 80; bx(c, left, 80, w, 280, Color.rgb(25, 52, 80)); sc(c, left + w / 2, 220, 0, WHITE, 1);
                ln(c, left, 80, left + w, 80, MUTED, 4); ln(c, left, 360, left + w, 360, MUTED, 4); ln(c, left, 80, left, 360, MUTED, 4); ln(c, left + w, 80, left + w, 360, MUTED, 4);
                double sp = 0.25 + Math.sqrt(x / 400.0) * 0.9;
                for (int i = 0; i < 36; i++) {
                    double px = left + 14 + (w - 28) * tri(0.137 * i + sp * t * (0.8 + (i % 5) * .13)), py = 94 + 252 * tri(0.291 * i + sp * t * (0.6 + (i % 4) * .17));
                    fc(c, px, py, 7, x > 450 ? RED : x > 330 ? ORANGE : CYAN);
                }
                tx(c, "T=" + n(x, 0) + " K", 700, 100, WHITE, 28); tx(c, "V=" + n(y, 1) + " L", 700, 145, CYAN, 28); tx(c, "P=" + n(0.8314 * x / y, 0) + " kPa", 700, 190, GOLD, 28); break;
            }
            case 6: { // standing wave on string
                double len = 200 + x * 500, f = Math.sqrt(y / 0.001) / (2 * x), vis = Math.min(3.0, 0.5 + f / 120.0), x0 = 40;
                ln(c, x0, 220, x0 + len, 220, Color.rgb(40, 70, 100), 2);
                for (int h = 1; h <= 3; h++) {
                    Path p = new Path(); double amp = 78.0 / h;
                    for (int i = 0; i <= 80; i++) { double s = i / 80.0; double yy = 220 - amp * Math.sin(h * Math.PI * s) * Math.cos(2 * Math.PI * vis * h * 0.7 * t);
                        if (i == 0) p.moveTo((float) (x0 + len * s), (float) yy); else p.lineTo((float) (x0 + len * s), (float) yy); }
                    P.setStyle(Paint.Style.STROKE); P.setStrokeWidth(h == 1 ? 6 : 2); P.setColor(h == 1 ? GOLD : (h == 2 ? CYAN : GREEN)); c.drawPath(p, P); P.setStyle(Paint.Style.FILL);
                }
                fc(c, x0, 220, 9, WHITE); fc(c, x0 + len, 220, 9, WHITE); tx(c, "f1=" + n(f, 0) + " Hz", 700, 380, WHITE, 28); break;
            }
            case 7: { // spring-mass
                double per = 2 * Math.PI * Math.sqrt(x / y), vis = Math.min(3.0, 1 / per) + 0.15, w = 50 + x * 14;
                double disp = 100 * Math.cos(2 * Math.PI * vis * t), mx = 500 + disp;
                bx(c, 30, 130, 30, 200, MUTED);
                Path p = new Path(); p.moveTo(60, 230); double endx = mx - 0; int coils = 14;
                for (int i = 1; i <= coils; i++) { double s = i / (double) coils; p.lineTo((float) (60 + (endx - 60) * s), (float) (230 + (i % 2 == 0 ? -34 : 34))); }
                p.lineTo((float) endx, 230); P.setStyle(Paint.Style.STROKE); P.setStrokeWidth(5); P.setColor(CYAN); c.drawPath(p, P); P.setStyle(Paint.Style.FILL);
                bx(c, mx, 230 - w / 2, w, w, ORANGE); ln(c, 40, 360, 960, 360, MUTED, 3);
                tx(c, "T=" + n(per, 2) + " s", 700, 80, WHITE, 28); tx(c, "k=" + n(y, 0), 700, 125, CYAN, 26); break;
            }
            case 8: { // Snell / TIR
                double th = Math.toRadians(x), s = y * Math.sin(th);
                P.setColor(Color.argb(150, 40, 90, 140)); c.drawRect(0, 230, 1000, 440, P);
                ln(c, 0, 230, 1000, 230, WHITE, 3); ln(c, 500, 40, 500, 420, MUTED, 2);
                double ix = 500 - 250 * Math.sin(th), iy = 230 + 250 * Math.cos(th);
                ln(c, ix, iy, 500, 230, GOLD, 6);
                if (s >= 1) ln(c, 500, 230, 500 + 250 * Math.sin(th), 230 + 250 * Math.cos(th), ORANGE, 6);
                else { double t2 = Math.asin(s); ln(c, 500, 230, 500 + 250 * Math.sin(t2), 230 - 250 * Math.cos(t2), CYAN, 6); ln(c, 500, 230, 500 + 250 * Math.sin(th) * .6, 230 + 250 * Math.cos(th) * .6, Color.argb(90, 255, 143, 92), 3); }
                tx(c, "n=" + n(y, 2), 80, 330, WHITE, 28); tx(c, s >= 1 ? "TOTAL REFLECTION" : "REFRACTION", 80, 80, s >= 1 ? ORANGE : CYAN, 28); break;
            }
            case 9: { // thin lens
                double u2 = x, f = y, sc = 4.0; ln(c, 20, 230, 980, 230, MUTED, 2); ar(c, 500, 80, 500, 380, CYAN, 6); ar(c, 500, 380, 500, 80, CYAN, 6);
                fc(c, 500 - f * sc, 230, 6, GOLD); fc(c, 500 + f * sc, 230, 6, GOLD); tx(c, "F", 500 - f * sc - 8, 262, GOLD, 22); tx(c, "F'", 500 + f * sc - 8, 262, GOLD, 22);
                double ox = 500 - u2 * sc, tipY = 230 - 70; ar(c, ox, 230, ox, tipY, ORANGE, 6);
                double vv = Math.abs(u2 - f) < 0.3 ? 500 : u2 * f / (u2 - f); double ix = clamp(500 + vv * sc, 20, 980), m = vv / u2, iy = 230 + m * 70; iy = clamp(iy, 20, 420);
                ln(c, ox, tipY, 500, tipY, CYAN, 3); ln(c, 500, tipY, 500 + (f * sc), 230, CYAN, 3);
                if (Math.abs(u2 - f) >= 0.3) { double slope = (230 - tipY) / (f * sc); if (vv > 0) ln(c, 500 + f * sc, 230, ix, iy, Color.argb(120, 81, 219, 237), 2);
                    else ln(c, 500, tipY, 20, tipY - slope * (500 - 20), Color.argb(120, 81, 219, 237), 2); }
                ln(c, ox, tipY, 500, 230, GOLD, 3); if (vv > 0 && Math.abs(u2 - f) >= 0.3) ln(c, 500, 230, ix, iy, Color.argb(160, 246, 197, 99), 2);
                if (Math.abs(u2 - f) >= 0.3) { ar(c, ix, 230, ix, iy, vv > 0 ? GREEN : Color.argb(180, 113, 221, 163), 6); }
                tx(c, vv > 0 ? "REAL IMAGE" : "VIRTUAL IMAGE", 70, 70, vv > 0 ? GREEN : MUTED, 26); break;
            }
            case 10: { // blackbody
                ln(c, 70, 390, 960, 390, MUTED, 3); ln(c, 70, 390, 70, 40, MUTED, 3);
                for (int i = 0; i < 90; i++) { double nm = 400 + 300.0 * i / 90; bx(c, 70 + (nm - 100) / 2900.0 * 890, 392, 12, 14, wavelengthColor(nm)); }
                Path p = new Path();
                for (int i = 0; i <= 120; i++) { double nm = 100 + 2900.0 * i / 120, r = LabCatalog.planckRel(x, nm); float px = (float) (70 + (nm - 100) / 2900.0 * 890), py = (float) (385 - 310 * r); if (i == 0) p.moveTo(px, py); else p.lineTo(px, py); }
                P.setStyle(Paint.Style.STROKE); P.setStrokeWidth(5); P.setColor(starColor(x)); c.drawPath(p, P); P.setStyle(Paint.Style.FILL);
                double mx = 70 + (y - 100) / 2900.0 * 890, r = LabCatalog.planckRel(x, y); fc(c, mx, 385 - 310 * r, 11, GOLD); ln(c, mx, 385 - 310 * r, mx, 390, GOLD, 2);
                double lm = 2.897771955e-3 / x * 1e9; tx(c, "peak " + n(lm, 0) + " nm", 640, 70, WHITE, 28); break;
            }
            case 11: { // gravity of a planet
                double r = 40 + 40 * y, cx = 300, cy = 220; double g = 6.6743e-11 * x * 5.972e24 / Math.pow(y * 6.371e6, 2);
                fc(c, cx, cy, r, x > 8 ? Color.rgb(200, 150, 100) : Color.rgb(60, 130, 190)); sc(c, cx, cy, r, WHITE, 3);
                double al = clamp(g * 6, 30, 110);
                for (int i = 0; i < 6; i++) { double a = i * Math.PI / 3 + 0.3, sx = cx + (r + al + 20) * Math.cos(a), sy = cy + (r + al + 20) * Math.sin(a), ex = cx + (r + 18) * Math.cos(a), ey = cy + (r + 18) * Math.sin(a); if (sx > 20 && sx < 640 && sy > 20 && sy < 420) ar(c, sx, sy, ex, ey, GOLD, 5); }
                bx(c, 760, 400 - clamp(g / 9.80665, 0, 3.4) * 100 - 10, 60, clamp(g / 9.80665, 0, 3.4) * 100 + 10, ORANGE); ln(c, 740, 300, 840, 300, WHITE, 2); tx(c, "Earth g", 735, 290, WHITE, 18);
                tx(c, "g=" + n(g, 2) + " m/s2", 620, 60, WHITE, 28); break;
            }
            case 12: { // Hubble
                double z = Math.sqrt((1 + x * y / 299792.458) / (1 - x * y / 299792.458)) - 1;
                for (int i = 0; i < 120; i++) { double nm = 380 + 400.0 * i / 120; bx(c, 120 + 760.0 * i / 120, 60, 7, 70, wavelengthColor(nm)); }
                for (int i = 0; i < 120; i++) { double nm = 380 + 400.0 * i / 120; bx(c, 120 + 760.0 * i / 120, 200, 7, 70, wavelengthColor(nm)); }
                double[] lines = {410, 434, 486, 656};
                for (double l : lines) { ln(c, 120 + (l - 380) / 400.0 * 760, 60, 120 + (l - 380) / 400.0 * 760, 130, BG, 5); double sl = l * (1 + z); if (sl <= 780) ln(c, 120 + (sl - 380) / 400.0 * 760, 200, 120 + (sl - 380) / 400.0 * 760, 270, BG, 5); }
                tx(c, "REST", 30, 100, WHITE, 20); tx(c, "GALAXY", 30, 240, ORANGE, 20); fc(c, 150 + 600 * u, 360, 14, GOLD); fc(c, 90, 360, 8, CYAN);
                ar(c, 170 + 600 * u, 360, 210 + 600 * u + 30 * (y / 80), 360, ORANGE, 5); tx(c, "z=" + n(z, 3), 700, 420, WHITE, 28); break;
            }
            case 13: { // binary orbit
                double r = 40 + x * 3, per = 1.5 + y * 0.14, ang = 2 * Math.PI * t / per;
                ov(c, 500, 220, r, r, Color.rgb(50, 82, 110), 2); fc(c, 500, 220, 6, WHITE);
                fc(c, 500 + r * Math.cos(ang), 220 + r * Math.sin(ang), 24, ORANGE); fc(c, 500 - r * Math.cos(ang), 220 - r * Math.sin(ang), 24, CYAN);
                tx(c, "M=" + n(x * x * x / (y * y), 2) + " Msun", 70, 70, WHITE, 28); break;
            }
            case 14: { // star luminosity
                double r = 14 + 22 * Math.sqrt(x), L = x * x * Math.pow(y / 5772.0, 4);
                int col = starColor(y); sc(c, 400, 220, r + 14 + 6 * Math.log10(1 + L), Color.argb(70, Color.red(col), Color.green(col), Color.blue(col)), 12);
                fc(c, 400, 220, r, col); fc(c, 820, 220, 14, GOLD); sc(c, 820, 220, 24, Color.argb(90, 246, 197, 99), 4); tx(c, "Sun", 795, 275, WHITE, 22);
                tx(c, "L=" + n(L, 2) + " Lsun", 70, 60, WHITE, 28); tx(c, n(y, 0) + " K", 70, 105, col, 28); break;
            }
            case 15: { // parallax
                double d = 40 + (1 - u) * 160, sy = 240 - (d + 100), ang = t * 0.5, ex = 500 + 100 * Math.cos(ang), ey = 300 + 30 * Math.sin(ang);
                fc(c, 500, 300, 18, GOLD); ov(c, 500, 300, 100, 30, Color.rgb(50, 82, 110), 2);
                fc(c, 500, sy, 11, WHITE); ln(c, 400, 300, 500, sy, Color.argb(120, 81, 219, 237), 2); ln(c, 600, 300, 500, sy, Color.argb(120, 81, 219, 237), 2);
                fc(c, ex, ey, 10, CYAN); ln(c, ex, ey, 500, sy, CYAN, 3);
                tx(c, "p=" + n(x, 0) + " mas", 70, 70, WHITE, 28); tx(c, "d=" + n(1000 / x, 1) + " pc", 70, 115, CYAN, 28); break;
            }
            case 16: { // transit
                double rs = 40 + 60 * y, rp = rs * Math.sqrt(Math.max(1e-6, Math.pow(x / (109.076 * y), 2))), depth = Math.pow(x / (109.076 * y), 2);
                double p = (t * 0.25) % 1.0, px = 200 + 600 * p; fc(c, 500, 130, rs * 0.75, Color.rgb(255, 215, 130)); fc(c, 200 + 600 * p, 130, rp * 0.75, BG);
                ln(c, 60, 400, 940, 400, MUTED, 2); double dip = Math.min(70, 2500 * depth);
                Path path = new Path(); path.moveTo(60, 330);
                for (int i = 0; i <= 100; i++) { double pp = i / 100.0, xx = 60 + 880 * pp; double ph = Math.abs(pp - 0.5); double yy = 330 + (ph < 0.18 ? dip * (1 - Math.max(0, (ph - 0.12) / 0.06)) : 0); if (pp > p + 0.001) break; path.lineTo((float) xx, (float) yy); }
                P.setStyle(Paint.Style.STROKE); P.setStrokeWidth(5); P.setColor(CYAN); c.drawPath(path, P); P.setStyle(Paint.Style.FILL);
                tx(c, "depth " + n(100 * depth, 3) + "%", 700, 60, WHITE, 26); break;
            }
            case 17: { // habitable zone
                double lo = 0.95 * Math.sqrt(x), hi = 1.37 * Math.sqrt(x), s = 150;
                P.setStyle(Paint.Style.STROKE); P.setColor(Color.argb(70, 113, 221, 163)); P.setStrokeWidth((float) ((hi - lo) * s)); c.drawCircle(120, 220, (float) ((lo + hi) / 2 * s), P); P.setStyle(Paint.Style.FILL);
                fc(c, 120, 220, 16 + 12 * Math.pow(x, 0.3), Color.rgb(255, 215, 130));
                double pr = y * s; double teq = 278.3 * Math.pow(0.7, 0.25) * Math.pow(x, 0.25) / Math.sqrt(y);
                sc(c, 120, 220, pr, Color.rgb(60, 90, 120), 2);
                double px = 120 + pr, py = 220; fc(c, px, py, 12, teq > 273 && teq < 373 ? GREEN : (teq >= 373 ? RED : CYAN));
                tx(c, "Teq=" + n(teq, 0) + " K", 640, 60, WHITE, 28); tx(c, (y >= lo && y <= hi) ? "IN HABITABLE ZONE" : "OUTSIDE", 640, 105, (y >= lo && y <= hi) ? GREEN : ORANGE, 24); break;
            }
            case 18: { // tides
                double tide = y * Math.pow(384.4 / x, 3), mx = 560 + (x - 300) * 1.6; fc(c, 400, 220, 66, Color.rgb(90, 140, 90));
                ov(c, 400, 220, 80 + 18 * tide, 86, Color.argb(200, 60, 150, 220), 10); fc(c, mx, 220, 14 + 10 * Math.sqrt(y), MUTED);
                ar(c, mx - 24, 220, mx - 90, 220, GOLD, 4); tx(c, "tide x" + n(tide, 2), 70, 60, WHITE, 28);
                break;
            }
            case 19: { // black hole time dilation
                double r = 40 + x * 0.35, f = Math.sqrt(1 - 1 / y);
                sc(c, 300, 220, r * 1.5, Color.argb(160, 255, 143, 92), 4); ov(c, 300, 220, r * 2.2, r * 0.5, Color.argb(190, 246, 197, 99), 6); fc(c, 300, 220, r, Color.BLACK); sc(c, 300, 220, r, MUTED, 2);
                double cx2 = 300 + r * y; if (cx2 < 650) fc(c, cx2, 220, 8, CYAN);
                sc(c, 790, 150, 60, WHITE, 4); ln(c, 790, 150, 790 + 50 * Math.sin(t), 150 - 50 * Math.cos(t), WHITE, 4);
                sc(c, 790, 330, 60, ORANGE, 4); ln(c, 790, 330, 790 + 50 * Math.sin(t * f), 330 - 50 * Math.cos(t * f), ORANGE, 4);
                tx(c, "far", 700, 235, WHITE, 20); tx(c, "near x" + n(f, 2), 640, 420, ORANGE, 22); tx(c, "Rs=" + n(2.9533 * x, 1) + " km", 60, 60, WHITE, 28); break;
            }
            case 20: { // comet orbit
                double e = y, rx = 300, ry = rx * Math.sqrt(1 - e * e), cx = 560, cy = 220, fx = cx - rx * e;
                ov(c, cx, cy, rx, ry, CYAN, 3); fc(c, fx, cy, 18, GOLD);
                double M = 2 * Math.PI * (t / (6.0 + 0.2 * x) % 1.0), E = M; for (int i = 0; i < 8; i++) E = E - (E - e * Math.sin(E) - M) / (1 - e * Math.cos(E));
                double px = cx + rx * Math.cos(E), py = cy + ry * Math.sin(E), dx = px - fx, dy = py - cy, dl = Math.sqrt(dx * dx + dy * dy) + 1e-6;
                ln(c, px, py, px + dx / dl * 70, py + dy / dl * 70, Color.argb(160, 180, 220, 255), 6); fc(c, px, py, 9, WHITE);
                double vp = Math.sqrt(1.32712440018e20 * (1 + e) / (x * 1.495978707e11 * (1 - e))) / 1000;
                tx(c, "vp=" + n(vp, 1) + " km/s", 70, 60, WHITE, 28); tx(c, "e=" + n(e, 2), 70, 105, GOLD, 26); break;
            }
            case 21: { // radial velocity
                double K = 28.4329 * x * Math.pow(y / 365.25, -1.0 / 3.0), amp = 10 + 12 * Math.log10(1 + K), per = 1.5 + y / 60.0, ang = 2 * Math.PI * t / per;
                ov(c, 250, 220, 130, 130, Color.rgb(50, 82, 110), 2); double sx = 250 - 0.12 * amp * Math.cos(ang) * 1.0 - 0, sy = 220;
                fc(c, 250 + amp * 0.4 * Math.cos(ang + Math.PI), 220, 28, Color.rgb(255, 215, 130)); fc(c, 250 + 130 * Math.cos(ang), 220 + 130 * Math.sin(ang), 10, CYAN);
                ln(c, 520, 220, 960, 220, MUTED, 2); Path p = new Path();
                for (int i = 0; i <= 100; i++) { double s = i / 100.0, yy = 220 - amp * 2.4 * Math.sin(2 * Math.PI * (s * 2.2 - t / per)); float xx = (float) (520 + 440 * s); if (i == 0) p.moveTo(xx, (float) yy); else p.lineTo(xx, (float) yy); }
                P.setStyle(Paint.Style.STROKE); P.setStrokeWidth(5); P.setColor(ORANGE); c.drawPath(p, P); P.setStyle(Paint.Style.FILL); tx(c, "K=" + n(K, 1) + " m/s", 560, 60, WHITE, 28); break;
            }
            case 22: { // hydrogen levels
                int nl = (int) Math.round(y), nu = (int) Math.round(x); if (nu <= nl) nu = nl + 1; double lam = LabCatalog.hydrogenNm(x, y);
                for (int i = 1; i <= 10; i++) {
                    double dy = 380 - 320 * (1 - 1.0 / (i * i));
                    ln(c, 120, dy, 420, dy, i == nu ? GOLD : (i == nl ? CYAN : Color.rgb(60, 90, 120)), (i == nu || i == nl) ? 5 : 2); if (i <= 5 || i == nu || i == nl) tx(c, "n=" + i, 430, dy + 7, MUTED, 18);
                }
                double y1 = 380 - 320 * (1 - 1.0 / (nu * nu)), y2 = 380 - 320 * (1 - 1.0 / (nl * nl)); ar(c, 270, y1, 270, y2, wavelengthColor(lam), 7);
                double ph = t * 4;
                Path p = new Path(); for (int i = 0; i <= 60; i++) { double s = i / 60.0; float xx = (float) (470 + 460 * s), yy = (float) (220 + 36 * Math.sin(ph - s * 18)); if (i == 0) p.moveTo(xx, yy); else p.lineTo(xx, yy); }
                P.setStyle(Paint.Style.STROKE); P.setStrokeWidth(6); P.setColor(wavelengthColor(lam)); c.drawPath(p, P); P.setStyle(Paint.Style.FILL);
                tx(c, n(lam, 1) + " nm", 640, 140, WHITE, 30); tx(c, nu + " > " + nl, 640, 330, GOLD, 28); break;
            }
            case 23: { // antenna and wavelength
                double lam = 299.792458 / x, ant = 30 + y * 0.8, spacing = clamp(24 + 14 * lam, 24, 170);
                for (int i = 0; i < 8; i++) { double r = ((t * 60 + i * spacing) % (spacing * 8)); sc(c, 220, 220, r, Color.argb((int) (200 * (1 - r / (spacing * 8))), 81, 219, 237), 3); }
                ln(c, 220, 220 - ant / 2, 220, 220 + ant / 2, GOLD, 10); ln(c, 220, 220 + ant / 2, 220, 400, MUTED, 4);
                ln(c, 520, 380, 520 + spacing, 380, WHITE, 5); tx(c, "lambda=" + n(lam, 2) + " m", 520, 360, WHITE, 24);
                tx(c, "f=" + n(x, 0) + " MHz", 640, 80, WHITE, 28); break;
            }
            case 24: { // FSPL
                double loss = LabCatalog.fspl(x * 1000, y), endAmp = Math.max(3, 60 * Math.pow(10, -(loss - 80) / 60.0));
                bx(c, 70, 200, 14, 190, MUTED); bx(c, 900, 260, 14, 130, MUTED); fc(c, 77, 190, 12, GOLD); fc(c, 907, 250, 12, GOLD);
                Path p = new Path(); for (int i = 0; i <= 120; i++) { double s = i / 120.0, a = 60 * (1 - s) + endAmp * s, yy = 220 + a * Math.sin(s * 40 - t * 6) * (1 - 0.0); float xx = (float) (90 + 800 * s); if (i == 0) p.moveTo(xx, (float) yy); else p.lineTo(xx, (float) yy); }
                P.setStyle(Paint.Style.STROKE); P.setStrokeWidth(5); P.setColor(CYAN); c.drawPath(p, P); P.setStyle(Paint.Style.FILL);
                tx(c, "FSPL " + n(loss, 1) + " dB", 330, 80, WHITE, 32); tx(c, n(y, 0) + " km  @ " + n(x, 1) + " GHz", 330, 125, MUTED, 24); break;
            }
            case 25: { // link budget
                double rx = x + 20 - LabCatalog.fspl(900, y), frac = clamp((rx + 120) / 90.0, 0, 1);
                bx(c, 70, 160, 14, 190, MUTED); bx(c, 840, 220, 14, 130, MUTED); fc(c, 77, 150, 12, GOLD); fc(c, 847, 210, 12, GOLD);
                for (int i = 0; i < 5; i++) { double r = (t * 120 + i * 140) % 700; sc(c, 77, 150, r, Color.argb((int) (150 * (1 - r / 700)), 81, 219, 237), 3); }
                bx(c, 900, 100, 50, 250, Color.rgb(25, 52, 80)); bx(c, 900, 350 - 250 * frac, 50, 250 * frac, frac > .45 ? GREEN : frac > .15 ? GOLD : RED);
                tx(c, "Rx " + n(rx, 1) + " dBm", 300, 70, WHITE, 30); tx(c, "margin " + n(rx + 100, 1) + " dB", 300, 115, rx + 100 > 0 ? GREEN : RED, 26); break;
            }
            case 26: { // Nyquist
                double N = 24, fsig = x - y * Math.rint(x / y);
                ln(c, 40, 220, 960, 220, Color.rgb(40, 70, 100), 2);
                Path p = new Path(), q = new Path();
                for (int i = 0; i <= 600; i++) { double s = i / 600.0; float xx = (float) (40 + 920 * s); double yy = 220 - 120 * Math.sin(2 * Math.PI * x / y * N * s + t * 0), yy2 = 220 - 120 * Math.sin(2 * Math.PI * fsig / y * N * s);
                    if (i == 0) { p.moveTo(xx, (float) yy); q.moveTo(xx, (float) yy2); } else { p.lineTo(xx, (float) yy); q.lineTo(xx, (float) yy2); } }
                P.setStyle(Paint.Style.STROKE); P.setStrokeWidth(2); P.setColor(Color.argb(120, 81, 219, 237)); c.drawPath(p, P); P.setStrokeWidth(4); P.setColor(x <= y / 2 ? GREEN : ORANGE); c.drawPath(q, P); P.setStyle(Paint.Style.FILL);
                for (int i = 0; i <= N; i++) { double s = i / N; fc(c, 40 + 920 * s, 220 - 120 * Math.sin(2 * Math.PI * x / y * i), 7, GOLD); }
                tx(c, "f=" + n(x, 0) + "  fs=" + n(y, 0), 60, 60, WHITE, 26); tx(c, x <= y / 2 ? "OK" : "ALIAS " + n(Math.abs(fsig), 0) + " Hz", 700, 60, x <= y / 2 ? GREEN : ORANGE, 30); break;
            }
            case 27: { // Shannon
                double snr = Math.pow(10, y / 10), cap = x * Math.log(1 + snr) / Math.log(2), h = 30 + x * 1.6, top = 220 - h / 2;
                bx(c, 80, top, 840, h, Color.rgb(25, 52, 80)); Random rnd = new Random((long) (t * 8)); int noise = (int) ((40 - y) * 2 + 4);
                for (int i = 0; i < noise; i++) fc(c, 90 + rnd.nextDouble() * 820, top + 4 + rnd.nextDouble() * (h - 8), 3, Color.argb(160, 255, 99, 99));
                double sp = 40 + cap * 1.3; for (int i = 0; i < 14; i++) { double bxp = 80 + ((i * 64 + t * sp) % 840); bx(c, bxp, 215, 22, 12, i % 2 == 0 ? CYAN : GOLD); }
                tx(c, "C=" + n(cap, 1) + " Mb/s", 80, 70, WHITE, 30); tx(c, "B=" + n(x, 0) + " MHz  SNR=" + n(y, 0) + " dB", 80, 115, MUTED, 24); break;
            }
            case 28: { // AM
                double m = x, r = y; ln(c, 40, 220, 960, 220, Color.rgb(40, 70, 100), 2);
                Path p = new Path(), env = new Path(), env2 = new Path();
                for (int i = 0; i <= 900; i++) { double s = i / 900.0, amp = 1 + m * Math.cos(2 * Math.PI * (2 * s - t * 0.3)); double yy = 220 - 70 * amp * Math.cos(2 * Math.PI * (2 * r * s - t * 2));
                    float xx = (float) (40 + 920 * s); if (i == 0) { p.moveTo(xx, (float) yy); env.moveTo(xx, (float) (220 - 70 * amp)); env2.moveTo(xx, (float) (220 + 70 * amp)); } else { p.lineTo(xx, (float) yy); env.lineTo(xx, (float) (220 - 70 * amp)); env2.lineTo(xx, (float) (220 + 70 * amp)); } }
                P.setStyle(Paint.Style.STROKE); P.setStrokeWidth(2); P.setColor(x > 1 ? RED : CYAN); c.drawPath(p, P); P.setStrokeWidth(3); P.setColor(GOLD); c.drawPath(env, P); c.drawPath(env2, P); P.setStyle(Paint.Style.FILL);
                tx(c, "m=" + n(m, 2), 60, 60, WHITE, 30); tx(c, m > 1 ? "OVERMODULATION" : "OK", 600, 60, m > 1 ? RED : GREEN, 28); break;
            }
            case 29: { // FM
                double beta = Math.min(8, x / y); ln(c, 40, 140, 960, 140, Color.rgb(40, 70, 100), 1); ln(c, 40, 330, 960, 330, Color.rgb(40, 70, 100), 1);
                Path msg = new Path(), fm = new Path();
                for (int i = 0; i <= 900; i++) { double s = i / 900.0, ms = Math.sin(2 * Math.PI * (2 * s - t * 0.3)); float xx = (float) (40 + 920 * s); double yy = 140 - 55 * ms, yy2 = 330 - 70 * Math.cos(2 * Math.PI * 22 * s + beta * 2 * Math.PI * 0.3 * (-Math.cos(2 * Math.PI * (2 * s - t * 0.3))) * 1.0 + t * 0);
                    if (i == 0) { msg.moveTo(xx, (float) yy); fm.moveTo(xx, (float) yy2); } else { msg.lineTo(xx, (float) yy); fm.lineTo(xx, (float) yy2); } }
                P.setStyle(Paint.Style.STROKE); P.setStrokeWidth(4); P.setColor(GOLD); c.drawPath(msg, P); P.setStrokeWidth(2); P.setColor(CYAN); c.drawPath(fm, P); P.setStyle(Paint.Style.FILL);
                tx(c, "BW=" + n(2 * (x + y), 0) + " kHz", 700, 60, WHITE, 28); tx(c, "beta=" + n(x / y, 2), 60, 60, MUTED, 26); break;
            }
            case 30: { // BPSK constellation
                double sigma = 1.0 / Math.sqrt(2 * Math.pow(10, x / 10)), unit = 150; Random rnd = new Random((long) (t * 6));
                ln(c, 500, 60, 500, 400, MUTED, 3); ln(c, 140, 230, 860, 230, Color.rgb(40, 70, 100), 2);
                for (int i = 0; i < 160; i++) { int bit = rnd.nextBoolean() ? 1 : -1; double nx = rnd.nextGaussian() * sigma, ny = rnd.nextGaussian() * sigma; double px = bit + nx;
                    fc(c, 500 + px * unit, 230 + ny * unit, 4, (px * bit) < 0 ? RED : (bit > 0 ? CYAN : GOLD)); }
                fc(c, 500 - unit, 230, 7, WHITE); fc(c, 500 + unit, 230, 7, WHITE);
                double ber = 0.5 * LabCatalog.erfc(Math.sqrt(Math.pow(10, x / 10))); tx(c, "BER " + String.format(Locale.US, "%.1e", ber), 70, 60, WHITE, 28); break;
            }
            case 31: { // dish
                double dia = 60 + x * 28, lam = 299792458.0 / (y * 1e9), bw = 70 * lam / x, half = Math.toRadians(clamp(bw / 2, 1.5, 40)), f = 200;
                Path p = new Path();
                for (int i = -20; i <= 20; i++) { double yy = dia * i / 20.0; double xx = 150 + (yy * yy) / 233.0; float px = (float) xx, py = (float) (220 + yy); if (i == -20) p.moveTo(px, py); else p.lineTo(px, py); }
                P.setStyle(Paint.Style.STROKE); P.setStrokeWidth(8); P.setColor(GOLD); c.drawPath(p, P); P.setStyle(Paint.Style.FILL);
                double len = 760; ln(c, 190, 220, 190 + len, 220 - len * Math.tan(half), Color.argb(120, 81, 219, 237), 3); ln(c, 190, 220, 190 + len, 220 + len * Math.tan(half), Color.argb(120, 81, 219, 237), 3);
                for (int i = 0; i < 6; i++) { double r = ((t * 140 + i * 130) % 760); double ex = 190 + r; double hh = r * Math.tan(half); P.setStyle(Paint.Style.STROKE); P.setStrokeWidth(3); P.setColor(Color.argb((int) (180 * (1 - r / 760)), 81, 219, 237)); c.drawArc(new RectF((float) (ex - 18), (float) (220 - hh), (float) (ex + 18), (float) (220 + hh)), -90, 180, false, P); P.setStyle(Paint.Style.FILL); }
                tx(c, "G=" + n(10 * Math.log10(0.6 * Math.pow(Math.PI * x * y * 1e9 / 299792458.0, 2)), 1) + " dBi", 640, 60, WHITE, 28); tx(c, "beam " + n(bw, 1) + " deg", 640, 105, MUTED, 24); break;
            }
            case 32: { // satellite delay
                double re = 6371, e = Math.toRadians(y), dkm = Math.sqrt(Math.pow(re + x, 2) - Math.pow(re * Math.cos(e), 2)) - re * Math.sin(e), delay = dkm / 299792.458 * 1000;
                sc(c, 250, 480, 170, Color.rgb(60, 130, 190), 6); fc(c, 250, 310, 9, GOLD);
                double dpx = 70 + Math.min(220, 2.0 * delay), sx = 250 + dpx * Math.cos(e), sy = 310 - dpx * Math.sin(e);
                ln(c, 250, 310, sx, sy, Color.argb(120, 81, 219, 237), 3); fc(c, sx, sy, 15, ORANGE);
                double ph = (t * 0.7) % 1.0; fc(c, 250 + (sx - 250) * ph, 310 + (sy - 310) * ph, 7, CYAN);
                tx(c, n(delay, 1) + " ms one way", 560, 80, WHITE, 30); tx(c, "ping " + n(2 * delay, 0) + " ms", 560, 125, MUTED, 26); break;
            }
            case 33: { // radio horizon
                double d = 4.12 * (Math.sqrt(x) + Math.sqrt(y)), ds = clamp(d * 5.5, 120, 760), th = 20 + x * 0.5, rh = 12 + y * 0.8; Path g = new Path(); g.moveTo(0, 440);
                for (int i = 0; i <= 100; i++) { double xx = 1000.0 * i / 100, yy = 350 + Math.pow((xx - 500) / 500, 2) * 60; g.lineTo((float) xx, (float) yy); } g.lineTo(1000, 440); g.close(); P.setColor(Color.rgb(30, 70, 60)); c.drawPath(g, P);
                double x1 = 500 - ds / 2, x2 = 500 + ds / 2, b1 = 350 + Math.pow((x1 - 500) / 500, 2) * 60, b2 = 350 + Math.pow((x2 - 500) / 500, 2) * 60;
                ln(c, x1, b1, x1, b1 - th * 1.2, GOLD, 6); ln(c, x2, b2, x2, b2 - rh * 1.2, CYAN, 6); ln(c, x1, b1 - th * 1.2, x2, b2 - rh * 1.2, Color.argb(160, 81, 219, 237), 3);
                for (int i = 0; i < 4; i++) { double r = ((t * 120 + i * 90) % 360); sc(c, x1, b1 - th * 1.2, r, Color.argb((int) (160 * (1 - r / 360)), 246, 197, 99), 3); }
                tx(c, "range " + n(d, 1) + " km", 70, 70, WHITE, 30); break;
            }
            case 34: { // optical fibre
                double n1 = x, n2 = x * (1 - y / 100), thc = Math.asin(n2 / n1), alpha = Math.PI / 2 - thc; bx(c, 60, 120, 880, 200, Color.rgb(25, 52, 80)); bx(c, 60, 170, 880, 100, Color.rgb(50, 120, 160));
                tx(c, "cladding", 80, 150, MUTED, 20); tx(c, "core", 80, 225, WHITE, 20);
                double slope = Math.tan(alpha) * 1.6; double yy = 220, dir = 1; Path p = new Path(); p.moveTo(60, 220); double px = 60; int guard = 0;
                while (px < 940 && guard++ < 2000) { double nx = px + 6, ny = yy + dir * slope * 6; if (ny > 268) { ny = 268 - (ny - 268); dir = -1; } else if (ny < 172) { ny = 172 + (172 - ny); dir = 1; } p.lineTo((float) nx, (float) ny); px = nx; yy = ny; }
                P.setStyle(Paint.Style.STROKE); P.setStrokeWidth(5); P.setColor(GOLD); c.drawPath(p, P); P.setStyle(Paint.Style.FILL);
                double ph = (t * 0.5) % 1.0; fc(c, 60 + 880 * ph, 220, 0, WHITE);
                double na = Math.sqrt(Math.max(0, n1 * n1 - n2 * n2)); tx(c, "NA=" + n(na, 3), 640, 60, WHITE, 30); tx(c, "accept " + n(Math.toDegrees(Math.asin(Math.min(1, na))), 1) + " deg", 640, 105, MUTED, 26); break;
            }
            default: break;
        }
    }
}
