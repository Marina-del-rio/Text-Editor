import javax.swing.*;
import javax.swing.text.*;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.*;

public class EditorFrame extends JFrame {
    public EditorFrame() {

        // Ventana principal
        JFrame principal = this;
        principal.setTitle("Editor de texto");
        principal.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        principal.setLocationRelativeTo(null);
        principal.setSize(700, 450);
        principal.setLayout(new BorderLayout());

        // Barra de herramientas
        JPanel barraSuperior = new JPanel(new BorderLayout());
        barraSuperior.setBackground(Color.WHITE);

        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelBoton.setBackground(Color.WHITE);

        JButton btnMayus = EditorController.crearBoton("A");
        btnMayus.setToolTipText("Mayúsculas (Ctrl+M)");
        panelBoton.add(btnMayus);
        JButton btnMinus = EditorController.crearBoton("a");
        btnMinus.setToolTipText("Minúsculas (Ctrl+N)");
        panelBoton.add(btnMinus);

        JToggleButton btnNegrita = EditorController.crearBotonToggle("B", Font.BOLD);
        btnNegrita.setToolTipText("Negrita (Ctrl+B)");
        panelBoton.add(btnNegrita);

        JToggleButton btnCursiva = EditorController.crearBotonToggle("I", Font.ITALIC);
        btnCursiva.setToolTipText("Cursiva (Ctrl+I)");
        panelBoton.add(btnCursiva);

        // botón de búsqueda
        JPanel panelDerecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        panelDerecha.setBackground(Color.WHITE);

        JButton btnBuscar = EditorController.crearBoton("?");
        btnBuscar.setToolTipText("Buscar/reemplazar (Ctrl+F)");
        panelDerecha.add(btnBuscar);

        barraSuperior.add(panelBoton, BorderLayout.WEST);
        barraSuperior.add(panelDerecha, BorderLayout.EAST);

        principal.add(barraSuperior, BorderLayout.NORTH);

        JPanel area = new JPanel(new BorderLayout());
        JTextPane textPane = new JTextPane();
        textPane.setFont(new Font("Calibri", Font.PLAIN, 15));
        textPane.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane scroll = new JScrollPane(textPane,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        area.add(scroll, BorderLayout.CENTER);

        //Contador dinámico
        JLabel cont = new JLabel("Líneas: 0 | Palabras: 0");
        cont.setFont(new Font("Calibri", Font.PLAIN, 13));
        cont.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        textPane.getDocument().addDocumentListener(EditorController.crearContador(textPane, cont));//El DocumentLstener es un oyente que se activa cada vez que el texto cambia
        area.add(cont, BorderLayout.SOUTH);

        principal.add(area, BorderLayout.CENTER);

        // Acciones de transformación de texto
        btnMayus.addActionListener(e -> EditorController.transformarSeleccion(textPane, true));
        btnMinus.addActionListener(e -> EditorController.transformarSeleccion(textPane, false));

        // Acciones de estilo
        btnNegrita.addActionListener(e ->
                EditorController.aplicarEstilo(textPane, Font.BOLD, btnNegrita.isSelected()));
        btnCursiva.addActionListener(e ->
                EditorController.aplicarEstilo(textPane, Font.ITALIC, btnCursiva.isSelected()));

        // Buscar / Reemplazar
        btnBuscar.addActionListener(e -> {
            EditorController.buscarRemplazar(principal, textPane);
        });

        UndoManager undoManager = new UndoManager();
        textPane.getDocument().addUndoableEditListener(undoManager);//El UndoManager registra los cambios de texto

        EditorActions.configurarAtajos(textPane, btnNegrita, btnCursiva, undoManager, principal);

        EditorController.configurarMenuContextual(textPane);

        principal.setVisible(true);
    }
}