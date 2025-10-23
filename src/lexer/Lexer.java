package lexer;

import java.io.*;
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

    private void skipWS() {
        while (true) {
            char c = peek();
            if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
                advance();
            } else break;
        }
    }

    private static boolean isAZ(char c) { return (c >= 'A' && c <= 'Z'); }
    private static boolean isaz(char c) { return (c >= 'a' && c <= 'z'); }
    private static boolean isLetter(char c){ return isAZ(c) || isaz(c); }
    private static boolean isDigit(char c){ return c >= '0' && c <= '9'; }

    private String readWhile(java.util.function.Predicate<Character> p) {
        StringBuilder sb = new StringBuilder();
        while (p.test(peek())) sb.append(advance());
        return sb.toString();
    }

    // Lee un nombre de etiqueta: letras (may/min)
    private String readTagName() {
        return readWhile(Lexer::isLetter);
    }

    // Lee una palabra de operación (SUMA, RESTA, ...)
    private String readOpWord() {
        return readWhile(Lexer::isAZ); // Enunciado usa MAYÚSCULAS
    }

    private Token makeErrorToken(String bad, int sl, int sc) {
        errores.add(new LexError(bad, sl, sc));
        return new Token(TokenType.ERROR, bad, sl, sc);
    }

    // <...> o </...>
    private Token readTag() {
        int sl = line, sc = col;
        advance(); // consume '<'

        boolean closing = false;
        if (peek() == '/') { closing = true; advance(); }

        // Nombre de etiqueta
        String tag = readTagName(); // "Operacion", "Numero", "P", "R"
        if (tag.isEmpty()) {
            return makeErrorToken("<" + (closing ? "/" : "") + ">", sl, sc);
        }

        // Ignorar espacios dentro de la etiqueta
        skipWS();

        if ("Operacion".equals(tag) && !closing) {
            // Esperamos: = <espacios> PALABRA_MAYUS >   e.g. <Operacion= SUMA>
            if (peek() != '=') {
                return makeErrorToken("<Operacion ...> sin '='", sl, sc);
            }
            advance(); // '='
            skipWS();
            String op = readOpWord();
            if (op.isEmpty()) {
                return makeErrorToken("<Operacion= ?> falta operación", sl, sc);
            }
            skipWS();
            if (peek() != '>') {
                return makeErrorToken("Falta '>' en <Operacion= " + op + ">", sl, sc);
            }
            advance(); // '>'
            return new Token(TokenType.OPERACION_OPEN, op, sl, sc);
        } else {
            // Debe venir '>' inmediatamente (o tras espacios si cierre)
            skipWS();
            if (peek() != '>') {
                return makeErrorToken("Falta '>' en etiqueta <" + (closing?"/":"") + tag + ">", sl, sc);
            }
            advance(); // '>'

            if ("Operacion".equals(tag)) {
                return closing
                        ? new Token(TokenType.OPERACION_CLOSE, "</Operacion>", sl, sc)
                        : makeErrorToken("<Operacion> de apertura requiere '= OP'", sl, sc);
            } else if ("Numero".equals(tag)) {
                return closing
                        ? new Token(TokenType.NUMERO_CLOSE, "</Numero>", sl, sc)
                        : new Token(TokenType.NUMERO_OPEN, "<Numero>", sl, sc);
            } else if ("P".equals(tag)) {
                return closing
                        ? new Token(TokenType.P_CLOSE, "</P>", sl, sc)
                        : new Token(TokenType.P_OPEN, "<P>", sl, sc);
            } else if ("R".equals(tag)) {
                return closing
                        ? new Token(TokenType.R_CLOSE, "</R>", sl, sc)
                        : new Token(TokenType.R_OPEN, "<R>", sl, sc);
            } else {
                return makeErrorToken("Etiqueta no reconocida: " + tag, sl, sc);
            }
        }
    }

    private Token readNumber() {
        int sl = line, sc = col;
        StringBuilder sb = new StringBuilder();

        // parte entera
        sb.append(readWhile(Lexer::isDigit));

        // fracción opcional
        if (peek() == '.') {
            sb.append(advance());
            if (!isDigit(peek())) {
                // error: punto sin dígitos después
                return makeErrorToken(sb.toString(), sl, sc);
            }
            sb.append(readWhile(Lexer::isDigit));
        }

        return new Token(TokenType.NUMBER, sb.toString(), sl, sc);
    }

    public Token next() {
        skipWS();

        int startLine = line, startCol = col;
        char c = peek();
        if (c == '\0') return new Token(TokenType.EOF, "", startLine, startCol);

        if (c == '<') {
            return readTag();
        }

        if (isDigit(c)) {
            return readNumber();
        }

        // Cualquier otra cosa es error léxico (consume 1 char para no ciclar)
        String bad = String.valueOf(advance());
        return makeErrorToken(bad, startLine, startCol);
    }

    // Utilidad para leer archivo
    public static String readFile(String path) throws IOException {
        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(path))) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
