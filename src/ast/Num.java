package ast;

/** Nodo numérico: envuelve un literal double. */
public class Num implements Node {
    private final double value;

    public Num(double value) {
        this.value = value;
    }

    @Override
    public double evaluate() {
        return value;
    }

    @Override
    public String render() {
        return Double.toString(value);
    }
}
