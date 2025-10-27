import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.ActionEvent;

public class EditorActions {

    public static void configurarAtajos(JTextPane textPane, JToggleButton btnNegrita, JToggleButton btnCursiva, UndoManager undoManager, JFrame principal) {

        // Ctrl + M
        textPane.getInputMap().put(KeyStroke.getKeyStroke("control M"), "mayus");
        textPane.getActionMap().put("mayus", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EditorController.transformarSeleccion(textPane, true);
            }
        });

        // Ctrl + N
        textPane.getInputMap().put(KeyStroke.getKeyStroke("control N"), "minus");
        textPane.getActionMap().put("minus", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EditorController.transformarSeleccion(textPane, false);
            }
        });

        // Ctrl + B
        textPane.getInputMap().put(KeyStroke.getKeyStroke("control B"), "negrita");
        textPane.getActionMap().put("negrita", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnNegrita.setSelected(!btnNegrita.isSelected());
                EditorController.aplicarEstilo(textPane, Font.BOLD, btnNegrita.isSelected());
            }
        });

        // Ctrl + I
        textPane.getInputMap().put(KeyStroke.getKeyStroke("control I"), "cursiva");
        textPane.getActionMap().put("cursiva", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnCursiva.setSelected(!btnCursiva.isSelected());
                EditorController.aplicarEstilo(textPane, Font.ITALIC, btnCursiva.isSelected());
            }
        });

        // Ctrl + F
        textPane.getInputMap().put(KeyStroke.getKeyStroke("control F"), "buscar");
        textPane.getActionMap().put("buscar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EditorController.buscarRemplazar(principal, textPane);
            }
        });

        // Ctrl + Z
        textPane.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "deshacer");
        textPane.getActionMap().put("deshacer", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (undoManager.canUndo()) undoManager.undo();
            }
        });

        // Ctrl + alt + Z (Remake)
        textPane.getInputMap().put(KeyStroke.getKeyStroke("control alt Z"), "rehacer");
        textPane.getActionMap().put("rehacer", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (undoManager.canRedo()) undoManager.redo();
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

        // Ctrl + S
        textPane.getInputMap().put(KeyStroke.getKeyStroke("control S"), "guardar");
        textPane.getActionMap().put("guardar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EditorController.guardarArchivo(principal, textPane);
            }
        });
    }
}