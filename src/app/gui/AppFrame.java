package app.gui;

import app.Analizar;
import app.report.ArbolGrafico;
import app.report.HtmlReport;
import app.report.OperacionResultado;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;

/**
 * Ventana principal con menús:
 *  Archivo  → Abrir, Guardar, Salir
 *  Acciones → Analizar, Abrir Resultados, Abrir Errores, Abrir Árbol
 *  Ayuda    → Manual de Usuario (PDF), Manual Técnico (PDF), Acerca de
 *
 * Trabaja con el pipeline existente:
 *  - Lexer → tokens/errores
 *  - Analizar → resultados y ASTs
 *  - HtmlReport → Resultados.html / Errores.html
 *  - ArbolGrafico → arbol_#.dot/.png
 */
public class AppFrame extends JFrame {

    private final JTextArea editor;
    private Path archivoActual = null;

    public AppFrame() {
        super("Proyecto 2 · Analizador de Operaciones");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);

        // Editor
        editor = new JTextArea();
        editor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        editor.setTabSize(2);
        editor.setLineWrap(false);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(new JScrollPane(editor), BorderLayout.CENTER);

        // Menú
        setJMenuBar(crearMenuBar());

        // Carga un ejemplo si no hay archivo abierto
        editor.setText("""
        <Operacion= SUMA>
            <Numero> 3.2 </Numero>
            <Numero> 7.8 </Numero>
            <Numero> 1   </Numero>
        </Operacion>
        """);
    }

    private JMenuBar crearMenuBar() {
        JMenuBar mb = new JMenuBar();

        // ===== Archivo =====
        JMenu mArchivo = new JMenu("Archivo");
        mArchivo.setMnemonic(KeyEvent.VK_A);

        JMenuItem miAbrir = new JMenuItem("Abrir...");
        miAbrir.addActionListener(e -> accionAbrir());
        JMenuItem miGuardar = new JMenuItem("Guardar");
        miGuardar.addActionListener(e -> accionGuardar(false));
        JMenuItem miGuardarComo = new JMenuItem("Guardar como...");
        miGuardarComo.addActionListener(e -> accionGuardar(true));
        JMenuItem miSalir = new JMenuItem("Salir");
        miSalir.addActionListener(e -> System.exit(0));

        mArchivo.add(miAbrir);
        mArchivo.add(miGuardar);
        mArchivo.add(miGuardarComo);
        mArchivo.addSeparator();
        mArchivo.add(miSalir);

        // ===== Acciones =====
        JMenu mAcciones = new JMenu("Acciones");
        mAcciones.setMnemonic(KeyEvent.VK_C);

        JMenuItem miAnalizar = new JMenuItem("Analizar");
        miAnalizar.setAccelerator(KeyStroke.getKeyStroke("control ENTER"));
        miAnalizar.addActionListener(e -> accionAnalizar());

        JMenuItem miAbrirRes = new JMenuItem("Abrir Resultados");
        miAbrirRes.addActionListener(e -> abrirEnNavegador(Path.of("out", "Resultados.html")));
        JMenuItem miAbrirErr = new JMenuItem("Abrir Errores");
        miAbrirErr.addActionListener(e -> abrirEnNavegador(Path.of("out", "ERRORES_Grupo1.html")));
        JMenuItem miAbrirArb = new JMenuItem("Abrir Árbol (carpeta out)");
        miAbrirArb.addActionListener(e -> abrirEnExplorador(Path.of("out")));

        mAcciones.add(miAnalizar);
        mAcciones.addSeparator();
        mAcciones.add(miAbrirRes);
        mAcciones.add(miAbrirErr);
        mAcciones.add(miAbrirArb);

        // ===== Ayuda =====
        JMenu mAyuda = new JMenu("Ayuda");
        mAyuda.setMnemonic(KeyEvent.VK_Y);

        JMenuItem miManUsuario = new JMenuItem("Manual de Usuario (PDF)");
        miManUsuario.addActionListener(e -> abrirEnNavegador(Path.of("docs", "Manual_Usuario.pdf")));
        JMenuItem miManTecnico = new JMenuItem("Manual Técnico (PDF)");
        miManTecnico.addActionListener(e -> abrirEnNavegador(Path.of("docs", "Manual_Tecnico.pdf")));
        JMenuItem miAcerca = new JMenuItem("Acerca de...");
        miAcerca.addActionListener(e -> mostrarAcerca());

        mAyuda.add(miManUsuario);
        mAyuda.add(miManTecnico);
        mAyuda.addSeparator();
        mAyuda.add(miAcerca);

        mb.add(mArchivo);
        mb.add(mAcciones);
        mb.add(mAyuda);
        return mb;
    }

    // ==================== Acciones ====================

    private void accionAbrir() {
        JFileChooser ch = new JFileChooser();
        ch.setFileFilter(new FileNameExtensionFilter("Texto (*.txt)", "txt"));
        if (ch.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            archivoActual = ch.getSelectedFile().toPath();
            try {
                editor.setText(Files.readString(archivoActual, StandardCharsets.UTF_8));
                setTitle("Proyecto 2 · " + archivoActual.getFileName());
            } catch (IOException ex) {
                mostrarError("No se pudo abrir: " + ex.getMessage());
            }
        }
    }

    private void accionGuardar(boolean como) {
        try {
            if (archivoActual == null || como) {
                JFileChooser ch = new JFileChooser();
                ch.setFileFilter(new FileNameExtensionFilter("Texto (*.txt)", "txt"));
                if (ch.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
                archivoActual = ch.getSelectedFile().toPath();
                if (!archivoActual.toString().toLowerCase().endsWith(".txt")) {
                    archivoActual = Path.of(archivoActual.toString() + ".txt");
                }
            }
            Files.writeString(archivoActual, editor.getText(), StandardCharsets.UTF_8);
            setTitle("Proyecto 2 · " + archivoActual.getFileName());
        } catch (IOException ex) {
            mostrarError("No se pudo guardar: " + ex.getMessage());
        }
    }

    private void accionAnalizar() {
    try {
        // 1) Lexer
        String src = editor.getText();
        Lexer lx = new Lexer(src);
        List<String> outTokens = new ArrayList<>();
        List<String> outErrors = new ArrayList<>();
        List<Token> tokensParaParser = new ArrayList<>();

        Token t;
        while ((t = lx.next()).type != TokenType.EOF) {
            if (t.type != TokenType.ERROR) {
                String s = t.type + "('" + t.lexeme + "')@" + t.line + ":" + t.column;
                outTokens.add(s);
                tokensParaParser.add(t);
            } else {
                String e = "LEXERROR '" + t.lexeme + "' @ " + t.line + ":" + t.column;
                outErrors.add(e);
            }
        }
        for (Lexer.LexError e : lx.errores) {
            String s = "LEXERROR '" + e.lexema + "' @ " + e.line + ":" + e.col;
            if (!outErrors.contains(s)) outErrors.add(s);
        }

        Files.createDirectories(Path.of("out"));
        Files.write(Path.of("out", "tokens.txt"), outTokens, StandardCharsets.UTF_8);
        Files.write(Path.of("out", "errores.txt"), outErrors, StandardCharsets.UTF_8);

        // 2) Parser + AST + resultados (texto)
        String reporte = Analizar.ejecutar(tokensParaParser);
        Files.writeString(Path.of("out", "resultados.txt"), reporte, StandardCharsets.UTF_8);

        // 3) Resultados evaluados (lista) y ASTs
        List<OperacionResultado> resultados = Analizar.analizar(tokensParaParser);
        var arboles = Analizar.parsearArboles(tokensParaParser);  // <-- SOLO UNA VEZ

        // 4) Árboles .dot/.png
        for (int i = 0; i < arboles.size(); i++) {
            Path dot = Path.of("out", "arbol_" + (i + 1) + ".dot");
            Path png = Path.of("out", "arbol_" + (i + 1) + ".png");
            ArbolGrafico.generarDot(arboles.get(i), dot);
            boolean okPng = ArbolGrafico.dotAPng(dot, png);
            if (!okPng) {
                System.err.println("Aviso: no se pudo generar PNG (Graphviz).");
            }
        }

        // 5) HTML bonito (usa AST para la notación infija) + errores
        HtmlReport.generarResultados(resultados, arboles, Path.of("out", "Resultados.html"));
        HtmlReport.generarErrores(outErrors, Path.of("out", "ERRORES_Grupo1.html"), "Grupo1");

        JOptionPane.showMessageDialog(this,
                "Análisis completado.\nSe generó out/Resultados.html, out/ERRORES_Grupo1.html y arbol_#.png",
                "OK", JOptionPane.INFORMATION_MESSAGE);

        abrirEnNavegador(Path.of("out", "Resultados.html"));

    } catch (Exception ex) {
        ex.printStackTrace();
        mostrarError("Fallo el análisis: " + ex.getMessage());
    }
}


    private void abrirEnNavegador(Path p) {
        try {
            if (Files.exists(p)) {
                Desktop.getDesktop().browse(p.toUri());
            } else {
                mostrarError("No existe: " + p);
            }
        } catch (Exception ex) {
            mostrarError("No se pudo abrir: " + ex.getMessage());
        }
    }

    private void abrirEnExplorador(Path dir) {
        try {
            if (Files.exists(dir)) {
                Desktop.getDesktop().open(dir.toFile());
            } else {
                mostrarError("No existe: " + dir);
            }
        } catch (Exception ex) {
            mostrarError("No se pudo abrir carpeta: " + ex.getMessage());
        }
    }

    private void mostrarAcerca() {
        JOptionPane.showMessageDialog(this,
                """
                Analizador de Operaciones · Fase 2
                URL · Ingeniería en Sistemas
                Integrantes: Luis Antonio Andrade García - 1135724
                Juan Pablo Mazariegos Sepulveda - 1140024
                
                - Lexer propio (ER/DFA)
                - Parser recursivo
                - AST + evaluación
                - Reporte HTML y árboles .dot/.png
                """,
                "Acerca de", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
