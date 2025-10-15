import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Main {
    public static void main(String[] args) {

        // Ventana principal
        JFrame principal = new JFrame("Editor de texto con estilos");
        principal.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        principal.setLocationRelativeTo(null);
        principal.setSize(600, 400);
        principal.setLayout(new BorderLayout());


        JPanel area = new JPanel(new BorderLayout());
        // Área de texto con soporte para estilos
        JTextPane textPane = new JTextPane();
        textPane.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane scroll = new JScrollPane(textPane,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        area.add(scroll, BorderLayout.CENTER);
        // Contador dinámico
        JLabel cont = new JLabel("Líneas: 0 | Palabras: 0");
        textPane.getDocument().addDocumentListener(new DocumentListener() {
            public void actualizar() {
                String texto = textPane.getText();
                int lineas = texto.split("\n").length;
                int palabras = texto.trim().isEmpty() ? 0 : texto.trim().split("\\s+").length;
                cont.setText("Líneas: " + lineas + " | Palabras: " + palabras);
            }
            @Override public void insertUpdate(DocumentEvent e) { actualizar(); }
            @Override public void removeUpdate(DocumentEvent e) { actualizar(); }
            @Override public void changedUpdate(DocumentEvent e) { actualizar(); }
        });
        area.add(cont, BorderLayout.NORTH);

        principal.add(area, BorderLayout.CENTER);

        // Panel de botones
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnMayus = crearBoton("Mayus");
        panelBoton.add(btnMayus);
        JButton btnMinus = crearBoton("Minus");
        panelBoton.add(btnMinus);
        JButton btnNegrita = crearBoton("Bold");
        btnNegrita.setFont(new Font("Arial", Font.BOLD, 15));
        panelBoton.add(btnNegrita);
        JButton btnCursiva = crearBoton("Italic");
        btnCursiva.setFont(new Font("Arial", Font.ITALIC, 15));
        panelBoton.add(btnCursiva);
        JButton btnNormal = crearBoton("Normal");
        panelBoton.add(btnNormal);
        JButton btnBuscar = crearBoton("Buscar");
        panelBoton.add(btnBuscar);

        principal.add(panelBoton, BorderLayout.SOUTH);

        // Acciones de transformación de texto
        btnMayus.addActionListener(e -> transformarSeleccion(textPane, true));
        btnMinus.addActionListener(e -> transformarSeleccion(textPane, false));

        textPane.getInputMap().put(KeyStroke.getKeyStroke("control M"), "mayus");
        textPane.getActionMap().put("mayus", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                transformarSeleccion(textPane, true);
            }
        });

        textPane.getInputMap().put(KeyStroke.getKeyStroke("control N"), "minus");
        textPane.getActionMap().put("minus", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                transformarSeleccion(textPane, false);
            }
        });

        // Acciones de estilo
        btnNegrita.addActionListener(e -> aplicarEstilo(textPane, Font.BOLD));
        btnCursiva.addActionListener(e -> aplicarEstilo(textPane, Font.ITALIC));
        btnNormal.addActionListener(e -> aplicarEstilo(textPane, Font.PLAIN));

        textPane.getInputMap().put(KeyStroke.getKeyStroke("control B"), "negrita");
        textPane.getActionMap().put("negrita", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                aplicarEstilo(textPane, Font.BOLD);
            }
        });

        textPane.getInputMap().put(KeyStroke.getKeyStroke("control I"), "cursiva");
        textPane.getActionMap().put("cursiva", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                aplicarEstilo(textPane, Font.ITALIC);
            }
        });

        textPane.getInputMap().put(KeyStroke.getKeyStroke("control P"), "normal");
        textPane.getActionMap().put("normal", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                aplicarEstilo(textPane, Font.PLAIN);
            }
        });

        // Buscar / Reemplazar
        btnBuscar.addActionListener(e -> {
            buscarRemplazar(principal, textPane);
        });
        textPane.getInputMap().put(KeyStroke.getKeyStroke("control F"), "buscar");
        textPane.getActionMap().put("buscar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buscarRemplazar(principal, textPane);
            }
        });

        UndoManager undoManager = new UndoManager();
        textPane.getDocument().addUndoableEditListener(undoManager);

        // Ctrl + Z
        textPane.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "deshacer");
        textPane.getActionMap().put("deshacer", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (undoManager.canUndo()) undoManager.undo();
            }
        });

        // Ctrl + C
        textPane.getInputMap().put(KeyStroke.getKeyStroke("control C"), "copiar");
        textPane.getActionMap().put("copiar", new DefaultEditorKit.CopyAction());

        // Ctrl + V
        textPane.getInputMap().put(KeyStroke.getKeyStroke("control V"), "pegar");
        textPane.getActionMap().put("pegar", new DefaultEditorKit.PasteAction());

        // Ctrl + X
        textPane.getInputMap().put(KeyStroke.getKeyStroke("control X"), "cortar");
        textPane.getActionMap().put("cortar", new DefaultEditorKit.CutAction());

        //Menu contextual por click izquierdo para cortar, copiar y pegar
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

        principal.setVisible(true);
    }

    // ---------- FUNCIONES REUTILIZABLES ------------

    public static JButton crearBoton(String nombre) {
        JButton btn = new JButton(nombre);
        btn.setForeground(Color.WHITE);
        btn.setBackground(Color.BLACK);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        return btn;
    }

    public static void transformarSeleccion(JTextPane textPane, boolean aMayusculas) {
        String seleccionado = textPane.getSelectedText();
        if (seleccionado != null && !seleccionado.isEmpty()) {
            int start = textPane.getSelectionStart();
            int end = textPane.getSelectionEnd();
            String reemplazo = aMayusculas ? seleccionado.toUpperCase() : seleccionado.toLowerCase();
            textPane.replaceSelection(reemplazo);
            textPane.select(start, start + reemplazo.length());
        } else {
            String texto = textPane.getText();
            textPane.setText(aMayusculas ? texto.toUpperCase() : texto.toLowerCase());
        }
    }

    public static void aplicarEstilo(JTextPane textPane, int estilo) {
        int start = textPane.getSelectionStart();
        int end = textPane.getSelectionEnd();
        StyledDocument doc = textPane.getStyledDocument();
        MutableAttributeSet attr = new SimpleAttributeSet();

        // Configurar atributos según el estilo
        if (estilo == Font.BOLD) {
            StyleConstants.setBold(attr, true);
        } else if (estilo == Font.ITALIC) {
            StyleConstants.setItalic(attr, true);
        } else if (estilo == Font.PLAIN) {
            StyleConstants.setBold(attr, false);
            StyleConstants.setItalic(attr, false);
        }

        // Si no hay selección, aplicar a todo el texto
        if (start == end) {
            start = 0;
            end = doc.getLength();
        }

        doc.setCharacterAttributes(start, end - start, attr, false);
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
            String buscar = JOptionPane.showInputDialog(frame, "Palabra que buscar: ");
            if (buscar != null && !buscar.isEmpty()) {
                String text = textPane.getText();
                if (text.contains(buscar)) {
                    JOptionPane.showMessageDialog(frame, "Palabra encontrada");
                } else {
                    JOptionPane.showMessageDialog(frame, "Palabra no encontrada");
                }
            }
        } else if (seleccion == 1) {
            String buscar = JOptionPane.showInputDialog(frame, "Palabra a buscar: ");
            String reemplazar = JOptionPane.showInputDialog(frame, "Reemplazar por: ");
            if (buscar != null && reemplazar != null) {
                String text = textPane.getText();
                text = text.replace(buscar, reemplazar);
                textPane.setText(text);
            }
        }
    }
}
