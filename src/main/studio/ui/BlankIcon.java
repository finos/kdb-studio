package studio.ui;

import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;

public class BlankIcon implements Icon {
    private int width;
    private int height;

    public BlankIcon(Icon icon) {
        width = icon.getIconWidth();
        height = icon.getIconHeight();
    }

    public BlankIcon(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void paintIcon(Component component,Graphics g,int i,int i0) {
    }

    public int getIconWidth() {
        return width;
    }

    public int getIconHeight() {
        return height;
    }
};
