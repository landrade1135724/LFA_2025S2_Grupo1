package ast;

/**
 * Nodo del AST. Todos los nodos deben poder evaluarse a double.
 */
public interface Node {
    double evaluate();
    String render(); // descripción breve (útil para HTML/logs)
}
