package ast.ops;

import ast.Node;
import ast.Op;

public class Suma extends Op {
    public Suma() { super("SUMA"); }

    @Override
    public double evaluate() {
        exigirAridad(2, -1); // 2 o más
        double acc = 0.0;
        for (Node n : hijos) acc += n.evaluate();
        return acc;
    }
}
