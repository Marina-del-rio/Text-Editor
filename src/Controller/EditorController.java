package Controller;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import Components.*;


public class EditorController {

    public static final FileHandler fileHandler = new FileHandler();

    public static JButton crearBoton(String nombre) {
        JButton btn = new JButton(nombre);
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.BLACK);
        btn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFont(new Font("Calibri", Font.PLAIN, 15));
        return btn;
    }

    public static JButton crearBotonEmoji(String emoji) {
        JButton btn = new JButton(emoji);
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.BLACK);
        btn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        return btn;
    }

    public static JToggleButton crearBotonToggle(String texto, int estilo) {
        JToggleButton btn = new JToggleButton(texto);
        btn.setFont(new Font("Calibri", estilo, 15));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.BLACK);
        btn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(40, 32));

        btn.addChangeListener(e -> {
            if (btn.isSelected()) {
                btn.setBackground(new Color(0, 120, 215));
                btn.setForeground(Color.BLACK);
            } else {
                btn.setBackground(Color.WHITE);
                btn.setForeground(Color.BLACK);
            }
        });

        return btn;
    }

    public static void transformarSeleccion(JTextPane textPane, boolean aMayusculas) {
        String seleccionado = textPane.getSelectedText();
        if (seleccionado != null && !seleccionado.isEmpty()) {
            int start = textPane.getSelectionStart();
            String reemplazo = aMayusculas ? seleccionado.toUpperCase() : seleccionado.toLowerCase();
            textPane.replaceSelection(reemplazo);
            textPane.select(start, start + reemplazo.length());
        } else {
            String texto = textPane.getText();
            textPane.setText(aMayusculas ? texto.toUpperCase() : texto.toLowerCase());
        }
    }

    public static void aplicarEstilo(JTextPane textPane, int estilo, boolean activar) {
        int start = textPane.getSelectionStart();
        int end = textPane.getSelectionEnd();
        StyledDocument doc = textPane.getStyledDocument();
        MutableAttributeSet attrs = new SimpleAttributeSet();

        if (estilo == Font.BOLD)
            StyleConstants.setBold(attrs, activar);
        else if (estilo == Font.ITALIC)
            StyleConstants.setItalic(attrs, activar);

        if (start == end) {

            textPane.setCharacterAttributes(attrs, false);
        } else {
            doc.setCharacterAttributes(start, end - start, attrs, false);
        }
    }

    public static DocumentListener crearContador(JTextPane textPane, JLabel cont) {
        return new DocumentListener() {
            public void actualizar() {//Calculo de lineas y palabras
                String texto = textPane.getText();
                int lineas = texto.split("\n").length;
                int palabras = texto.trim().isEmpty() ? 0 : texto.trim().split("\\s+").length;
                cont.setText("Líneas: " + lineas + " | Palabras: " + palabras);
            }
            @Override public void insertUpdate(DocumentEvent e) { actualizar(); }
            @Override public void removeUpdate(DocumentEvent e) { actualizar(); }
            @Override public void changedUpdate(DocumentEvent e) { actualizar(); }
        };
    }

    //Buscar
    public static int buscarTexto(JTextPane textPane, String buscar) {
        Highlighter highlighter = textPane.getHighlighter();
        highlighter.removeAllHighlights();

        if (buscar == null || buscar.isEmpty()) {
            return 0; // No hacer nada si la búsqueda está vacía
        }

        StyledDocument doc = textPane.getStyledDocument();
        int contador = 0;
        int primerResultado = -1; // Guardaremos la posición del primer resultado

        try {
            // Obtenemos el texto directamente del Document para asegurar que los índices coincidan
            String textoCompleto = doc.getText(0, doc.getLength());
            String textoBusquedaLower = buscar.toLowerCase();
            String textoCompletoLower = textoCompleto.toLowerCase();

            int index = 0;
            while ((index = textoCompletoLower.indexOf(textoBusquedaLower, index)) != -1) {
                int end = index + textoBusquedaLower.length();

                // Usamos los índices calculados en el texto del Document para resaltar
                highlighter.addHighlight(index, end,
                        new DefaultHighlighter.DefaultHighlightPainter(
                                new Color(173, 216, 230, 150) // azul clarito translúcido
                        ));

                if (primerResultado == -1) {
                    primerResultado = index; // Guardamos la posición del primer match
                }

                contador++;
                index = end; // Continuamos la búsqueda desde el final de la coincidencia actual
            }

            if (contador > 0) {
                // Movemos el cursor al inicio de la primera coincidencia encontrada
                textPane.setCaretPosition(primerResultado);
                textPane.requestFocus();
            }

        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        return contador;
    }
    //Reemplazar
    public static void reemplazarTexto(JTextPane textPane, String buscar, String reemplazar) {
        // Reemplazo ignorando mayúsculas/minúsculas
        String nuevoTexto = textPane.getText().replaceAll("(?i)" + java.util.regex.Pattern.quote(buscar), reemplazar);
        textPane.setText(nuevoTexto);

        // Limpiar resaltados después de reemplazar
        textPane.getHighlighter().removeAllHighlights();
    }

    //buscarRemplazar en JOption
    public static void buscarRemplazar(JFrame frame, JTextPane textPane) {
        String[] opciones = {"Solo Buscar", "Buscar y Reemplazar"};
        int seleccion = JOptionPane.showOptionDialog(frame,
                "Elige una opción:",
                "Buscar / Reemplazar",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, opciones, opciones[0]);

        if (seleccion == JOptionPane.CLOSED_OPTION) return;

        String buscar = JOptionPane.showInputDialog(frame, "Buscar: ");
        if (buscar == null || buscar.isEmpty()) return;

        int encontrados = buscarTexto(textPane, buscar);

        if (encontrados == 0) {
            JOptionPane.showMessageDialog(frame, "No se encontraron coincidencias.");
            return;
        }

        if (seleccion == 1) { // Reemplazar
            String reemplazar = JOptionPane.showInputDialog(frame, "Reemplazar por: ");
            if (reemplazar != null) {
                reemplazarTexto(textPane, buscar, reemplazar);
                JOptionPane.showMessageDialog(frame, "Reemplazo completado.");
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Se encontraron " + encontrados + " coincidencias resaltadas.");
        }

        // Limpiar resaltado al interactuar con el textPane
        Highlighter highlighter = textPane.getHighlighter();
        textPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { highlighter.removeAllHighlights(); }
        });
        textPane.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) { highlighter.removeAllHighlights(); }
            @Override
            public void keyPressed(KeyEvent e) { highlighter.removeAllHighlights(); }
        });
    }


    public static void configurarMenuContextual(JTextPane textPane) {

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem cortar = new JMenuItem("Cortar   Ctrl+X");
        JMenuItem copiar = new JMenuItem("Copiar   Ctrl+C");
        JMenuItem pegar = new JMenuItem("Pegar    Ctrl+V");

        cortar.addActionListener(new DefaultEditorKit.CutAction());
        copiar.addActionListener(new DefaultEditorKit.CopyAction());
        pegar.addActionListener(new DefaultEditorKit.PasteAction());

        popupMenu.add(cortar);
        popupMenu.add(copiar);
        popupMenu.add(pegar);

        // Mostrar el menú contextual
        textPane.setComponentPopupMenu(popupMenu);

        // Aplicar modo oscuro cuando se abre el popup
        popupMenu.addPopupMenuListener(new PopupMenuListener() {
            @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {

                boolean oscuro = esModoOscuro; // usa tu misma variable global

                Color fondo = oscuro ? new Color(45,45,45) : Color.WHITE;
                Color texto = oscuro ? Color.WHITE : Color.BLACK;
                Color borde = oscuro ? Color.GRAY : Color.LIGHT_GRAY;

                popupMenu.setBackground(fondo);
                popupMenu.setBorder(BorderFactory.createLineBorder(borde, 1));

                for (Component item : popupMenu.getComponents()) {
                    item.setBackground(fondo);
                    item.setForeground(texto);
                    if (item instanceof JMenuItem) {
                        ((JMenuItem) item).setOpaque(true);
                    }
                }
            }
            @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
            @Override public void popupMenuCanceled(PopupMenuEvent e) {}
        });
    }

    //Menu guardar
    public static void guardarArchivo(JFrame frame, JTextPane textPane, ProgressLabel progressLabel) {
        fileHandler.guardarArchivo(frame, textPane, progressLabel);
    }

    //Menu abrir
    public static void abrirArchivo(JFrame frame, JTextPane textPane, ProgressLabel progressLabel) {
        fileHandler.abrirArchivo(frame, textPane, progressLabel);
    }

    public static boolean esModoOscuro = false;

    public static void modoOscuro(JFrame frame, JTextPane textPane) {
        esModoOscuro = !esModoOscuro; // alternar modo

        // Colores principales
        Color fondoVentana = esModoOscuro ? new Color(35, 35, 35) : Color.WHITE;
        Color fondoEditor = esModoOscuro ? new Color(28, 28, 28) : Color.WHITE;
        Color texto = esModoOscuro ? Color.WHITE : Color.BLACK;
        Color botones = esModoOscuro ? new Color(50, 50, 50) : Color.WHITE;

        // Ventana
        frame.getContentPane().setBackground(fondoVentana);

        // JTextPane
        textPane.setBackground(fondoEditor);
        textPane.setForeground(texto);
        textPane.setCaretColor(texto);
        textPane.putClientProperty("defaultFont", textPane.getFont()); // evita restauraciones de color

        StyledDocument doc = textPane.getStyledDocument();
        MutableAttributeSet attr = new SimpleAttributeSet();
        StyleConstants.setForeground(attr, texto);
        doc.setCharacterAttributes(0, doc.getLength(), attr, true);

        textPane.setCharacterAttributes(attr, true);

        // Aplicar colores a todos los componentes
        for (Component comp : frame.getContentPane().getComponents()) {
            aplicarColores(comp, fondoVentana, fondoEditor, texto, botones);
        }

        // UIManager: JOptionPane / JFileChooser / Buscar
        UIManager.put("Panel.background", fondoVentana);
        UIManager.put("OptionPane.background", fondoVentana);
        UIManager.put("OptionPane.messageForeground", texto);
        UIManager.put("Button.background", botones);
        UIManager.put("Button.foreground", texto);
        UIManager.put("TextField.background", fondoEditor);
        UIManager.put("TextField.foreground", texto);
        UIManager.put("Label.foreground", texto);

        frame.repaint();
    }

    private static void aplicarColores(Component comp, Color fondoVentana, Color fondoEditor, Color texto, Color boton) {

        if (comp instanceof JPanel) {
            comp.setBackground(fondoVentana);
            for (Component c : ((JPanel) comp).getComponents()) {
                aplicarColores(c, fondoVentana, fondoEditor, texto, boton);
            }
        }

        else if (comp instanceof JButton || comp instanceof JToggleButton) {
            comp.setBackground(boton);
            comp.setForeground(texto);
            ((AbstractButton) comp).setBorderPainted(false);
            ((AbstractButton) comp).setFocusPainted(false);
        }

        else if (comp instanceof JLabel) {
            comp.setForeground(texto);
        }

        else if (comp instanceof JScrollPane) {
            comp.setBackground(fondoVentana);
            JScrollPane sp = (JScrollPane) comp;
            sp.getViewport().getView().setBackground(fondoEditor);
            sp.getViewport().getView().setForeground(texto);
            sp.setBorder(BorderFactory.createEmptyBorder());

            JScrollBar vBar = sp.getVerticalScrollBar();
            JScrollBar hBar = sp.getHorizontalScrollBar();
            if (vBar != null) {
                vBar.setBackground(fondoVentana);
                vBar.setForeground(boton);
            }
            if (hBar != null) {
                hBar.setBackground(fondoVentana);
                hBar.setForeground(boton);
            }
        }

        else if (comp instanceof JProgressBar) {
            comp.setBackground(fondoVentana);
            ((JProgressBar) comp).setForeground(boton);
            ((JProgressBar) comp).setStringPainted(true);
            ((JProgressBar) comp).setBorder(BorderFactory.createLineBorder(boton.darker(), 1));
        }
    }

}
