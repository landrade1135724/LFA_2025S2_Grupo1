package ast.ops;

import ast.Node;
import ast.Op;

public class Multiplicacion extends Op {
    public Multiplicacion() { super("MULTIPLICACION"); }

    @Override
    public double evaluate() {
        exigirAridad(2, -1);
        double acc = 1.0;
        for (Node n : hijos) acc *= n.evaluate();
        return acc;
    }
}
