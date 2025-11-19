package Components;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;

public class FileHandler {

    //Estilos predefinidos
    private static final SimpleAttributeSet STYLE_NORMAL = new SimpleAttributeSet();
    private static final SimpleAttributeSet STYLE_BOLD;
    private static final SimpleAttributeSet STYLE_ITALIC;
    private static final SimpleAttributeSet STYLE_BOLD_ITALIC;

    //Bloque static porque le corresponde a la clase no a cada objeto
    static {
        STYLE_BOLD = new SimpleAttributeSet();
        StyleConstants.setBold(STYLE_BOLD, true);

        STYLE_ITALIC = new SimpleAttributeSet();
        StyleConstants.setItalic(STYLE_ITALIC, true);

        STYLE_BOLD_ITALIC = new SimpleAttributeSet();
        StyleConstants.setBold(STYLE_BOLD_ITALIC, true);
        StyleConstants.setItalic(STYLE_BOLD_ITALIC, true);
    }

    // Guardar archivo
    public void guardarArchivo(JFrame frame, JTextPane textPane, ProgressLabel progressLabel) {
        FileDialog fd = new FileDialog(frame, "Guardar archivo", FileDialog.SAVE);//Para elegir/crear el archivo, alternativa FileChooser
        fd.setFile("*.txt");
        fd.setVisible(true);

        String file = fd.getFile();
        String dir = fd.getDirectory();

        if (file == null) return;//Si el usuario cancela, se regresa

        if (!file.toLowerCase().endsWith(".txt")) {
            file += ".txt";
        }

        File archivo = new File(dir, file);

        SwingWorker<Void, Integer> worker = new SwingWorker<>() {//Sirve para no bloquear el event-dispatch thread
            @Override
            //Llama a convertir estilos para obtener el texto plano con marcas
            protected Void doInBackground() throws Exception {
                String contenido = convertirEstilosAMarcas(textPane.getStyledDocument(), this::publish);
                try (FileWriter writer = new FileWriter(archivo)) {
                    writer.write(contenido);
                }
                return null;
            }

            @Override
            //ecibe publicaciones de vuelta y llama a UpdateProgress del ProgresLabel
            protected void process(List<Integer> chunks) {
                progressLabel.updateProgress("Guardando...", chunks.get(chunks.size() - 1));
            }

            @Override
            //Marca la tarea como completada, salvo en caso de error, que lo muestra en el pogressLabel
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
        worker.execute();//Empieza
    }

    private String convertirEstilosAMarcas(StyledDocument doc, Consumer<Integer> progressUpdater) throws BadLocationException {
        //Recoge el archivo como un style document
        StringBuilder contenido = new StringBuilder();
        Element root = doc.getDefaultRootElement();
        int numElems = root.getElementCount();
        int ultimoProgreso = -1;

        //Recorre cada elemento por cada parrafo para marcar su estilo
        for (int i = 0; i < numElems; i++) {
            Element parrafo = root.getElement(i);

            for (int j = 0; j < parrafo.getElementCount(); j++) {
                Element elem = parrafo.getElement(j);
                AttributeSet attrs = elem.getAttributes();

                String text = doc.getText(elem.getStartOffset(),
                        elem.getEndOffset() - elem.getStartOffset());

                boolean bold = StyleConstants.isBold(attrs);
                boolean italic = StyleConstants.isItalic(attrs);

                if (bold && italic) {
                    contenido.append("***").append(text).append("***");
                } else if (bold) {
                    contenido.append("**").append(text).append("**");
                } else if (italic) {
                    contenido.append("_").append(text).append("_");
                } else {
                    contenido.append(text);
                }
            }
            contenido.append("\n");//va construyendo el stringbuilder

            //Calcula el porcentaje de progreso para el swingworker
            int progreso = (int) (((i + 1.0) / numElems) * 100);
            if (progreso > ultimoProgreso) {
                ultimoProgreso = progreso;
                progressUpdater.accept(progreso);
            }
        }
        return contenido.toString();
    }


    // Abrir archivo
    public void abrirArchivo(JFrame frame, JTextPane textPane, ProgressLabel progressLabel) {
        FileDialog fd = new FileDialog(frame, "Abrir archivo", FileDialog.LOAD);//Para seleccionar archivo
        fd.setFile("*.txt");
        fd.setVisible(true);

        String file = fd.getFile();
        String dir = fd.getDirectory();

        if (file == null) return;//Si se cancela se vuelve

        File archivo = new File(dir, file);

        progressLabel.startTask("Cargando archivo...");
        progressLabel.setVisible(true);

        SwingWorker<DefaultStyledDocument, Integer> worker = new SwingWorker<>() {//Abre swingworker

            @Override
            //Crea un DefaultStyledDocument en memoria y lee el archivo con un bufferedReader para aplicarle a cada linea el parseo
            protected DefaultStyledDocument doInBackground() throws Exception {
                long total = archivo.length();
                long leido = 0;

                DefaultStyledDocument docMemoria = new DefaultStyledDocument();

                try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
                    String linea;
                    while ((linea = br.readLine()) != null) {
                        parseLineaADoc(linea, docMemoria);
                        leido += linea.getBytes(StandardCharsets.UTF_8).length + 1;
                        int progreso = (int) ((leido * 100) / total);
                        publish(progreso);
                    }
                }
                return docMemoria;
            }

            @Override
            //Actualiza el progreso
            protected void process(List<Integer> chunks) {
                int progreso = chunks.get(chunks.size() - 1);
                progressLabel.updateProgress("Cargando...", progreso);
            }

            @Override
            //Asigna el resultado al TextPane
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

    //Divide la linea que recibe en segmentos para que los espacios queden como tokens(se usa lookbehind y ahead para no perderlos)
    //Y determina si cada segmento empieza y/o termina con las marcas asignadas a cada estilo
    private void parseLineaADoc(String linea, StyledDocument doc) {
        int i = 0;
        boolean bold = false;
        boolean italic = false;

        while (i < linea.length()) {
            // Detecta apertura/cierre de bold+italic
            if (linea.startsWith("***", i)) {
                bold = !bold;
                italic = !italic;
                i += 3;
                continue;
            }
            // Detecta apertura/cierre de bold
            if (linea.startsWith("**", i)) {
                bold = !bold;
                i += 2;
                continue;
            }
            // Detecta apertura/cierre de italic
            if (linea.startsWith("_", i)) {
                italic = !italic;
                i += 1;
                continue;
            }

            int next = linea.length();
            int nextBoldItalic = linea.indexOf("***", i);
            int nextBold = linea.indexOf("**", i);
            int nextItalic = linea.indexOf("_", i);

            if (nextBoldItalic != -1 && nextBoldItalic < next) next = nextBoldItalic;
            if (nextBold != -1 && nextBold < next) next = nextBold;
            if (nextItalic != -1 && nextItalic < next) next = nextItalic;

            String texto = linea.substring(i, next);

            AttributeSet estilo = STYLE_NORMAL;
            if (bold && italic) estilo = STYLE_BOLD_ITALIC;
            else if (bold) estilo = STYLE_BOLD;
            else if (italic) estilo = STYLE_ITALIC;

            try {
                doc.insertString(doc.getLength(), texto, estilo);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }

            i = next;
        }

        try {
            doc.insertString(doc.getLength(), "\n", STYLE_NORMAL);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
}