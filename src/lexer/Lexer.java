package lexer;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private final String input;
    private int idx = 0, line = 1, col = 1;

    public static class LexError {
        public final String lexema;
        public final int line, col;
        public LexError(String lexema, int line, int col) {
            this.lexema = lexema; this.line = line; this.col = col;
        }
    }

    public final List<LexError> errores = new ArrayList<>();

    public Lexer(String input) { this.input = input; }

    private char peek() { return idx < input.length() ? input.charAt(idx) : '\0'; }
    private char advance() {
        char c = peek(); idx++;
        if (c == '\n') { line++; col = 1; } else { col++; }
        return c;
    }

    private static boolean isDigit(char c){ return c >= '0' && c <= '9'; }
    private static boolean isLetter(char c){ return c >= 'A' && c <= 'Z'; }
    private static boolean isWS(char c){ return c==' '||c=='\t'||c=='\r'||c=='\n'; }

    public Token next() {
        // Ignorar blancos (pero actualizamos línea/columna)
        while (isWS(peek())) advance();

        int startLine = line, startCol = col;
        char c = peek();
        if (c == '\0') return new Token(TokenType.EOF, "", startLine, startCol);

        // Símbolos directos
        if (c == '(') { advance(); return new Token(TokenType.LPAREN, "(", startLine, startCol); }
        if (c == ')') { advance(); return new Token(TokenType.RPAREN, ")", startLine, startCol); }
        if (c == ',') { advance(); return new Token(TokenType.COMMA,  ",", startLine, startCol); }

        // NUMBER: \d+(\.\d+)?
        if (isDigit(c)) {
            StringBuilder sb = new StringBuilder();
            while (isDigit(peek())) sb.append(advance());
            if (peek() == '.') {
                sb.append(advance());
                if (!isDigit(peek())) {
                    // error léxico: punto sin dígitos después
                    errores.add(new LexError(sb.toString(), startLine, startCol));
                    return new Token(TokenType.ERROR, sb.toString(), startLine, startCol);
                }
                while (isDigit(peek())) sb.append(advance());
            }
            return new Token(TokenType.NUMBER, sb.toString(), startLine, startCol);
        }

        // Palabras reservadas (solo mayúsculas, según enunciado)
        if (isLetter(c)) {
            StringBuilder sb = new StringBuilder();
            while (isLetter(peek())) sb.append(advance());
            String w = sb.toString();
            switch (w) {
                case "SUMA":            return new Token(TokenType.SUMA, w, startLine, startCol);
                case "RESTA":           return new Token(TokenType.RESTA, w, startLine, startCol);
                case "MULTIPLICACION":  return new Token(TokenType.MULTIPLICACION, w, startLine, startCol);
                case "DIVISION":        return new Token(TokenType.DIVISION, w, startLine, startCol);
                case "POTENCIA":        return new Token(TokenType.POTENCIA, w, startLine, startCol);
                case "RAIZ":            return new Token(TokenType.RAIZ, w, startLine, startCol);
                case "INVERSO":         return new Token(TokenType.INVERSO, w, startLine, startCol);
                case "MOD":             return new Token(TokenType.MOD, w, startLine, startCol);
                default:
                    errores.add(new LexError(w, startLine, startCol));
                    return new Token(TokenType.ERROR, w, startLine, startCol);
            }
        }

        // Cualquier otro char: error léxico (consume 1)
        String bad = String.valueOf(advance());
        errores.add(new LexError(bad, startLine, startCol));
        return new Token(TokenType.ERROR, bad, startLine, startCol);
    }

    // Utilidad para leer archivo
    public static String readFile(String path) throws IOException {
        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(path))) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
