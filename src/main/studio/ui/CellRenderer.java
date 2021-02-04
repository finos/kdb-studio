package studio.ui;

import studio.kdb.Config;
import studio.kdb.K;
import studio.kdb.KFormatContext;
import studio.kdb.KTableModel;
import java.awt.Color;
import java.awt.Component;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

class CellRenderer extends DefaultTableCellRenderer {
    private static final Color keyColor = new Color(220,255,220);
    private static final Color altColor = new Color(220,220,255);
    private static final Color nullColor = new Color(255,150,150);
    private static final Color selColor = UIManager.getColor("Table.selectionBackground");
    private static final Color fgColor = UIManager.getColor("Table.foreground");
    private JTable table = null;

    private KFormatContext formatContextWithType, formatContextNoType;

    private void initLabel(JTable table) {
        setHorizontalAlignment(SwingConstants.LEFT);
        setOpaque(true);
    }

    public CellRenderer(JTable t) {
        setFormatContext(KFormatContext.DEFAULT);
        table = t;
        table.addPropertyChangeListener(propertyChangeEvent -> {
            if ("zoom".equals(propertyChangeEvent.getPropertyName()))
                setFont(table.getFont());
        });

        initLabel(t);
        setFont(UIManager.getFont("Table.font"));
        setBackground(UIManager.getColor("Table.background"));
    }

    public void setFormatContext(KFormatContext formatContext) {
        formatContextWithType = new KFormatContext(formatContext).setShowType(true);
        formatContextNoType = new KFormatContext(formatContext).setShowType(false);
    }

    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int column) {
        K.KBase kb = (K.KBase) value;
        String text = kb.toString(kb instanceof K.KBaseVector ? formatContextWithType : formatContextNoType);
        text = Util.limitString(text, Config.getInstance().getMaxCharsInTableCell());
        setText(text);
        setForeground(kb.isNull() ? nullColor : fgColor);

        if (!isSelected) {
            KTableModel ktm = (KTableModel) table.getModel();
            column = table.convertColumnIndexToModel(column);
            if (ktm.isKey(column))
                setBackground(keyColor);
            else if (row % 2 == 0)
                setBackground(altColor);
            else
                setBackground(UIManager.getColor("Table.background"));
        }
        else {
            setForeground(UIManager.getColor("Table.selectionForeground"));
            setBackground(selColor);
        }
        return this;
    }
}
