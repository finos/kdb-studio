package studio.kdb;

import studio.ui.SorterDrawer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class IndexHeader extends JLabel {

    private KTableModel tableModel;

    public IndexHeader(KTableModel tableModel, JScrollPane scrollPane) {
        super("");

        this.tableModel = tableModel;
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                tableModel.sort(-1);
                scrollPane.repaint();
            }
        });
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        SorterDrawer.paint(tableModel.isSortedAsc(-1), tableModel.isSortedDesc(-1), this, 0, g);
    }
}
