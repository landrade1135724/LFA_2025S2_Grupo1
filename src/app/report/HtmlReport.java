package app.report;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class HtmlReport {

    private HtmlReport() {
    }

    private static String css() {
        return """
        <style>
        :root { --bg:#0f172a; --card:#111827; --txt:#e5e7eb; --muted:#94a3b8; --ok:#22c55e; --err:#ef4444; --accent:#38bdf8; }
        body { font-family: Segoe UI, Roboto, Arial, sans-serif; background: var(--bg); color: var(--txt); margin:32px; }
        h1 { font-size: 20px; margin: 0 0 6px 0; color: var(--accent); }
        .sub { color: var(--muted); margin-bottom: 18px; }
        .card { background: var(--card); border-radius: 14px; padding: 18px; box-shadow: 0 8px 30px rgba(0,0,0,.35); }
        table { width: 100%; border-collapse: collapse; margin-top:8px; }
        th, td { text-align: left; padding: 10px 12px; border-bottom: 1px solid #1f2937; font-size: 14px; }
        th { color: var(--muted); font-weight: 600; }
        tr:hover td { background: #0b1220; }
        .pill { display:inline-block; padding:2px 8px; border-radius:999px; font-size:12px; }
        .ok { background: rgba(34,197,94,.15); color: var(--ok); }
        .err{ background: rgba(239,68,68,.15); color: var(--err); }
        .mono{ font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace; }
        footer { color: var(--muted); font-size: 12px; margin-top: 16px; }
        a { color: var(--accent); }
        </style>
        """;
    }

    public static void generarResultados(List<OperacionResultado> ops, Path destinoHtml) throws IOException {
        StringBuilder rows = new StringBuilder();
        for (OperacionResultado r : ops) {
            String estado = r.ok ? "<span class='pill ok'>OK</span>" : "<span class='pill err'>ERROR</span>";
            String valor = r.ok ? String.valueOf(r.valor) : "-";
            String error = r.ok ? "" : ("<div class='mono' style='color:var(--err)'>" + escape(r.error) + "</div>");
            rows.append("<tr>")
                    .append("<td>").append(r.indice).append("</td>")
                    .append("<td class='mono'>").append(escape(r.expresion)).append("</td>")
                    .append("<td>").append(estado).append("</td>")
                    .append("<td class='mono'>").append(valor).append("</td>")
                    .append("<td>").append(error).append("</td>")
                    .append("</tr>\n");
        }

        // Galería: como el HTML se guarda en /out, basta referenciar arbol_#.png relativo.
        StringBuilder gal = new StringBuilder();
        gal.append("<div class='sub'>Imágenes del árbol (si existen):</div>")
                .append("<div style='display:grid;grid-template-columns:repeat(auto-fill,minmax(180px,1fr));gap:12px;'>");
        for (int i = 1; i <= ops.size(); i++) {
            Path png = Path.of("out", "arbol_" + i + ".png");
            if (Files.exists(png)) {
                gal.append("""
              <a href="arbol_%d.png" target="_blank" style="text-decoration:none;">
                <div class="card" style="padding:8px;">
                  <div class="sub" style="margin:0 0 8px 0;">arbol_%d.png</div>
                  <img src="arbol_%d.png" alt="arbol_%d" style="width:100%%;height:140px;object-fit:contain;background:#0b1220;border-radius:10px;">
                </div>
              </a>
            """.formatted(i, i, i, i));
            }
        }
        gal.append("</div>");

        String html = """
    <!doctype html><html lang="es"><meta charset="utf-8"><title>Resultados</title>
    %s
    <body>
      <div class="card">
        <h1>Resultados de Operaciones</h1>
        <div class="sub">Generado: %s</div>
        <table>
          <thead><tr>
            <th>#</th><th>Expresión</th><th>Estado</th><th>Valor</th><th>Detalle</th>
          </tr></thead>
          <tbody>
    %s
          </tbody>
        </table>
        %s
      </div>
      <footer>Proyecto 2 · Analizador de Operaciones Aritméticas</footer>
    </body></html>
    """.formatted(css(), now(), rows.toString(), gal.toString());

        Files.createDirectories(destinoHtml.getParent());
        Files.writeString(destinoHtml, html, StandardCharsets.UTF_8);
    }

    public static void generarErrores(List<String> erroresLexicos, Path destinoHtml, String equipoId) throws IOException {
        StringBuilder rows = new StringBuilder();
        int i = 1;
        for (String s : erroresLexicos) {
            rows.append("<tr>")
                    .append("<td>").append(i++).append("</td>")
                    .append("<td class='mono'>").append(escape(s)).append("</td>")
                    .append("</tr>\n");
        }

        String html = """
    <!doctype html><html lang="es"><meta charset="utf-8"><title>ERRORES_%s</title>
    %s
    <body>
      <div class="card">
        <h1>Errores Léxicos</h1>
        <div class="sub">Generado: %s · Equipo: %s</div>
        <table>
          <thead><tr><th>#</th><th>Detalle</th></tr></thead>
          <tbody>
    %s
          </tbody>
        </table>
      </div>
      <footer>Proyecto 2 · Analizador de Operaciones Aritméticas</footer>
    </body></html>
    """.formatted(escape(equipoId), css(), now(), escape(equipoId), rows.toString());

        Files.createDirectories(destinoHtml.getParent());
        Files.writeString(destinoHtml, html, StandardCharsets.UTF_8);
    }

    private static String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private static String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
