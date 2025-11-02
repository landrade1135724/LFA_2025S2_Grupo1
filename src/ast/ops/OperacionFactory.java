package ast.ops;

import ast.Op;

/** Fábrica que devuelve la operación correcta según el nombre del atributo. */
public final class OperacionFactory {
    private OperacionFactory(){}

    public static Op of(String nombre) {
        String n = nombre.toUpperCase();
        return switch (n) {
            case "SUMA"            -> new Suma();
            case "RESTA"           -> new Resta();
            case "MULTIPLICACION"  -> new Multiplicacion();
            case "DIVISION"        -> new Division();
            case "POTENCIA"        -> new Potencia();
            case "RAIZ"            -> new Raiz();
            case "INVERSO"         -> new Inverso();
            case "MOD"             -> new Mod();
            default -> throw new IllegalArgumentException("Operacion desconocida: " + nombre);
        };
    }
}
