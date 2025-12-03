package Nui.adapters;

import Nui.*;

import javax.swing.*;
import java.awt.*;

public class SimulatedVoiceAdapter extends JPanel {

    private JTextField input;
    private NuiController controller;

    public SimulatedVoiceAdapter(NuiController controller) {
        this.controller = controller;

        setLayout(new BorderLayout());
        add(new JLabel("Simulador de voz:"), BorderLayout.NORTH);

        input = new JTextField();
        add(input, BorderLayout.CENTER);

        JButton send = new JButton("Enviar comando");
        send.addActionListener(e -> interpretCommand());
        add(send, BorderLayout.SOUTH);
    }

    private void interpretCommand() {
        String text = input.getText().toLowerCase();

        input.setText("");

        if (text.contains("guardar")){
            controller.sendCommand(NuiCommand.GUARDAR_DOCUMENTO, null);}

        else if (text.contains("abrir")){
            controller.sendCommand(NuiCommand.ABRIR_DOCUMENTO, null);
        }

        else if (text.contains("negrita")){
            controller.sendCommand(NuiCommand.APLICAR_NEGRITA, null);
        }
        else if (text.contains("cursiva")){
            controller.sendCommand(NuiCommand.APLICAR_CURSIVA, null);
        }
        else if (text.contains("mayusculas")){
            controller.sendCommand(NuiCommand.MAYUSCULAS, null);
        }
        else if (text.contains("minusculas")){
            controller.sendCommand(NuiCommand.MINUSCULAS, null);
        }
        else if (text.contains("deshacer")){
            controller.sendCommand(NuiCommand.DESHACER, null);
        }
        else if (text.contains("rehacer")){
            controller.sendCommand(NuiCommand.REHACER, null);
        }

    }
}