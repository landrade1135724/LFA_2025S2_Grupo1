package ast.ops;

import ast.Op;

public class Division extends Op {
    public Division() { super("DIVISION"); }

    @Override
    public double evaluate() {
        exigirAridad(2, -1);
        double acc = hijos.get(0).evaluate();
        for (int i=1;i<hijos.size();i++) {
            double d = hijos.get(i).evaluate();
            if (d == 0.0) throw new ArithmeticException("DIVISION por cero");
            acc /= d;
        }
        return acc;
    }
}
