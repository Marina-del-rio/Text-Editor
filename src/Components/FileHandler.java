package Components;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.function.Consumer;

/**
 * Gestiona las operaciones de guardado y apertura de archivos para un JTextPane,
 * manejando los estilos de texto y realizando las operaciones en segundo plano
 * para no congelar la interfaz de usuario.
 */
public class FileHandler {

    // --- ESTILOS PREDEFINIDOS ---
    private static final SimpleAttributeSet STYLE_NORMAL = new SimpleAttributeSet();
    private static final SimpleAttributeSet STYLE_BOLD;
    private static final SimpleAttributeSet STYLE_ITALIC;
    private static final SimpleAttributeSet STYLE_BOLD_ITALIC;

    static {
        STYLE_BOLD = new SimpleAttributeSet();
        StyleConstants.setBold(STYLE_BOLD, true);

        STYLE_ITALIC = new SimpleAttributeSet();
        StyleConstants.setItalic(STYLE_ITALIC, true);

        STYLE_BOLD_ITALIC = new SimpleAttributeSet();
        StyleConstants.setBold(STYLE_BOLD_ITALIC, true);
        StyleConstants.setItalic(STYLE_BOLD_ITALIC, true);
    }

    private static class StyledTextChunk {
        final String text;
        final AttributeSet style;
        StyledTextChunk(String text, AttributeSet style) { this.text = text; this.style = style; }
    }

    //GUARDAR ARCHIVO
    public void guardarArchivo(JFrame frame, JTextPane textPane, ProgressLabel progressLabel) {
        FileDialog fd = new FileDialog(frame, "Guardar archivo", FileDialog.SAVE);
        fd.setFile("*.txt");
        fd.setVisible(true);

        String file = fd.getFile();
        String dir = fd.getDirectory();
        if (file == null) return; // Usuario canceló

        if (!file.toLowerCase().endsWith(".txt")) {
            file += ".txt";
        }
        File archivo = new File(dir, file);

        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                String contenido = convertirEstilosAMarcas(textPane.getStyledDocument(), this::publish);
                try (FileWriter writer = new FileWriter(archivo)) {
                    writer.write(contenido);
                }
                return null;
            }

            @Override
            protected void process(List<Integer> chunks) {
                progressLabel.updateProgress("Guardando", chunks.get(chunks.size() - 1));
            }

            @Override
            protected void done() {
                try {
                    get(); // Verifica si hubo errores
                    progressLabel.finishTask("Guardado completado");
                } catch (Exception e) {
                    e.printStackTrace();
                    progressLabel.showError("Error al guardar");
                }
            }
        };

        progressLabel.startTask("Guardando archivo...");
        worker.execute();
    }

    private String convertirEstilosAMarcas(StyledDocument doc, Consumer<Integer> progressUpdater) throws BadLocationException {
        StringBuilder contenido = new StringBuilder();
        Element root = doc.getDefaultRootElement();
        int numElems = root.getElementCount();
        int ultimoProgresoPublicado = -1;

        for (int i = 0; i < numElems; i++) {
            Element parrafo = root.getElement(i);
            for (int j = 0; j < parrafo.getElementCount(); j++) {
                Element elem = parrafo.getElement(j);
                AttributeSet attrs = elem.getAttributes();
                String text = doc.getText(elem.getStartOffset(), elem.getEndOffset() - elem.getStartOffset());

                boolean bold = StyleConstants.isBold(attrs);
                boolean italic = StyleConstants.isItalic(attrs);

                if (bold && italic) contenido.append("***").append(text.trim()).append("*** ");
                else if (bold) contenido.append("**").append(text.trim()).append("** ");
                else if (italic) contenido.append("_").append(text.trim()).append("_ ");
                else contenido.append(text);
            }
            contenido.append("\n");

            int progresoActual = (int) (((i + 1.0) / numElems) * 100);
            if (progresoActual > ultimoProgresoPublicado) {
                ultimoProgresoPublicado = progresoActual;
                progressUpdater.accept(progresoActual);
            }
        }
        return contenido.toString();
    }



    // ABRIR ARCHIVO
    public void abrirArchivo(JFrame frame, JTextPane textPane, ProgressLabel progressLabel) {

        FileDialog fd = new FileDialog(frame, "Abrir archivo", FileDialog.LOAD);
        fd.setFile("*.txt");
        fd.setVisible(true);

        String file = fd.getFile();
        String dir = fd.getDirectory();
        if (file == null) return; // cancelado

        File archivo = new File(dir, file);

        progressLabel.startTask("Cargando archivo...");
        progressLabel.setVisible(true);

        SwingWorker<Void, Integer> worker = new SwingWorker<>() {

            @Override
            protected Void doInBackground() throws Exception {

                long total = archivo.length();
                long leido = 0;

                StyledDocument doc = textPane.getStyledDocument();
                doc.remove(0, doc.getLength()); // limpiar antes de insertar

                try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {

                    String linea;
                    while ((linea = br.readLine()) != null) {

                        // Convertir la línea en fragmentos con estilo
                        parseLineAndPublish(linea, chunk -> {
                            try {
                                doc.insertString(doc.getLength(), chunk.text, chunk.style);
                            } catch (BadLocationException e) {
                                e.printStackTrace();
                            }
                        });

                        // Progreso en tiempo real
                        leido += linea.getBytes(StandardCharsets.UTF_8).length + 1;
                        int progreso = (int)((leido * 100) / total);
                        publish(progreso);
                    }
                }
                return null;
            }

            @Override
            protected void process(List<Integer> chunks) {
                int progreso = chunks.get(chunks.size() - 1);
                progressLabel.updateProgress("Cargando... " + progreso + "%", progreso);
            }

            @Override
            protected void done() {
                try {
                    get();
                    progressLabel.finishTask("Archivo cargado");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    progressLabel.showError("Error al cargar archivo");
                    JOptionPane.showMessageDialog(frame, "Error al abrir el archivo:\n" + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }



    private void parseLineAndPublish(String linea, Consumer<StyledTextChunk> publisher) {
        String[] segmentos = linea.split("(?<=\\s)|(?=\\s)");
        for (String segmento : segmentos) {
            if (segmento.startsWith("***") && segmento.endsWith("***") && segmento.length() > 5) {
                publisher.accept(new StyledTextChunk(segmento.substring(3, segmento.length() - 3), STYLE_BOLD_ITALIC));
            } else if (segmento.startsWith("**") && segmento.endsWith("**") && segmento.length() > 3) {
                publisher.accept(new StyledTextChunk(segmento.substring(2, segmento.length() - 2), STYLE_BOLD));
            } else if (segmento.startsWith("_") && segmento.endsWith("_") && segmento.length() > 1) {
                publisher.accept(new StyledTextChunk(segmento.substring(1, segmento.length() - 1), STYLE_ITALIC));
            } else {
                publisher.accept(new StyledTextChunk(segmento, STYLE_NORMAL));
            }
        }

        publisher.accept(new StyledTextChunk("\n", STYLE_NORMAL));
    }
}