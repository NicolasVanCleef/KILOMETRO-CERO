package com.uruguaymnatural.backend.model;

import java.util.List;

/**
 * The 19 departments of Uruguay, used to validate locations.
 */
public final class Departments {

    public static final List<String> ALL = List.of(
            "Artigas", "Canelones", "Cerro Largo", "Colonia", "Durazno", "Flores", "Florida",
            "Lavalleja", "Maldonado", "Montevideo", "Paysandú", "Río Negro", "Rivera", "Rocha",
            "Salto", "San José", "Soriano", "Tacuarembó", "Treinta y Tres");

    private Departments() {
    }

    public static boolean isValid(String department) {
        return ALL.contains(department);
    }
}
