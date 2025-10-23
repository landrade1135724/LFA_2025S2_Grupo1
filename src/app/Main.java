package app;

import java.nio.file.*;
import java.util.*;
import lexer.*;

public class Main {
    public static void main(String[] args) throws Exception {
        String path = args.length > 0 ? args[0] : "resources/entrada.txt";
        String src = Lexer.readFile(path);
        Lexer lx = new Lexer(src);

        List<String> outTokens = new ArrayList<>();
        List<String> outErrors = new ArrayList<>();

        Token t;
        while ((t = lx.next()).type != TokenType.EOF) {
            if (t.type != TokenType.ERROR) {
                String s = t.type + "('" + t.lexeme + "')@" + t.line + ":" + t.column;
                System.out.println(s);
                outTokens.add(s);
            } else {
                String e = "LEXERROR '" + t.lexeme + "' @ " + t.line + ":" + t.column;
                System.out.println(e);
                outErrors.add(e);
            }
        }

        // también registra los errores que acumuló el lexer
        for (Lexer.LexError e : lx.errores) {
            String s = "LEXERROR '" + e.lexema + "' @ " + e.line + ":" + e.col;
            if (!outErrors.contains(s)) outErrors.add(s);
        }

        Files.createDirectories(Path.of("out"));
        Files.write(Path.of("out", "tokens.txt"), outTokens);
        Files.write(Path.of("out", "errores.txt"), outErrors);

        System.out.println("\nOK -> Generado out/tokens.txt y out/errores.txt");
    }
}
