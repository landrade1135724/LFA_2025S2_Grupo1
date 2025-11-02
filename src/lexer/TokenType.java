package lexer;

public enum TokenType {
    ETIQUETACIERRE,     // "</"
    ETIQUETAAPRETURA,   // "<" 
    CIERREETIQUETA,     // ">"

    ABRIROPERACION,     // "<Operacion="
    CERRAROPERACION,    // "</Operacion>"

    ABRIRONUMERO,       // "<Numero>"
    CERRARNUMERO,       // "</Numero>"

    ABRIRO_P,           // "<P>"
    CERRAR_P,           // "</P>"

    ABRIRO_R,           // "<R>"
    CERRAR_R,           // "</R>"

    TipoOperacion,      // SUMA | RESTA | DIVISION | MULTIPLICACION | POTENCIA | RAIZ | INVERSA | MOD (+ INVERSO)

    ESPACIO,            // [\t\r\n ]+
    NUMERO,             // [0-9]+(\.[0-9]+)?

    EOF,
    ERROR               // cualquier cosa no reconocida
}
