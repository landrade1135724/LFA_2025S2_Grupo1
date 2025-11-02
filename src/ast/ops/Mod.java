package ast.ops;

import ast.Op;

public class Mod extends Op {
    public Mod() { super("MOD"); }

    @Override
    public double evaluate() {
        exigirAridad(2, 2);
        double a = hijos.get(0).evaluate();
        double b = hijos.get(1).evaluate();
        if (b == 0.0) throw new ArithmeticException("MOD por 0");
        return a % b;
    }
}
