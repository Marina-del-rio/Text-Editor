package Nui.adapters;

import Nui.*;
import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.LibVosk;
import org.vosk.LogLevel;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.io.IOException;

public class VoskAdapter extends JPanel {

    private NuiController controller;

    // Control del hilo de escucha
    private volatile boolean isListening = false;
    private Thread captureThread;

    // Variables de Vosk y Audio
    private TargetDataLine microphone;
    private Model model;

    // Componente visual
    private JLabel statusLabel;

    public VoskAdapter(NuiController controller) {
        this.controller = controller;

        //Configuración visual
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(null);

        // Etiqueta informativa
        statusLabel = new JLabel("Cargando voz...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(Color.GRAY);
        add(statusLabel, BorderLayout.CENTER);

        //Apagado automático
        // Si cierras la app, apaga el micro para liberar recursos
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0 && !isDisplayable()) {
                pararEscucha();
            }
        });


        // Iniciar la carga del modelo en un hilo separado para no congelar
        new Thread(this::iniciarSistema).start();
    }

    // Método que carga el "cerebro" de la IA
    private void iniciarSistema() {
        LibVosk.setLogLevel(LogLevel.WARNINGS);

        try {
            System.out.println(">> [VOSK] Buscando carpeta 'vosk-model' en la raíz...");

            // Cargar modelo
            model = new Model("vosk-model");

            System.out.println(">> [VOSK] Modelo cargado correctamente.");

            // Actualizar la interfaz
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("️ Escucha activa");
            });

            // Arrancar el micrófono
            iniciarCapturaAudio();

        } catch (IOException e) {
            System.err.println(">> [ERROR CRÍTICO] No se encuentra la carpeta 'vosk-model'.");
            e.printStackTrace();

            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("Error modelo voz");
                statusLabel.setForeground(Color.RED);
                JOptionPane.showMessageDialog(this,
                        "Error: No se encuentra la carpeta 'vosk-model' en la raíz del proyecto.\n" +
                                "La escucha constante no funcionará.",
                        "Error de Voz", JOptionPane.ERROR_MESSAGE);
            });
        }
    }

    // Abre el micrófono y procesa el audio en bucle
    private void iniciarCapturaAudio() {
        isListening = true;

        captureThread = new Thread(() -> {
            // Vosk requiere audio a 16kHz
            try (Recognizer recognizer = new Recognizer(model, 16000)) {

                // Formato: 16kHz, 16 bits, Mono, Signed, Little Endian
                AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

                if (!AudioSystem.isLineSupported(info)) {
                    System.err.println(">> [ERROR] Micrófono no disponible o no soportado.");
                    return;
                }

                microphone = (TargetDataLine) AudioSystem.getLine(info);
                microphone.open(format);
                microphone.start();

                System.out.println(">> [VOSK] Micrófono abierto. ESCUCHANDO...");


                byte[] buffer = new byte[4096];//donde se recoge el audio

                while (isListening) {
                    // Leemos del micro
                    int nbytes = microphone.read(buffer, 0, buffer.length);

                    if (nbytes >= 0) {
                        // Enviar a Vosk para analizar
                        if (recognizer.acceptWaveForm(buffer, nbytes)) {
                            // Dtecta una frase completa, se procesa el resultado
                            procesarJSON(recognizer.getResult());
                        }
                    }
                }

            } catch (Exception e) {
                System.err.println(">> [ERROR] Fallo durante la captura de audio:");
                e.printStackTrace();
            }
        });
        captureThread.start();
    }

    // Detener todo limpiamente
    private void pararEscucha() {
        isListening = false;
        if (microphone != null && microphone.isOpen()) {
            microphone.close();
            System.out.println(">> [VOSK] Micrófono cerrado y liberado.");
        }
    }

    // Recibe el JSON sucio de Vosk y extrae el texto
    private void procesarJSON(String json) {
        String texto = json.toLowerCase();

        // IgnoraR silencios
        if (texto.contains("\"text\" : \"\"")) return;

        // Limpieza manual del JSON {"text": "hola"} -> hola
        String limpio = texto
                .replace("{", "")
                .replace("}", "")
                .replace("\"text\" : ", "")
                .replace("\"", "")
                .trim();

        if (!limpio.isEmpty()) {
            // MostraR en terminal lo que ha entendido
            System.out.println(">> [Voz]: " + limpio);

            // Convertirlo en un comando
            interpretarComando(limpio);
        }
    }

    // PALABRA -> ACCIÓN
    private void interpretarComando(String texto) {

        if (texto.contains("guardar")) {
            System.out.println("   -> Ejecutando: GUARDAR");
            controller.sendCommand(NuiCommand.GUARDAR_DOCUMENTO, null);
        }
        else if (texto.contains("abrir")) {
            System.out.println("   -> Ejecutando: ABRIR");
            controller.sendCommand(NuiCommand.ABRIR_DOCUMENTO, null);
        }
        else if (texto.contains("negrita")) {
            System.out.println("   -> Ejecutando: NEGRITA");
            controller.sendCommand(NuiCommand.APLICAR_NEGRITA, null);
        }
        else if (texto.contains("cursiva")) {
            System.out.println("   -> Ejecutando: CURSIVA");
            controller.sendCommand(NuiCommand.APLICAR_CURSIVA, null);
        }
        else if (texto.contains("mayúsculas") || texto.contains("mayusculas")) {
            System.out.println("   -> Ejecutando: MAYÚSCULAS");
            controller.sendCommand(NuiCommand.MAYUSCULAS, null);
        }
        else if (texto.contains("minúsculas") || texto.contains("minusculas")) {
            System.out.println("   -> Ejecutando: MINÚSCULAS");
            controller.sendCommand(NuiCommand.MINUSCULAS, null);
        }
        else if (texto.contains("deshacer")) {
            System.out.println("   -> Ejecutando: DESHACER");
            controller.sendCommand(NuiCommand.DESHACER, null);
        }
        else if (texto.contains("rehacer")) {
            System.out.println("   -> Ejecutando: REHACER");
            controller.sendCommand(NuiCommand.REHACER, null);
        }
    }
}