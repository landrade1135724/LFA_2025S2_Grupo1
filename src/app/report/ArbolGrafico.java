package app.report;

import ast.Node;
import ast.Op;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/**
 * Genera .dot y (si hay Graphviz) .png para un AST.
 */
public final class ArbolGrafico {

    private ArbolGrafico() {
    }

    public static void generarDot(Node raiz, Path destinoDot) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph G {\n")
                .append("  rankdir=TB;\n")
                .append("  node [shape=box, style=rounded, fontname=\"Segoe UI\", fontsize=11];\n");

        // recorrido DFS con ids incrementales
        int[] id = {1};
        escribirNodo(raiz, -1, sb, id);

        sb.append("}\n");
        Files.createDirectories(destinoDot.getParent());
        Files.writeString(destinoDot, sb.toString(), StandardCharsets.UTF_8);
    }

    public static boolean dotAPng(java.nio.file.Path archivoDot, java.nio.file.Path destinoPng) {
        try {
            String dotExe = findDotExecutable(); // ruta completa o "dot"
            // 1) Intento PNG
            int exit = runDot(dotExe, "-Tpng", archivoDot, destinoPng);
            if (exit == 0 && java.nio.file.Files.exists(destinoPng)) {
                return true;
            }

            // 2) Si falló, intentemos SVG (dejas el .svg por si el evaluador lo prefiere)
            java.nio.file.Path destinoSvg = java.nio.file.Path.of(
                    destinoPng.toString().replaceAll("\\.png$", ".svg")
            );
            exit = runDot(dotExe, "-Tsvg", archivoDot, destinoSvg);
            if (exit == 0 && java.nio.file.Files.exists(destinoSvg)) {
                return true;
            }

            return false;
        } catch (Exception e) {
            try {
                java.nio.file.Files.writeString(
                        java.nio.file.Path.of("out", "graphviz_error.txt"),
                        "[JAVA] Excepción al invocar dot: " + e + System.lineSeparator(),
                        java.nio.charset.StandardCharsets.UTF_8,
                        java.nio.file.StandardOpenOption.CREATE,
                        java.nio.file.StandardOpenOption.APPEND
                );
            } catch (Exception ignore) {
            }
            return false;
        }
    }

// --- Helpers ---
    private static int runDot(String dotExe, String formatFlag,
            java.nio.file.Path srcDot, java.nio.file.Path outFile) throws Exception {
        java.nio.file.Files.createDirectories(outFile.getParent());
        Process p = new ProcessBuilder(
                dotExe, formatFlag,
                srcDot.toAbsolutePath().toString(),
                "-o", outFile.toAbsolutePath().toString()
        )
                .redirectErrorStream(true) // mezcla stderr en stdout
                .start();

        // Capturar salida para diagnóstico
        byte[] outBytes = p.getInputStream().readAllBytes();
        int exit = p.waitFor();

        // Log detallado
        String log = new String(outBytes, java.nio.charset.StandardCharsets.UTF_8);
        java.nio.file.Files.writeString(
                java.nio.file.Path.of("out", "graphviz_error.txt"),
                "[DOT] exe=" + dotExe + " exit=" + exit + " format=" + formatFlag
                + "\nDOT=" + srcDot.toAbsolutePath()
                + "\nOUT=" + outFile.toAbsolutePath()
                + "\n--- salida dot ---\n" + log + "\n-------------------\n",
                java.nio.charset.StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.CREATE,
                java.nio.file.StandardOpenOption.APPEND
        );
        return exit;
    }

    /**
     * Intenta localizar dot.exe de forma robusta en Windows; si no, devuelve
     * "dot".
     */
    private static String findDotExecutable() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            // 1) Usa 'where dot'
            try {
                Process pw = new ProcessBuilder("where", "dot").redirectErrorStream(true).start();
                String found = new String(pw.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).trim();
                pw.waitFor();
                if (!found.isEmpty()) {
                    // Primera línea es la ruta
                    String first = found.split("\\R")[0].trim();
                    if (new java.io.File(first).exists()) {
                        return first;
                    }
                }
            } catch (Exception ignore) {
            }

            // 2) Rutas comunes
            String[] comunes = {
                "C:\\Program Files\\Graphviz\\bin\\dot.exe",
                "C:\\Program Files (x86)\\Graphviz\\bin\\dot.exe"
            };
            for (String p : comunes) {
                if (new java.io.File(p).exists()) {
                    return p;
                }
            }
            // 3) Último recurso: nombre corto (si PATH lo tiene bien configurado)
            return "dot";
        } else {
            // Linux/Mac
            return "dot";
        }
    }

    private static int escribirNodo(Node n, int padreId, StringBuilder sb, int[] id) {
        int myId = id[0]++;
        String etiqueta;
        if (n instanceof Op op) {
            etiqueta = op.getNombre();
        } else {
            etiqueta = n.render(); // para Num u otros
        }
        sb.append("  n").append(myId).append(" [label=\"").append(esc(etiqueta)).append("\"];\n");
        if (padreId >= 0) {
            sb.append("  n").append(padreId).append(" -> n").append(myId).append(";\n");
        }
        if (n instanceof Op op) {
            for (Node h : op.getHijos()) {
                escribirNodo(h, myId, sb, id);
            }
        }
        return myId;
    }

    private static String esc(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
