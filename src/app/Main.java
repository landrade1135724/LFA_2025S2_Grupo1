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
        List<String> outErrorsLex = new ArrayList<>();
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
                outErrorsLex.add(e);
            }
        }
        for (Lexer.LexError e : lx.errores) {
            String s = "LEXERROR '" + e.lexema + "' @ " + e.line + ":" + e.col;
            if (!outErrorsLex.contains(s)) outErrorsLex.add(s);
        }

        // === 3. Guardar tokens/errores léxicos ===
        Files.createDirectories(Path.of("out"));
        Files.write(Path.of("out", "tokens.txt"), outTokens);
        Files.write(Path.of("out", "errores.txt"), outErrorsLex);

        // === 4. Reporte de texto (no tolerante, solo informativo) ===
        String reporte = Analizar.ejecutar(tokensParaParser);
        Files.writeString(Path.of("out", "resultados.txt"), reporte);
        System.out.println("\n=== RESULTADOS ===\n" + reporte);

        // === 5. USAR MODO TOLERANTE: ASTs válidos + errores de parseo acumulados ===
        Analizar.ParseBatch batch = Analizar.parsearArbolesTolerante(tokensParaParser);
        List<OperacionResultado> resultados = Analizar.analizarTolerante(tokensParaParser);
        var arboles = batch.arboles;

        // === 6. Árboles .dot/.png solo para ASTs válidos ===
        for (int i = 0; i < arboles.size(); i++) {
            Path dot = Path.of("out", "arbol_" + (i + 1) + ".dot");
            Path png = Path.of("out", "arbol_" + (i + 1) + ".png");
            ArbolGrafico.generarDot(arboles.get(i), dot);
            boolean okPng = ArbolGrafico.dotAPng(dot, png);
            if (!okPng) {
                System.err.println("Aviso: no se pudo generar PNG (¿falta Graphviz 'dot' en PATH?).");
            }
        }

        // === 7. HTML bonito (usa AST) + ERRORES combinados (léxicos + sintácticos) ===
        HtmlReport.generarResultados(resultados, arboles, Path.of("out", "Resultados.html"));

        List<String> erroresTotales = new ArrayList<>(outErrorsLex); // léxicos
        erroresTotales.addAll(batch.errores);                        // sintácticos
        HtmlReport.generarErrores(erroresTotales, Path.of("out", "ERRORES_Grupo1.html"), "Grupo1");

        System.out.println("\nOK -> Generado out/Resultados.html, out/ERRORES_Grupo1.html y arbol_#.dot/.png");

        // (Opcional) abrir en navegador
        try {
            java.awt.Desktop.getDesktop().browse(Path.of("out", "Resultados.html").toUri());
        } catch (Exception ex) {
            System.err.println("No se pudo abrir el navegador automáticamente.");
        }
    }
}
