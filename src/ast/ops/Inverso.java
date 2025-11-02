package ast.ops;

import ast.Op;

public class Inverso extends Op {
    public Inverso() { super("INVERSO"); }

    @Override
    public double evaluate() {
        exigirAridad(1, 1);
        double x = hijos.get(0).evaluate();
        if (x == 0.0) throw new ArithmeticException("INVERSO de 0");
        return 1.0 / x;
    }
}
