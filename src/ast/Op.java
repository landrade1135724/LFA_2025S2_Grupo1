package ast;

import java.util.ArrayList;
import java.util.List;

/** Nodo operación genérico con lista de hijos. */
public abstract class Op implements Node {
    protected final String nombre;       // SUMA, RESTA, etc.
    protected final List<Node> hijos;    // operandos (2+ o 1, según operación)

    protected Op(String nombre) {
        this.nombre = nombre;
        this.hijos = new ArrayList<>();
    }

    public void add(Node n) { hijos.add(n); }

    public List<Node> getHijos() { return hijos; }

    public String getNombre() { return nombre; }

    /** Validación de cantidad mínima/máxima de hijos. max<0 significa "sin tope". */
    protected void exigirAridad(int min, int max) {
        int k = hijos.size();
        if (k < min || (max >= 0 && k > max)) {
            throw new IllegalArgumentException(
                "Operación " + nombre + " con aridad inválida: " + k +
                " (esperado min=" + min + ", max=" + (max<0?"∞":max) + ")"
            );
        }
    }

    @Override
    public String render() {
        StringBuilder sb = new StringBuilder();
        sb.append(nombre).append("(");
        for (int i = 0; i < hijos.size(); i++) {
            if (i>0) sb.append(", ");
            sb.append(hijos.get(i).render());
        }
        sb.append(")");
        return sb.toString();
    }
}
