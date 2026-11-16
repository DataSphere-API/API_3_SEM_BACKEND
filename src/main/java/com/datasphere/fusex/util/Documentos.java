package com.datasphere.fusex.util;

public final class Documentos {

    private Documentos() {
    }

    public static String somenteDigitos(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replaceAll("\\D", "");
    }
}