package Components;

import javax.swing.*;
import java.awt.*;

public class ProgressLabel extends JPanel {//Al extender JPanel es un componente que se puede colocar en cuaslquier ventana

    public final JLabel label;
    public final JProgressBar progressBar;

    public final String defaultText;//Texto que se muestra al finalizar una tarea

    // Colores diferentes resultados
    private final Color SUCCESS_COLOR = new Color(34, 139, 34);
    private final Color ERROR_COLOR = new Color(220, 20, 60);

    private Color foregroundColor = Color.BLACK;

    //Inicializa los componentes y los coloca en el layout
    public ProgressLabel(String defaultText) {
        super(new BorderLayout(5, 0));

        this.defaultText = defaultText;

        label = new JLabel("");
        progressBar = new JProgressBar(0,100);

        this.add(label,BorderLayout.WEST);
        this.add(progressBar,BorderLayout.CENTER);

        progressBar.setVisible(false);//No se debe ver la barra
    }

    //Muestra el mensaje de la tarea al iniciar, pone el porcentaje a 0 y pone visible la barra
    public void startTask(String mensaje) {
        label.setText(mensaje);
        label.setForeground(foregroundColor);

        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setString("0%");
        progressBar.setVisible(true);
    }

    //Cada vez que el swingWorker hace un publich, se actualiza el porcentaje
    public void updateProgress(String nomTarea, int progreso) {
        label.setText(nomTarea);

        progressBar.setValue(progreso);
        progressBar.setStringPainted(true);
        progressBar.setString(progreso + "%");
    }

    //Mensaje final y barra al 100%
    public void finishTask(String mensaje) {
        label.setText(mensaje);
        label.setForeground(SUCCESS_COLOR);

        progressBar.setValue(100);
        progressBar.setString("100%");

        hideAfterDelay();
    }

    //Mensaje de error y cambio de colores
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
            progressBar.setStringPainted(false); // Desactivar el texto al ocultar
            progressBar.setForeground(UIManager.getColor("ProgressBar.foreground"));

        });
        timer.setRepeats(false);
        timer.start();
    }
}