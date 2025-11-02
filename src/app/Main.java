package app;

import java.nio.file.*;
import java.util.*;

import lexer.*;
import app.report.HtmlReport;
import app.report.OperacionResultado;
import app.report.ArbolGrafico;

/**
 * Proyecto 2 – Analizador de Operaciones Aritméticas
 * Fase 2: Lexer + Parser + AST + Exportación HTML + Árboles.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        // === 1. Leer entrada ===
        String path = args.length > 0 ? args[0] : "resources/entrada.txt";
        String src = Lexer.readFile(path);
        Lexer lx = new Lexer(src);

        List<String> outTokens = new ArrayList<>();
        List<String> outErrors = new ArrayList<>();
        List<Token> tokensParaParser = new ArrayList<>();

        // === 2. Ejecutar Lexer ===
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

        // también registra errores acumulados en el Lexer
        for (Lexer.LexError e : lx.errores) {
            String s = "LEXERROR '" + e.lexema + "' @ " + e.line + ":" + e.col;
            if (!outErrors.contains(s)) outErrors.add(s);
        }

        // === 3. Guardar tokens y errores ===
        Files.createDirectories(Path.of("out"));
        Files.write(Path.of("out", "tokens.txt"), outTokens);
        Files.write(Path.of("out", "errores.txt"), outErrors);

        // === 4. Analizar y evaluar (Parser + AST) ===
        String reporte = Analizar.ejecutar(tokensParaParser);
        Files.writeString(Path.of("out", "resultados.txt"), reporte);

        System.out.println("\n=== RESULTADOS ===");
        System.out.println(reporte);

        // === 5. Generar HTML y Árboles ===
        try {
            // Resultados HTML
            List<OperacionResultado> resultados = Analizar.analizar(tokensParaParser);
            HtmlReport.generarResultados(resultados, Path.of("out", "Resultados.html"));

            // Errores HTML
            HtmlReport.generarErrores(outErrors, Path.of("out", "ERRORES_Grupo1.html"), "Grupo1");

            // Árboles (.dot y .png)
            var arboles = Analizar.parsearArboles(tokensParaParser);
            for (int i = 0; i < arboles.size(); i++) {
                Path dot = Path.of("out", "arbol_" + (i + 1) + ".dot");
                Path png = Path.of("out", "arbol_" + (i + 1) + ".png");
                ArbolGrafico.generarDot(arboles.get(i), dot);
                boolean okPng = ArbolGrafico.dotAPng(dot, png);
                if (!okPng) {
                    System.err.println("Aviso: no se pudo generar PNG (¿falta Graphviz 'dot' en PATH?).");
                }
            }

            System.out.println("\nOK -> Generado out/Resultados.html, out/ERRORES_Grupo1.html y arbol_#.dot/.png");

            // (Opcional) abrir en navegador:
            try {
                java.awt.Desktop.getDesktop().browse(Path.of("out", "Resultados.html").toUri());
            } catch (Exception ex) {
                System.err.println("No se pudo abrir el navegador automáticamente.");
            }

        } catch (Exception ex) {
            System.err.println("No se pudo generar HTML/árboles: " + ex.getMessage());
        }
    }
}
