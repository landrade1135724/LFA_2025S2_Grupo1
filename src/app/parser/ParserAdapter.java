package app.parser;

import java.util.ArrayList;
import java.util.List;
import lexer.Token;

/**
 * Adapta List<lexer.Token> -> List<Parser.Token>
 * usando los nuevos nombres de TokenType definidos en tu enum.
 */
public final class ParserAdapter {
    private ParserAdapter() {}

    public static List<Parser.Token> adapt(List<Token> userTokens) {
        List<Parser.Token> out = new ArrayList<>(userTokens.size());

        for (Token t : userTokens) {
            String lx = t.lexeme;

            switch (t.type) {
                // === Operación ===
                case ABRIROPERACION -> {
                    // Ej: lexeme = SUMA | RESTA | DIVISION ...
                    lx = "<Operacion=" + t.lexeme + ">";
                }
                case CERRAROPERACION -> lx = "</Operacion>";

                // === Número ===
                case ABRIRONUMERO -> lx = "<Numero>";
                case CERRARNUMERO -> lx = "</Numero>";

                // === P y R ===
                case ABRIRO_P -> lx = "<P>";
                case CERRAR_P -> lx = "</P>";
                case ABRIRO_R -> lx = "<R>";
                case CERRAR_R -> lx = "</R>";

                // === Número literal ===
                case NUMERO -> lx = t.lexeme;

                // === Ignorables ===
                case ESPACIO -> lx = ""; // ignoramos espacios

                // === Otros casos: mantenemos el lexema como está ===
                default -> lx = t.lexeme;
            }

            out.add(new Parser.Token(lx, t.line, t.column));
        }

        return out;
    }
}
