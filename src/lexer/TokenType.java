package lexer;

public enum TokenType {
    // Etiquetas tipo XML simplificado
    OPERACION_OPEN,     // <Operacion= SUMA>  (lexeme = "SUMA"/"RESTA"/...)
    OPERACION_CLOSE,    // </Operacion>
    NUMERO_OPEN,        // <Numero>
    NUMERO_CLOSE,       // </Numero>
    P_OPEN,             // <P>
    P_CLOSE,            // </P>
    R_OPEN,             // <R>
    R_CLOSE,            // </R>

    // Literales
    NUMBER,

    // Utilitarios
    ERROR, EOF
}
