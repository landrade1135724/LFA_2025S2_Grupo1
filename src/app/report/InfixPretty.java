package app.report;

import ast.Node;
import ast.Num;
import ast.Op;
import ast.ops.*;

public final class InfixPretty {
    private InfixPretty(){}

    // Renderiza una expresión a notación infija legible.
    public static String render(Node n) {
        if (n instanceof Num num) {
            double v = num.evaluate();
            // Mostrar enteros sin ".0"
            return (v == Math.rint(v)) ? String.valueOf((long)v) : String.valueOf(v);
        }
        if (n instanceof Op op) {
            if (op instanceof Suma)              return join(op, " + ");
            if (op instanceof Resta)             return join(op, " - ");
            if (op instanceof Multiplicacion)    return join(op, " * ");
            if (op instanceof Division)          return join(op, " / ");
            if (op instanceof Potencia) {
                var h = op.getHijos(); // convención: hijos = [exponente, base] (por <P> y la operación)
                if (h.size()==2) return "(" + render(h.get(1)) + ")^" + render(h.get(0));
            }
            if (op instanceof Raiz) {
                var h = op.getHijos(); // [indice, radicando]
                if (h.size()==2) return render(h.get(0)) + "√(" + render(h.get(1)) + ")";
            }
            if (op instanceof Inverso) {
                var h = op.getHijos();
                if (h.size()==1) return "1/(" + render(h.get(0)) + ")";
            }
            if (op instanceof Mod) {
                var h = op.getHijos();
                if (h.size()==2) return render(h.get(0)) + " % " + render(h.get(1));
            }
            // fallback
            return op.getNombre() + "(" + join(op.getHijos(), ", ") + ")";
        }
        return n.render();
    }

    private static String join(Op op, String sep) {
        return join(op.getHijos(), sep);
    }
    private static String join(java.util.List<Node> hs, String sep) {
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<hs.size();i++) {
            if (i>0) sb.append(sep);
            sb.append(render(hs.get(i)));
        }
        return sb.toString();
    }
}
