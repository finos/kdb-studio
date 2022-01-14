package studio.ui;

import javax.swing.*;
import java.awt.*;

public class MinSizeLabel extends JLabel {

    private int minWidth;

    public MinSizeLabel() {
        super();
    }

    public MinSizeLabel(String text) {
        super(text);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension prefSize = super.getPreferredSize();
        Insets insets = getInsets();
        return new Dimension(Math.max(minWidth + insets.left + insets.right, prefSize.width), prefSize.height);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    public void setMinimumWidth(String... texts) {
        FontMetrics fm = getFontMetrics(getFont());
        minWidth = 0;
        for (String text: texts) {
            minWidth = Math.max(minWidth, SwingUtilities.computeStringWidth(fm, text));
        }
    }

}
