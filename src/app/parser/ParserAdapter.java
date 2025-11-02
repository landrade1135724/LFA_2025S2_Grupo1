package app.parser;

import java.util.ArrayList;
import java.util.List;
import lexer.Token;

/** Adapta List<lexer.Token> -> List<Parser.Token> construyendo los "lexemas" que el Parser espera. */
public final class ParserAdapter {
    private ParserAdapter() {}

    public static List<Parser.Token> adapt(List<Token> userTokens) {
        List<Parser.Token> out = new ArrayList<>(userTokens.size());
        for (Token t : userTokens) {
            String lx;
            switch (t.type) {
                case OPERACION_OPEN -> {
                    // Tu lexer pone SOLO "SUMA" | "RESTA" | ... en lexeme
                    // El Parser espera <Operacion=SUMA>
                    lx = "<Operacion=" + t.lexeme + ">";
                }
                case OPERACION_CLOSE -> lx = "</Operacion>";
                case NUMERO_OPEN     -> lx = "<Numero>";
                case NUMERO_CLOSE    -> lx = "</Numero>";
                case P_OPEN          -> lx = "<P>";
                case P_CLOSE         -> lx = "</P>";
                case R_OPEN          -> lx = "<R>";
                case R_CLOSE         -> lx = "</R>";
                case NUMBER          -> lx = t.lexeme; // número literal
                // ignora espacios u otros tokens que no se usan en el parser sintáctico
                default              -> lx = t.lexeme;
            }
            out.add(new Parser.Token(lx, t.line, t.column));
        }
        return out;
    }
}
