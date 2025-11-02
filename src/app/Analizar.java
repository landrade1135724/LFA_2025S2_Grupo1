package app;

import app.parser.ParseException;
import app.parser.Parser;
import app.parser.ParserAdapter;
import ast.Node;
import java.util.List;
import lexer.Token;

public class Analizar {

    public static java.util.List<ast.Node> parsearArboles(java.util.List<lexer.Token> tokensDelLexer) {
        java.util.List<ast.Node> arboles = new java.util.ArrayList<>();
        try {
            java.util.List<app.parser.Parser.Token> insumos = app.parser.ParserAdapter.adapt(tokensDelLexer);
            app.parser.Parser p = new app.parser.Parser(insumos);
            arboles = p.parsePrograma();
        } catch (Exception e) {
            // Si falla el parseo global, devolvemos lista vacía y que el HTML muestre el error en analizar(...)
        }
        return arboles;
    }

    public static java.util.List<app.report.OperacionResultado> analizar(java.util.List<lexer.Token> tokensDelLexer) {
        java.util.List<app.report.OperacionResultado> lista = new java.util.ArrayList<>();
        try {
            // Adaptar tokens del lexer al parser
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
            // 1) Adaptar tokens al Parser
            List<Parser.Token> insumos = ParserAdapter.adapt(tokensDelLexer);

            // 2) Parsear a árboles
            Parser p = new Parser(insumos);
            List<Node> arboles = p.parsePrograma();

            // 3) Evaluar y armar reporte
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
}
