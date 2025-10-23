package lexer;

public enum TokenType {
    // Palabras reservadas
    SUMA, RESTA, MULTIPLICACION, DIVISION, POTENCIA, RAIZ, INVERSO, MOD,
    // Símbolos
    LPAREN, RPAREN, COMMA,
    // Literales
    NUMBER,
    // Utilitarios
    WS, // (no se devuelve al usuario)
    ERROR, EOF
}
