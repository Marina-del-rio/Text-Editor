package View;

import Components.*;
import Controller.EditorActions;
import Controller.EditorController;

import Nui.*;
import Nui.adapters.*;
import Nui.NuiCommand.*;

import javax.swing.*;
import javax.swing.undo.UndoManager;
import java.awt.*;

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

        // botón de búsqueda, guardar, abrir y de voz
        JPanel panelDerecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        panelDerecha.setBackground(Color.WHITE);

        JButton btnGuardar = EditorController.crearBotonEmoji("💾");
        btnGuardar.setToolTipText("Guardar archivo (Ctrl+S)");
        panelDerecha.add(btnGuardar);

        JButton btnAbrir = EditorController.crearBotonEmoji("📂");
        btnAbrir.setToolTipText("Abrir archivo (Ctrl+O)");
        panelDerecha.add(btnAbrir);

        JButton btnVoz = EditorController.crearBotonEmoji("🎙️");
        btnVoz.setToolTipText("Abrir simulador de voz");
        panelDerecha.add(btnVoz);

        JButton btnBuscar = EditorController.crearBotonEmoji("🔍");
        btnBuscar.setToolTipText("Buscar/reemplazar (Ctrl+F)");
        panelDerecha.add(btnBuscar);

        JToggleButton btnModoOsc = EditorController.crearBotonToggle("🌔", Font.PLAIN);
        btnModoOsc.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        btnModoOsc.setToolTipText("Modo Oscuro");
        panelDerecha.add(btnModoOsc);

        barraSuperior.add(panelBoton, BorderLayout.WEST);
        barraSuperior.add(panelDerecha, BorderLayout.EAST);

        principal.add(barraSuperior, BorderLayout.NORTH);

        JPanel area = new JPanel(new BorderLayout());
        JTextPane textPane = new JTextPane();
        textPane.setEditorKit(new WrapEditorKit());
        textPane.setFont(new Font("Calibri", Font.PLAIN, 15));
        textPane.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane scroll = new JScrollPane(textPane,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        area.add(scroll, BorderLayout.CENTER);
        principal.add(area, BorderLayout.CENTER);

        // Contador dinámico y progressLabel
        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.setBackground(Color.WHITE);

        JLabel cont = new JLabel("Líneas: 0 | Palabras: 0");
        cont.setFont(new Font("Calibri", Font.PLAIN, 13));
        cont.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        panelInferior.add(cont, BorderLayout.WEST);

        ProgressLabel progressLabel = new ProgressLabel("Listo");
        progressLabel.setPreferredSize(new Dimension(150, 18));
        progressLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panelInferior.add(progressLabel, BorderLayout.EAST);

        textPane.getDocument().addDocumentListener(
                EditorController.crearContador(textPane, cont)
        );

        area.add(panelInferior, BorderLayout.SOUTH);

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

        //Guardar
        btnGuardar.addActionListener(e ->
                EditorController.guardarArchivo(principal, textPane, progressLabel));

        //Abrir
        btnAbrir.addActionListener(e ->
                EditorController.abrirArchivo(principal, textPane, progressLabel));


        //ModoOscuro
        btnModoOsc.addActionListener(e -> {
            EditorController.modoOscuro(principal, textPane);
            btnModoOsc.setText(EditorController.esModoOscuro ? "🌕" : "🌔");
        });

        UndoManager undoManager = new UndoManager();
        textPane.getDocument().addUndoableEditListener(undoManager);

        EditorActions.configurarAtajos(textPane, btnNegrita, btnCursiva, undoManager, principal, progressLabel);

        EditorController.configurarMenuContextual(textPane);

        // INTEGRACIÓN SIMULADOR DE VOZ
        NuiController nc = new NuiController();

        btnVoz.addActionListener(e -> {
            JDialog dialogVoz = new JDialog(principal, "Simulador de Voz", false);
            SimulatedVoiceAdapter tuPanelDeVoz = new SimulatedVoiceAdapter(nc);
            dialogVoz.add(tuPanelDeVoz);
            dialogVoz.pack();
            dialogVoz.setLocationRelativeTo(principal);
            dialogVoz.setVisible(true);
        });


        nc.addListener((cmd, payload) -> {
            System.out.println(">> COMANDO EJECUTADO: " + cmd);

            // Usamos SwingUtilities.invokeLater para encolar la acción y asegurar que
            // no haya conflictos de hilos.
            SwingUtilities.invokeLater(() -> {
                switch (cmd) {
                    case GUARDAR_DOCUMENTO:
                        // TRUCO: Traemos la ventana principal al frente y le damos el foco.
                        // Esto hace que el FileDialog (AWT) sepa dónde "agarrarse" y aparezca correctamente.
                        principal.toFront();
                        principal.requestFocus();
                        EditorController.guardarArchivo(principal, textPane, progressLabel);
                        break;
                    case ABRIR_DOCUMENTO:
                        // Lo mismo para abrir
                        principal.toFront();
                        principal.requestFocus();
                        EditorController.abrirArchivo(principal, textPane, progressLabel);
                        break;
                    case APLICAR_NEGRITA:
                        boolean nuevoNegrita = !btnNegrita.isSelected();
                        btnNegrita.setSelected(nuevoNegrita);
                        EditorController.aplicarEstilo(textPane, Font.BOLD, nuevoNegrita);
                        break;
                    case APLICAR_CURSIVA:
                        boolean nuevoCursiva = !btnCursiva.isSelected();
                        btnCursiva.setSelected(nuevoCursiva);
                        EditorController.aplicarEstilo(textPane, Font.ITALIC, nuevoCursiva);
                        break;
                    case MAYUSCULAS:
                        EditorController.transformarSeleccion(textPane, true);
                        break;
                    case MINUSCULAS:
                        EditorController.transformarSeleccion(textPane, false);
                        break;
                    case DESHACER:
                        if (undoManager.canUndo()) undoManager.undo();
                        break;
                    case REHACER:
                        if (undoManager.canRedo()) undoManager.redo();
                        break;
                }
            });
        });

        principal.setVisible(true);
    }
}