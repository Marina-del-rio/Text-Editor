package Components;

import javax.swing.*;
import java.awt.*;

public class ProgressLabel extends JPanel {
    public final JLabel label;
    public final JProgressBar progressBar;

    public final String defaultText;

    // Colores diferentes resultados
    private final Color SUCCESS_COLOR = new Color(34, 139, 34); // Verde oscuro
    private final Color ERROR_COLOR = new Color(220, 20, 60);   // Rojo carmesí

    private Color foregroundColor = Color.BLACK;

    public ProgressLabel(String defaultText) {
        super(new BorderLayout(5, 0));

        this.defaultText = defaultText;

        label = new JLabel("");
        progressBar = new JProgressBar(0,100);

        this.add(label,BorderLayout.WEST);
        this.add(progressBar,BorderLayout.CENTER);

        progressBar.setVisible(false);//No se debe ver la barra
    }

    public void startTask(String mensaje) {
        label.setText(mensaje);
        label.setForeground(foregroundColor);

        progressBar.setValue(0);
        progressBar.setVisible(true);
    }

    public void updateProgress(String nomTarea, int progreso) {
        label.setText(nomTarea);

        progressBar.setValue(progreso);
    }

    public void finishTask(String mensaje) {
        label.setText(mensaje);
        label.setForeground(SUCCESS_COLOR);

        progressBar.setValue(100);

        hideAfterDelay();
    }

    public void showError(String mensaje) {
        label.setText(mensaje);
        label.setForeground(ERROR_COLOR);

        progressBar.setForeground(ERROR_COLOR.darker());

        hideAfterDelay();
    }

    //Para modo oscuro
    @Override
    public void setForeground(Color color) {
        this.foregroundColor = color;
        if (label != null) {
            label.setForeground(color);
        }
    }

    //temporizador ocultar el label
    public void hideAfterDelay() {
        int ocultar_retraso = 2000;

        Timer timer = new Timer(ocultar_retraso, e -> {
            this.setVisible(false);

            label.setText(defaultText);
            label.setForeground(foregroundColor); // Restablecer al color por defecto

            progressBar.setValue(0);
            progressBar.setForeground(UIManager.getColor("ProgressBar.foreground"));

        });
        timer.setRepeats(false);
        timer.start();
    }

}
