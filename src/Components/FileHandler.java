package Components;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;

public class FileHandler {

    //ESTILOS PREDEFINIDOS
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

    // --- GUARDAR ARCHIVO ---
    public void guardarArchivo(JFrame frame, JTextPane textPane, ProgressLabel progressLabel) {
        FileDialog fd = new FileDialog(frame, "Guardar archivo", FileDialog.SAVE);
        fd.setFile("*.txt");
        fd.setVisible(true);

        String file = fd.getFile();
        String dir = fd.getDirectory();
        if (file == null) return;

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
                progressLabel.updateProgress("Guardando...", chunks.get(chunks.size() - 1));
            }

            @Override
            protected void done() {
                try {
                    get();
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
        if (file == null) return;

        File archivo = new File(dir, file);

        progressLabel.startTask("Cargando archivo...");
        progressLabel.setVisible(true);

        SwingWorker<DefaultStyledDocument, Integer> worker = new SwingWorker<>() {

            @Override
            protected DefaultStyledDocument doInBackground() throws Exception {
                long total = archivo.length();
                long leido = 0;

                DefaultStyledDocument docMemoria = new DefaultStyledDocument();

                try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
                    String linea;
                    while ((linea = br.readLine()) != null) {
                        parseLineToDoc(linea, docMemoria);
                        leido += linea.getBytes(StandardCharsets.UTF_8).length + 1;
                        int progreso = (int) ((leido * 100) / total);
                        publish(progreso);
                    }
                }
                return docMemoria;
            }

            @Override
            protected void process(List<Integer> chunks) {
                int progreso = chunks.get(chunks.size() - 1);
                progressLabel.updateProgress("Cargando...", progreso);
            }

            @Override
            protected void done() {
                try {
                    DefaultStyledDocument nuevoDoc = get();
                    textPane.setStyledDocument(nuevoDoc);
                    progressLabel.finishTask("Carga completada");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    progressLabel.showError("Error al leer");
                    JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
                }
            }
        };

        worker.execute();
    }

    private void parseLineToDoc(String linea, StyledDocument doc) {
        String[] segmentos = linea.split("(?<=\\s)|(?=\\s)");

        try {
            for (String segmento : segmentos) {
                AttributeSet estilo = STYLE_NORMAL;
                String textoLimpio = segmento;

                if (segmento.startsWith("***") && segmento.endsWith("***") && segmento.length() > 5) {
                    textoLimpio = segmento.substring(3, segmento.length() - 3);
                    estilo = STYLE_BOLD_ITALIC;
                } else if (segmento.startsWith("**") && segmento.endsWith("**") && segmento.length() > 3) {
                    textoLimpio = segmento.substring(2, segmento.length() - 2);
                    estilo = STYLE_BOLD;
                } else if (segmento.startsWith("_") && segmento.endsWith("_") && segmento.length() > 1) {
                    textoLimpio = segmento.substring(1, segmento.length() - 1);
                    estilo = STYLE_ITALIC;
                }
                doc.insertString(doc.getLength(), textoLimpio, estilo);
            }
            doc.insertString(doc.getLength(), "\n", STYLE_NORMAL);

        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
}