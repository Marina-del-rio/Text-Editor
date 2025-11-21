package Components;

import javax.swing.text.*;


public class WrapLabelView extends LabelView {

    public WrapLabelView(Element elem) {
        super(elem);
    }

    @Override
    //Sobreescribe el cálculo del ancho mínimo
    //Devuelve 0 en el eje X para forzar al layout a romper la línea si es necesario.
    public float getMinimumSpan(int axis) {
        switch (axis) {
            case X_AXIS -> { return 0; }
            case Y_AXIS -> { return super.getMinimumSpan(axis); }
            default -> throw new IllegalArgumentException("Invalid axis: " + axis);

        }
    }
}
