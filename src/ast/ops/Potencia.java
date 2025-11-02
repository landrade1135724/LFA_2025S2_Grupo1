package ast.ops;

import ast.Op;

public class Potencia extends Op {
    public Potencia() { super("POTENCIA"); }

    @Override
    public double evaluate() {
        exigirAridad(2, 2); // base^exponente
        double base = hijos.get(0).evaluate();
        double exp  = hijos.get(1).evaluate();
        return Math.pow(base, exp);
    }
}
