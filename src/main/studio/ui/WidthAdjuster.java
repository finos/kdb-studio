package studio.ui;

import studio.kdb.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.table.*;

public class WidthAdjuster extends MouseAdapter {

    private JTable table;
    private JScrollPane scrollPane;
    private int gap;
    private int cellMaxWidth;

    private static final int EPSILON = 5;   //boundary sensitivity
    private boolean[] limitWidthState;

    public WidthAdjuster(JTable table, JScrollPane scrollPane) {
        this.table = table;
        this.scrollPane = scrollPane;
        table.getTableHeader().addMouseListener(this);
        int colCount = table.getColumnCount();
        limitWidthState = new boolean[colCount];
        Arrays.fill(limitWidthState, true);
        int charWidth = SwingUtilities.computeStringWidth(table.getFontMetrics(UIManager.getFont("Table.font")), "x");
        gap =  (int) Math.round(charWidth * Config.getInstance().getDouble(Config.CELL_RIGHT_PADDING));
        cellMaxWidth = charWidth * Config.getInstance().getInt(Config.CELL_MAX_WIDTH);
    }

    public void mousePressed(MouseEvent evt) {
        if (evt.getClickCount() > 1 && usingResizeCursor())
            if ((table.getSelectedRowCount() == table.getRowCount()) && (table.getSelectedColumnCount() == table.getColumnCount()))
                resizeAllColumns(false);
            else {
                int col = getLeftColumn(evt.getPoint());
                if (col == -1) return;
                limitWidthState[col] = ! limitWidthState[col];
                resize(getLeftColumn(evt.getPoint()), limitWidthState[col]);
            }
    }

    public void mouseClicked(final MouseEvent e) {
        if (!usingResizeCursor()) {
            JTableHeader h = (JTableHeader) e.getSource();
            TableColumnModel columnModel = h.getColumnModel();
            int viewColumn = columnModel.getColumnIndexAtX(e.getX());
            if (viewColumn >= 0) {
                final int column = columnModel.getColumn(viewColumn).getModelIndex();

                KTableModel ktm = (KTableModel) table.getModel();
                ktm.sort(column);

                scrollPane.repaint();
            }
        }
    }

    private JTableHeader getTableHeader() {
        return table.getTableHeader();
    }

    private boolean usingResizeCursor() {
        Cursor cursor = getTableHeader().getCursor();
        return cursor.equals(EAST) || cursor.equals(WEST);
    }
    private static final Cursor EAST = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
    private static final Cursor WEST = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
    //if near the boundary, will choose left column
    private int getLeftColumn(Point pt) {
        pt.x -= EPSILON;
        return getTableHeader().columnAtPoint(pt);
    }

    public void resizeAllColumns(boolean limitWidth) {
        for (int i = 0;i < table.getColumnCount();i++)
            resize(i, limitWidth);
    }

    private void resize(int col, boolean limitWidth) {
        TableColumnModel tcm = table.getColumnModel();
        TableColumn tc = tcm.getColumn(col);
        TableCellRenderer tcr = tc.getHeaderRenderer();
        if (tcr == null)
            tcr = table.getTableHeader().getDefaultRenderer();

        Component comp = tcr.getTableCellRendererComponent(table,tc.getHeaderValue(),false,false,0,col);
        int maxWidth = comp.getPreferredSize().width;

        int ub = table.getRowCount();

        int stepSize = ub / 1000;

        if (stepSize == 0)
            stepSize = 1;

        for (int i = 0;i < ub;i += stepSize) {
            tcr = table.getCellRenderer(i,col);
            Object obj = table.getValueAt(i,col);
            comp = tcr.getTableCellRendererComponent(table,obj,false,false,i,col);
            maxWidth = Math.max(maxWidth, 2 + gap + comp.getPreferredSize().width); // we need to add a gap for lines between cells
        }
        if (limitWidth) {
            maxWidth = Math.min(maxWidth, cellMaxWidth);
        }

        tc.setPreferredWidth(maxWidth); //remembers the value
        tc.setWidth(maxWidth);          //forces layout, repaint
    }
}
