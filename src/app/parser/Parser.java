package app.parser;

import ast.Node;
import ast.Num;
import ast.Op;
import ast.ops.OperacionFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Parser recursivo-descent para el "XML" simplificado del proyecto. Gramática
 * (informal):
 *
 * Programa -> Elemento* Elemento -> Operacion | Numero Operacion ->
 * <Operacion=NAME> Elemento+ </Operacion>
 * Numero -> <Numero> NUM </Numero>
 *
 * NAME in {SUMA, RESTA, MULTIPLICACION, DIVISION, POTENCIA, RAIZ, INVERSO, MOD}
 * NUM = entero o decimal (con punto)
 */
public class Parser {

    /**
     * Token mínimo esperado desde el Lexer. Adapta a tu clase real si quieres.
     */
    public static class Token {

        public final String lexeme;
        public final int line;
        public final int column;

        public Token(String lexeme, int line, int column) {
            this.lexeme = lexeme;
            this.line = line;
            this.column = column;
        }
    }

    private final List<Token> tokens;
    private int pos = 0;

    // Patrones de reconocimiento por texto
    private static final Pattern OPEN_OP = Pattern.compile("^<\\s*Operacion\\s*=\\s*([A-Za-z_]+)\\s*>$");
    private static final Pattern CLOSE_OP = Pattern.compile("^</\\s*Operacion\\s*>$");
    private static final Pattern OPEN_NUM = Pattern.compile("^<\\s*Numero\\s*>$");
    private static final Pattern CLOSE_NUM = Pattern.compile("^</\\s*Numero\\s*>$");
    private static final Pattern NUMBER = Pattern.compile("^[+-]?\\d+(?:\\.\\d+)?$");
    private static final Pattern OPEN_P = Pattern.compile("^<\\s*P\\s*>$");
    private static final Pattern CLOSE_P = Pattern.compile("^</\\s*P\\s*>$");
    private static final Pattern OPEN_R = Pattern.compile("^<\\s*R\\s*>$");
    private static final Pattern CLOSE_R = Pattern.compile("^</\\s*R\\s*>$");

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /**
     * Punto de entrada: parsea todas las operaciones del archivo.
     */
    public List<Node> parsePrograma() {
        List<Node> ops = new ArrayList<>();
        while (!eof()) {
            // Ignora tokens vacíos o de espacio si tu lexer los generó (opcional)
            if (isIgnorable(peek())) {
                next();
                continue;
            }
            ops.add(parseElemento());
        }
        return ops;
    }

    private Node parseValorSimple(Pattern open, Pattern close, String etiqueta) {
        expect(open, "Se esperaba <" + etiqueta + ">");
        String numLex = next().lexeme.trim();
        if (!NUMBER.matcher(numLex).matches()) {
            throw error("Se esperaba un número dentro de <" + etiqueta + "> pero vino '" + numLex + "'");
        }
        double val = Double.parseDouble(numLex);
        expect(close, "Se esperaba </" + etiqueta + ">");
        return new ast.Num(val);
    }

    private Node parseElemento() {
        if (eof()) {
            throw error("Se esperaba Elemento pero no hay más tokens");
        }
        String lx = peek().lexeme.trim();

        if (OPEN_NUM.matcher(lx).matches()) {
            return parseNumero();                // <Numero> n </Numero>
        }
        if (OPEN_P.matcher(lx).matches()) {
            return parseValorSimple(OPEN_P, CLOSE_P, "P"); // <P> n </P> → Num(n)
        }
        if (OPEN_R.matcher(lx).matches()) {
            return parseValorSimple(OPEN_R, CLOSE_R, "R"); // <R> n </R> → Num(n)
        }

        java.util.regex.Matcher m = OPEN_OP.matcher(lx);
        if (m.matches()) {
            String nombre = m.group(1);
            return parseOperacion(nombre);       // <Operacion=NAME> ... </Operacion>
        }
        throw error("Token inesperado como inicio de Elemento: '" + lx + "'");
    }

    private Node parseNumero() {
        expect(OPEN_NUM, "Se esperaba <Numero>");
        // Se espera un token que sea un número (mismo o siguiente token, según tu lexer).
        String numLex = next().lexeme.trim();
        if (!NUMBER.matcher(numLex).matches()) {
            throw error("Se esperaba un número dentro de <Numero> pero vino '" + numLex + "'");
        }
        double val = Double.parseDouble(numLex);
        expect(CLOSE_NUM, "Se esperaba </Numero>");
        return new Num(val);
    }

    private Node parseOperacion(String nombre) {
        // Ya vimos <Operacion=NAME> fuera, consumimos ese token:
        expect(OPEN_OP, "Se esperaba <Operacion=...>"); // el actual debe coincidir con la misma forma
        Op op = OperacionFactory.of(nombre);

        // Debe haber al menos 1 hijo; en la guía usualmente >=2 para binarias (salvo unarias)
        // Aquí permitimos Elemento+ y la operación validará aridad en evaluate().
        while (!eof() && !test(CLOSE_OP)) {
            if (isIgnorable(peek())) {
                next();
                continue;
            }
            Node child = parseElemento();
            op.add(child);
        }

        expect(CLOSE_OP, "Se esperaba </Operacion>");
        return op;
    }

    // ==== utilidades del cursor ====
    private boolean eof() {
        return pos >= tokens.size();
    }

    private Token peek() {
        return tokens.get(pos);
    }

    private Token next() {
        return tokens.get(pos++);
    }

    private boolean test(Pattern p) {
        if (eof()) {
            return false;
        }
        return p.matcher(peek().lexeme.trim()).matches();
    }

    private void expect(Pattern p, String msg) {
        if (eof()) {
            throw error(msg + " pero no hay más tokens");
        }
        String lx = peek().lexeme.trim();
        if (!p.matcher(lx).matches()) {
            throw error(msg + ". Encontrado: '" + lx + "'");
        }
        next();
    }

    private boolean isIgnorable(Token t) {
        if (t == null) {
            return true;
        }
        String lx = t.lexeme;
        // si tu lexer emite saltos de línea/espacios como tokens, ignóralos
        return lx.isBlank();
    }

    private ParseException error(String msg) {
        int l = eof() ? (tokens.isEmpty() ? 1 : tokens.get(tokens.size() - 1).line) : peek().line;
        int c = eof() ? (tokens.isEmpty() ? 1 : tokens.get(tokens.size() - 1).column) : peek().column;
        return new ParseException("[línea " + l + ", col " + c + "] " + msg);
    }
    // === NUEVO: POJO de error sintáctico

    // === POJO de error sintáctico (lo puedes dejar donde ya lo pusiste)
    public static class ParseErrorInfo {

        public final int line, col;
        public final String msg;

        public ParseErrorInfo(int line, int col, String msg) {
            this.line = line;
            this.col = col;
            this.msg = msg;
        }

        @Override
        public String toString() {
            return "[línea " + line + ", col " + col + "] " + msg;
        }
    }

    private final java.util.List<ParseErrorInfo> errores = new java.util.ArrayList<>();

    public java.util.List<ParseErrorInfo> getErrores() {
        return errores;
    }

    /**
     * Parsea tantas expresiones (Elemento) como encuentre SIN abortar todo el
     * archivo. Acumula errores y continúa (panic-mode).
     */
    /**
     * Parsea tantas expresiones (Elemento) como encuentre SIN abortar todo el
     * archivo. Acumula errores y continúa (panic-mode) más agresivo y seguro.
     */
    public java.util.List<ast.Node> parseBatchTolerante() {
        java.util.List<ast.Node> nodos = new java.util.ArrayList<>();

        while (!eof()) {
            // Saltar ignorable
            if (isIgnorable(peek())) {
                next();
                continue;
            }

            String lx = peek().lexeme.trim();

            // Caso 1: inicio válido de Operacion: <Operacion=NAME>
            java.util.regex.Matcher m = OPEN_OP.matcher(lx);
            if (m.matches()) {
                String nombre = m.group(1);
                try {
                    // IMPORTANTE: parseOperacion consume el mismo OPEN_OP que estamos viendo
                    ast.Node n = parseOperacion(nombre);
                    if (n != null) {
                        nodos.add(n);
                    }
                } catch (ParseException ex) {
                    // Registrar y recuperar
                    int ln = peekPosLine(), co = peekPosCol();
                    errores.add(new ParseErrorInfo(ln, co, ex.getMessage()));
                    sincronizarAProximoInicio();
                }
                continue;
            }

            // Caso 2: inicio válido de Numero/P/R -> son Elementos válidos independientes
            if (OPEN_NUM.matcher(lx).matches() || OPEN_P.matcher(lx).matches() || OPEN_R.matcher(lx).matches()) {
                try {
                    ast.Node n = parseElemento();
                    if (n != null) {
                        nodos.add(n);
                    }
                } catch (ParseException ex) {
                    int ln = peekPosLine(), co = peekPosCol();
                    errores.add(new ParseErrorInfo(ln, co, ex.getMessage()));
                    sincronizarAProximoInicio();
                }
                continue;
            }

            // Caso 3: cierre suelto (</Operacion> o </Numero> / </P> / </R>) en tope de documento
            if (CLOSE_OP.matcher(lx).matches() || CLOSE_NUM.matcher(lx).matches()
                    || CLOSE_P.matcher(lx).matches() || CLOSE_R.matcher(lx).matches()) {
                // Error: cierre sin apertura en este nivel
                errores.add(new ParseErrorInfo(peek().line, peek().column,
                        "Cierre inesperado en tope: '" + lx + "'"));
                next(); // consumir el cierre para no ciclar
                continue;
            }

            // Caso 4: cualquier otra basura en tope
            errores.add(new ParseErrorInfo(peek().line, peek().column,
                    "Token inesperado como inicio de Elemento: '" + lx + "'"));
            next(); // descartar y seguir
        }

        return nodos;
    }

// Posición segura para reportes cuando estamos en o cerca de EOF
    private int peekPosLine() {
        if (eof()) {
            if (tokens.isEmpty()) {
                return 1;
            }
            return tokens.get(tokens.size() - 1).line;
        }
        return peek().line;
    }

    private int peekPosCol() {
        if (eof()) {
            if (tokens.isEmpty()) {
                return 1;
            }
            return tokens.get(tokens.size() - 1).column;
        }
        return peek().column;
    }

    /**
     * Avanza hasta un posible inicio de Elemento (<Operacion=...>, <Numero>,
     * <P>
     * , <R>) o EOF.
     */
    private void sincronizarAProximoInicio() {
        while (!eof()) {
            String lx = peek().lexeme.trim();
            if (OPEN_OP.matcher(lx).matches() || OPEN_NUM.matcher(lx).matches()
                    || OPEN_P.matcher(lx).matches() || OPEN_R.matcher(lx).matches()) {
                break; // punto seguro
            }
            // En recuperación: consumir TODO, incluyendo cierres/ruido
            next();
        }
    }
}
