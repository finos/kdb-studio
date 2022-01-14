package studio.ui.dndtabbedpane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.io.IOException;

class DropTargetHandler implements DropTargetListener {
    private final DraggableTabbedPane pane;

    private static final Logger log = LogManager.getLogger();

    private boolean before;
    private final static int WIDTH = 4;

    DropTargetHandler(DraggableTabbedPane pane) {
        this.pane = pane;
    }

    private TabbedPaneTransferable getTransferable(Transferable transferable) {
        if (! transferable.isDataFlavorSupported(TabbedPaneTransferable.TABBED_PANE_DATA_FLAVOR)) {
            return null;
        }

        try {
            TabbedPaneTransferable data = (TabbedPaneTransferable) transferable.getTransferData(TabbedPaneTransferable.TABBED_PANE_DATA_FLAVOR);
            if (data.getPane().getDragID().equals(pane.getDragID())) return data;
        } catch (UnsupportedFlavorException | IOException e) {
            log.error("Error during getting TabbedPaneTransferable", e);
        }
        return null;
    }

    private static Rectangle flip(boolean doFlip, Rectangle rect) {
        if (!doFlip) return rect;
        return new Rectangle(rect.y, rect.x, rect.height, rect.width);
    }

    private static Point flip(boolean doFlip, Point p) {
        if (!doFlip) return p;
        return new Point(p.y, p.x);
    }

    private int  checkHighlight(Transferable transferable, Point location) {
        if (getTransferable(transferable) == null) return -1;

        int idx = pane.indexAtLocation(location.x, location.y);
        int tabPlacement = pane.getTabPlacement();
        boolean flip = tabPlacement == JTabbedPane.LEFT || tabPlacement == JTabbedPane.RIGHT;

        location = flip(flip, location);

        int index = -1;
        if (idx == -1) {
            int dist = Integer.MAX_VALUE;

            int count = pane.getTabCount();
            for (int i = 0; i<count; i++) {
                Rectangle rect = pane.getBoundsAt(i);
                if (rect == null) continue;
                rect = flip(flip, rect);

                if (! (rect.y<=location.y && location.y<=rect.y+rect.height) ) continue;

                int dBefore = Math.abs(rect.x - location.x);
                int dAfter = Math.abs(rect.x + rect.width - location.x);

                if (dBefore < dist) {
                    dist = dBefore;
                    before = true;
                    index = i;
                }
                if (dAfter < dist) {
                    dist = dAfter;
                    before = false;
                    index = i;
                }

            }
        } else {
            index = idx;
            Rectangle r = flip(flip, pane.getBoundsAt(idx));
            before = location.x < r.x + r.width/2;
        }

        if (index == -1) {
            pane.setTargetRect(null);
        } else {
            Rectangle r = flip(flip, pane.getBoundsAt(index));
            int x = before ? r.x-WIDTH/2 : r.x + r.width - WIDTH/2;
            pane.setTargetRect(flip(flip, new Rectangle(x, r.y, WIDTH, r.height)));
        }

        return index;
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        if ( getTransferable(dtde.getTransferable()) == null ) {
            dtde.rejectDrag();
        } else {
            dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
            checkHighlight(dtde.getTransferable(), dtde.getLocation());
        }
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
        checkHighlight(dtde.getTransferable(), dtde.getLocation());
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
        pane.setTargetRect(null);
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        try {
            int index = checkHighlight(dtde.getTransferable(), dtde.getLocation());
            pane.setTargetRect(null);

            if (index == -1) return;

            TabbedPaneTransferable data = getTransferable(dtde.getTransferable());
            if (data == null) return;

            DraggableTabbedPane source = data.getPane();
            int srcIndex = data.getIndex();
            if (!before) index++;
            if (pane == source && index > srcIndex) index--;

            boolean success = source.dragTab(srcIndex, pane, index);
            if (success) {
                dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
            } else {
                dtde.rejectDrop();
            }
            dtde.dropComplete(success);
        } catch (RuntimeException e) {
            log.error("Unexpected exception", e);
        }
    }

}
