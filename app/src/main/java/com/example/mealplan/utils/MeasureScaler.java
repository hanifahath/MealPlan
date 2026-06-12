package com.example.mealplan.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MeasureScaler {

    private static final Pattern LEADING = Pattern.compile(
            "^\\s*(\\d+\\s+\\d+/\\d+|\\d+/\\d+|\\d+(?:[.,]\\d+)?)"
                    + "(\\s*-\\s*(\\d+\\s+\\d+/\\d+|\\d+/\\d+|\\d+(?:[.,]\\d+)?))?");

    public static String scale(String measure, float factor) {
        if (measure == null) return "";
        String m = measure.trim();
        if (m.isEmpty() || Math.abs(factor - 1f) < 0.001f) return measure;

        Matcher matcher = LEADING.matcher(m);
        if (!matcher.find()) return measure;

        String first = matcher.group(1);
        String rangeSecond = matcher.group(3);
        String rest = m.substring(matcher.end());

        StringBuilder sb = new StringBuilder();
        sb.append(format(parse(first) * factor));
        if (rangeSecond != null) {
            sb.append("-").append(format(parse(rangeSecond) * factor));
        }
        sb.append(rest);
        return sb.toString();
    }

    private static double parse(String s) {
        s = s.trim().replace(",", ".");
        if (s.contains(" ")) {
            String[] parts = s.split("\\s+");
            return parseSingle(parts[0]) + parseSingle(parts[1]);
        }
        return parseSingle(s);
    }

    private static double parseSingle(String s) {
        try {
            if (s.contains("/")) {
                String[] f = s.split("/");
                double den = Double.parseDouble(f[1]);
                if (den == 0) return 0;
                return Double.parseDouble(f[0]) / den;
            }
            return Double.parseDouble(s);
        } catch (Exception e) {
            return 0;
        }
    }

    private static String format(double v) {
        double rounded = Math.round(v * 100.0) / 100.0;
        long whole = (long) Math.floor(rounded + 1e-9);
        double frac = rounded - whole;

        String fracStr = nearestFraction(frac);
        if (fracStr == null) {
            if (Math.abs(rounded - Math.rint(rounded)) < 1e-9)
                return String.valueOf((long) Math.rint(rounded));
            String out = String.valueOf(rounded);
            if (out.endsWith(".0")) out = out.substring(0, out.length() - 2);
            return out;
        }
        if (fracStr.isEmpty()) return String.valueOf(whole);
        if (fracStr.equals("1")) return String.valueOf(whole + 1);
        return whole == 0 ? fracStr : (whole + " " + fracStr);
    }

    private static String nearestFraction(double frac) {
        double[] vals = {0, 0.25, 0.333, 0.5, 0.667, 0.75, 1};
        String[] reps = {"", "1/4", "1/3", "1/2", "2/3", "3/4", "1"};
        double tol = 0.06;
        for (int i = 0; i < vals.length; i++) {
            if (Math.abs(frac - vals[i]) <= tol) return reps[i];
        }
        return null;
    }
}