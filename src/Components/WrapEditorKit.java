package Components;

import javax.swing.text.*;

public class WrapEditorKit extends StyledEditorKit {
    //Se le asigna al viewFactory el WrapColumnFactory
    ViewFactory defaultFactory = new WrapColumnFactory();

    @Override
    public ViewFactory getViewFactory() {
        return defaultFactory;
    }
}
