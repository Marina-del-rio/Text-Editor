package Components;

import javax.swing.text.*;

public class WrapColumnFactory implements ViewFactory {

    @Override
    //Crea las vistas personalizadas para el contenido del texto (WrapLabelView)
    //y vistas estandar para lo demás
    public View create(Element elem) {
        String type = elem.getName();
        if(type != null) {
            if(type.equals(AbstractDocument.ContentElementName)) {
                return new WrapLabelView(elem);//se usa la vista personalizada

            } else if(type.equals(AbstractDocument.ParagraphElementName)) {
                return new ParagraphView(elem);

            } else if(type.equals(AbstractDocument.SectionElementName)) {
                return new BoxView(elem, View.Y_AXIS);

            } else if (type.equals(StyleConstants.ComponentElementName)) {
                return new ComponentView(elem);

            } else if (type.equals(StyleConstants.IconElementName)) {
                return new IconView(elem);
            }
        }
        return new LabelView(elem);
    }
}



