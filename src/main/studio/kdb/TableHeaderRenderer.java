package studio.kdb;

import javax.swing.border.Border;

import studio.ui.SorterDrawer;

import java.awt.*;
import javax.swing.*;

import javax.swing.table.DefaultTableCellRenderer;

public class TableHeaderRenderer extends DefaultTableCellRenderer {

    public TableHeaderRenderer() {
        super();
        setHorizontalAlignment(SwingConstants.LEFT);
        setVerticalAlignment(SwingConstants.CENTER);
        setOpaque(true);
        Border border = UIManager.getBorder("TableHeader.cellBorder");
        if (border == null) {
            border = BorderFactory.createMatteBorder(0, 0, 2, 1, Color.BLACK);
        }
        // add gap for sorter icon
        setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(5,2,1,0)));

        setFont(UIManager.getFont("TableHeader.font"));
        setBackground(UIManager.getColor("TableHeader.background"));
        setForeground(UIManager.getColor("TableHeader.foreground"));
    }

    public void setFont(Font f) {
        super.setFont(f);
        invalidate();
    }

    private boolean asc = false;
    private boolean desc = false;

    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int column) {
        // setFont(table.getFont());

        if (table.getModel() instanceof KTableModel) {
            column = table.convertColumnIndexToModel(column);
            KTableModel ktm = (KTableModel) table.getModel();
            if (ktm.isSortedAsc(column)) {
                asc = true;
                desc = false;
            } else if (ktm.isSortedDesc(column)) {
                asc = false;
                desc = true;
            } else {
                asc = false;
                desc = false;
            }
        }

        setText(value == null ? " " : value.toString());

        return this;
    }

    @Override
    public void paint(Graphics g) {
        int width = SwingUtilities.computeStringWidth(getFontMetrics(getFont()), getText());
        int availableWidth = Math.min(getInsets().left + width, getSize().width);
        SorterDrawer.paint(asc, desc, this, availableWidth, g);
        super.paint(g);
    }
}
