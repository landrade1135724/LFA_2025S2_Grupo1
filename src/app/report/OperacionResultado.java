package app.report;

public class OperacionResultado {
    public final int indice;
    public final String expresion;
    public final boolean ok;
    public final double valor;
    public final String error;

    public OperacionResultado(int indice, String expresion, double valor) {
        this.indice = indice;
        this.expresion = expresion;
        this.ok = true;
        this.valor = valor;
        this.error = "";
    }

    public OperacionResultado(int indice, String expresion, String error) {
        this.indice = indice;
        this.expresion = expresion;
        this.ok = false;
        this.valor = Double.NaN;
        this.error = error;
    }
}
