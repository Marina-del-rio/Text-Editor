import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;

public class EditorController {

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

    public static void buscarRemplazar(JFrame frame, JTextPane textPane) {
        String[] opciones = {"Solo Buscar", "Buscar y Reemplazar"};
        int seleccion = JOptionPane.showOptionDialog(frame,
                "Elige una opción:",
                "Buscar / Reemplazar",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, opciones, opciones[0]);

        if (seleccion == 0) {
            String buscar = JOptionPane.showInputDialog(frame, "Buscar: ");
            if (buscar != null && !buscar.isEmpty()) {
                String text = textPane.getText();
                if (text.contains(buscar)) {
                    JOptionPane.showMessageDialog(frame, "Palabra encontrada");
                } else {
                    JOptionPane.showMessageDialog(frame, "Palabra no encontrada");
                }
            }
        } else if (seleccion == 1) {
            String buscar = JOptionPane.showInputDialog(frame, "Buscar: ");
            String reemplazar = JOptionPane.showInputDialog(frame, "Reemplazar por: ");
            if (buscar != null && reemplazar != null) {
                String text = textPane.getText();
                text = text.replace(buscar, reemplazar);
                textPane.setText(text);
            }
        }
    }

    public static void configurarMenuContextual(JTextPane textPane) {
        //Menu contextual por click derecho para cortar, copiar y pegar
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem cortar = new JMenuItem("Cortar   ctrl+x");
        JMenuItem copiar = new JMenuItem("Copiar   ctrl+c");
        JMenuItem pegar = new JMenuItem("Pegar     ctrl+v");

        cortar.addActionListener(new DefaultEditorKit.CutAction());
        copiar.addActionListener(new DefaultEditorKit.CopyAction());
        pegar.addActionListener(new DefaultEditorKit.PasteAction());

        popupMenu.add(cortar);
        popupMenu.add(copiar);
        popupMenu.add(pegar);

        textPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }
}
