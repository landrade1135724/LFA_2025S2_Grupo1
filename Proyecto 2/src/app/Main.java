package app;

import lexer.*;

public class Main {
    public static void main(String[] args) throws Exception {
        String path = args.length > 0 ? args[0] : "resources/entrada.txt";
        String src = Lexer.readFile(path);
        Lexer lx = new Lexer(src);
        Token t;
        while ((t = lx.next()).type != TokenType.EOF) {
            if (t.type != TokenType.ERROR) {
                System.out.println(t);
            } else {
                System.out.println("LEXERROR '" + t.lexeme + "' @ " + t.line + ":" + t.column);
            }
        }
        if (!lx.errores.isEmpty()) {
            System.out.println("\nResumen de errores léxicos:");
            for (Lexer.LexError e : lx.errores) {
                System.out.println("- '" + e.lexema + "' @ " + e.line + ":" + e.col);
            }
        }
    }
}
