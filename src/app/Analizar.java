package app;

import app.parser.ParseException;
import app.parser.Parser;
import app.parser.ParserAdapter;
import ast.Node;
import java.util.List;
import lexer.Token;

public class Analizar {

    /**
     * Recibe los tokens reales del Lexer y se encarga de adaptarlos al Parser.
     */
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
