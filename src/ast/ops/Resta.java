package ast.ops;

import ast.Op;

public class Resta extends Op {
    public Resta() { super("RESTA"); }

    @Override
    public double evaluate() {
        exigirAridad(2, -1); // 2 o más (a - b - c - ...)
        double acc = hijos.get(0).evaluate();
        for (int i=1;i<hijos.size();i++) acc -= hijos.get(i).evaluate();
        return acc;
    }
}
