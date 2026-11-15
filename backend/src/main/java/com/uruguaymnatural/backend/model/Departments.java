package com.uruguaymnatural.backend.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The 19 departments of Uruguay, used to validate locations, with the coordinates
 * (latitude, longitude) of each capital to ask for the weather forecast.
 */
public final class Departments {

    private static final Map<String, double[]> CAPITALS = new LinkedHashMap<>();

    static {
        CAPITALS.put("Artigas", new double[] { -30.40, -56.47 });
        CAPITALS.put("Canelones", new double[] { -34.52, -56.28 });
        CAPITALS.put("Cerro Largo", new double[] { -32.37, -54.17 });
        CAPITALS.put("Colonia", new double[] { -34.47, -57.84 });
        CAPITALS.put("Durazno", new double[] { -33.38, -56.52 });
        CAPITALS.put("Flores", new double[] { -33.52, -56.90 });
        CAPITALS.put("Florida", new double[] { -34.10, -56.21 });
        CAPITALS.put("Lavalleja", new double[] { -34.37, -55.24 });
        CAPITALS.put("Maldonado", new double[] { -34.90, -54.95 });
        CAPITALS.put("Montevideo", new double[] { -34.90, -56.16 });
        CAPITALS.put("Paysandú", new double[] { -32.32, -58.08 });
        CAPITALS.put("Río Negro", new double[] { -33.13, -58.30 });
        CAPITALS.put("Rivera", new double[] { -30.90, -55.55 });
        CAPITALS.put("Rocha", new double[] { -34.48, -54.33 });
        CAPITALS.put("Salto", new double[] { -31.38, -57.97 });
        CAPITALS.put("San José", new double[] { -34.34, -56.71 });
        CAPITALS.put("Soriano", new double[] { -33.25, -58.03 });
        CAPITALS.put("Tacuarembó", new double[] { -31.71, -55.98 });
        CAPITALS.put("Treinta y Tres", new double[] { -33.23, -54.38 });
    }

    public static final List<String> ALL = List.copyOf(CAPITALS.keySet());

    private Departments() {
    }

    public static boolean isValid(String department) {
        return CAPITALS.containsKey(department);
    }

    /** Latitude and longitude of the department's capital (Montevideo's for an unknown name). */
    public static double[] coordinatesOf(String department) {
        return CAPITALS.getOrDefault(department, CAPITALS.get("Montevideo"));
    }
}
