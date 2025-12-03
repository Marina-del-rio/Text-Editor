package Nui;

import java.util.ArrayList;
import java.util.List;



public class NuiController {

    public List<NuiListener> listeners = new ArrayList<>();

    public void addListener(NuiListener l) {
        listeners.add(l);
    }

    public void sendCommand(NuiCommand cmd, String payload) {
        for (NuiListener l : listeners) {
            l.onCommand(cmd, payload);
        }
    }
}