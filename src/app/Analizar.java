package app;

import app.parser.ParseException;
import app.parser.Parser;
import app.parser.ParserAdapter;
import ast.Node;
import java.util.List;
import lexer.Token;

public class Analizar {

    // --------------------- MODO NO TOLERANTE (compat) ---------------------
    public static java.util.List<ast.Node> parsearArboles(java.util.List<lexer.Token> tokensDelLexer) {
        java.util.List<ast.Node> arboles = new java.util.ArrayList<>();
        try {
            java.util.List<app.parser.Parser.Token> insumos = app.parser.ParserAdapter.adapt(tokensDelLexer);
            app.parser.Parser p = new app.parser.Parser(insumos);
            arboles = p.parsePrograma();
        } catch (Exception e) {
            // si falla, devolvemos vacío
        }
        return arboles;
    }

    public static java.util.List<app.report.OperacionResultado> analizar(java.util.List<lexer.Token> tokensDelLexer) {
        java.util.List<app.report.OperacionResultado> lista = new java.util.ArrayList<>();
        try {
            java.util.List<app.parser.Parser.Token> insumos = app.parser.ParserAdapter.adapt(tokensDelLexer);
            app.parser.Parser p = new app.parser.Parser(insumos);
            java.util.List<ast.Node> arboles = p.parsePrograma();

            int i = 1;
            for (ast.Node n : arboles) {
                try {
                    double valor = n.evaluate();
                    lista.add(new app.report.OperacionResultado(i, n.render(), valor));
                } catch (IllegalArgumentException | ArithmeticException ae) {
                    lista.add(new app.report.OperacionResultado(i, n.render(),
                            "ERROR DE EVALUACION: " + ae.getMessage()));
                } catch (Exception e) {
                    lista.add(new app.report.OperacionResultado(i, n.render(),
                            "ERROR NO CONTROLADO: " + e.getMessage()));
                }
                i++;
            }
        } catch (app.parser.ParseException pe) {
            lista.add(new app.report.OperacionResultado(1, "(documento)",
                    "ERROR DE PARSEO: " + pe.getMessage()));
        } catch (Exception e) {
            lista.add(new app.report.OperacionResultado(1, "(documento)",
                    "ERROR NO CONTROLADO: " + e.getMessage()));
        }
        return lista;
    }

    public static String ejecutar(List<Token> tokensDelLexer) {
        StringBuilder sb = new StringBuilder();
        try {
            List<Parser.Token> insumos = ParserAdapter.adapt(tokensDelLexer);
            Parser p = new Parser(insumos);
            List<Node> arboles = p.parsePrograma();

            int i = 1;
            for (Node n : arboles) {
                double valor = n.evaluate();
                sb.append("Op ").append(i++).append(": ")
                        .append(n.render())
                        .append(" = ").append(valor)
                        .append(System.lineSeparator());
            }
        } catch (ParseException pe) {
            sb.append("ERROR DE PARSEO: ").append(pe.getMessage()).append(System.lineSeparator());
        } catch (IllegalArgumentException | ArithmeticException ae) {
            sb.append("ERROR DE EVALUACION: ").append(ae.getMessage()).append(System.lineSeparator());
        } catch (Exception e) {
            sb.append("ERROR NO CONTROLADO: ").append(e.getMessage()).append(System.lineSeparator());
        }
        return sb.toString();
    }

    // --------------------- MODO TOLERANTE (lo que necesitas) ---------------------

    /** Resultado de parseo tolerante: ASTs válidos + lista de mensajes de error. */
    public static class ParseBatch {
        public final java.util.List<ast.Node> arboles;
        public final java.util.List<String> errores;
        public ParseBatch(java.util.List<ast.Node> a, java.util.List<String> e) {
            this.arboles = a; this.errores = e;
        }
    }

    /** Usa Parser.parseBatchTolerante() y regresa mensajes legibles. */
    public static ParseBatch parsearArbolesTolerante(java.util.List<lexer.Token> tokens) {
        var tokensParser = app.parser.ParserAdapter.adapt(tokens);
        var parser = new app.parser.Parser(tokensParser);
        var arboles = parser.parseBatchTolerante();
        java.util.List<String> errs = new java.util.ArrayList<>();
        for (var e : parser.getErrores()) errs.add("ERROR DE PARSEO: " + e.toString());
        return new ParseBatch(arboles, errs);
    }

    /** Igual que 'analizar' pero continúa tras errores y agrega filas ERROR por cada fallo. */
    public static java.util.List<app.report.OperacionResultado> analizarTolerante(
            java.util.List<lexer.Token> tokens) {

        ParseBatch pb = parsearArbolesTolerante(tokens);
        java.util.List<app.report.OperacionResultado> out = new java.util.ArrayList<>();

        int idx = 1;
        // ASTs válidos → filas OK (por constructor normal)
        for (var n : pb.arboles) {
            try {
                double val = n.evaluate();
                out.add(new app.report.OperacionResultado(idx++, n.render(), val));
            } catch (IllegalArgumentException | ArithmeticException ae) {
                out.add(new app.report.OperacionResultado(idx++, n.render(),
                        "ERROR DE EVALUACION: " + ae.getMessage()));
            } catch (Exception e) {
                out.add(new app.report.OperacionResultado(idx++, n.render(),
                        "ERROR NO CONTROLADO: " + e.getMessage()));
            }
        }
        // Errores de parseo → filas ERROR
        for (var msg : pb.errores) {
            out.add(new app.report.OperacionResultado(idx++, "(operación)", msg));
        }
        return out;
    }
}
