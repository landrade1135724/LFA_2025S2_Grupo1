package app;

import app.report.ArbolGrafico;
import app.report.HtmlReport;
import app.report.OperacionResultado;
import java.nio.file.*;
import java.util.*;
import lexer.*;

public class Main {
    public static void main(String[] args) throws Exception {
        // === 1. Leer entrada ===
        String path = args.length > 0 ? args[0] : "resources/entrada.txt";
        String src = Lexer.readFile(path);
        Lexer lx = new Lexer(src);

        List<String> outTokens = new ArrayList<>();
        List<String> outErrors = new ArrayList<>();
        List<Token> tokensParaParser = new ArrayList<>();

        // === 2. Lexer ===
        Token t;
        while ((t = lx.next()).type != TokenType.EOF) {
            if (t.type != TokenType.ERROR) {
                String s = t.type + "('" + t.lexeme + "')@" + t.line + ":" + t.column;
                System.out.println(s);
                outTokens.add(s);
                tokensParaParser.add(t);
            } else {
                String e = "LEXERROR '" + t.lexeme + "' @ " + t.line + ":" + t.column;
                System.out.println(e);
                outErrors.add(e);
            }
        }
        for (Lexer.LexError e : lx.errores) {
            String s = "LEXERROR '" + e.lexema + "' @ " + e.line + ":" + e.col;
            if (!outErrors.contains(s)) outErrors.add(s);
        }

        // === 3. Guardar tokens/errores ===
        Files.createDirectories(Path.of("out"));
        Files.write(Path.of("out", "tokens.txt"), outTokens);
        Files.write(Path.of("out", "errores.txt"), outErrors);

        // === 4. Parser + evaluación (texto) ===
        String reporte = Analizar.ejecutar(tokensParaParser);
        Files.writeString(Path.of("out", "resultados.txt"), reporte);

        System.out.println("\n=== RESULTADOS ===");
        System.out.println(reporte);

        // === 5. Resultados estructurados + ASTs (UNA sola declaración de 'arboles') ===
        List<OperacionResultado> resultados = Analizar.analizar(tokensParaParser);
        var arboles = Analizar.parsearArboles(tokensParaParser);

        // === 6. Árboles .dot/.png ===
        for (int i = 0; i < arboles.size(); i++) {
            Path dot = Path.of("out", "arbol_" + (i + 1) + ".dot");
            Path png = Path.of("out", "arbol_" + (i + 1) + ".png");
            ArbolGrafico.generarDot(arboles.get(i), dot);
            boolean okPng = ArbolGrafico.dotAPng(dot, png);
            if (!okPng) {
                System.err.println("Aviso: no se pudo generar PNG (¿falta Graphviz 'dot' en PATH?).");
            }
        }

        // === 7. HTML bonito (usa AST) + Errores HTML ===
        HtmlReport.generarResultados(resultados, arboles, Path.of("out", "Resultados.html"));
        HtmlReport.generarErrores(outErrors, Path.of("out", "ERRORES_Grupo1.html"), "Grupo1");

        System.out.println("\nOK -> Generado out/Resultados.html, out/ERRORES_Grupo1.html y arbol_#.dot/.png");

        // (Opcional) abrir en navegador
        try {
            java.awt.Desktop.getDesktop().browse(Path.of("out", "Resultados.html").toUri());
        } catch (Exception ex) {
            System.err.println("No se pudo abrir el navegador automáticamente.");
        }
    }
}
