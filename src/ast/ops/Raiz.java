package ast.ops;

import ast.Op;

public class Raiz extends Op {
    public Raiz() { super("RAIZ"); }

    @Override
    public double evaluate() {
        // Convención: RAIZ(grado, radicando)  → si sólo dan 1 hijo, asumimos grado=2 (raíz cuadrada)
        if (hijos.size() == 1) {
            double x = hijos.get(0).evaluate();
            if (x < 0) throw new ArithmeticException("RAIZ de negativo");
            return Math.sqrt(x);
        }
        exigirAridad(2, 2);
        double grado = hijos.get(0).evaluate();
        double x     = hijos.get(1).evaluate();
        if (grado == 0) throw new ArithmeticException("RAIZ con grado 0");
        if (x < 0 && (Math.floor(grado) == grado) && (((int)grado) % 2 == 0)) {
            throw new ArithmeticException("RAIZ par de negativo");
        }
        return Math.pow(x, 1.0 / grado);
    }
}
