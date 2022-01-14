package studio.ui.dndtabbedpane;

import studio.ui.Util;

import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.image.BufferedImage;

class DragGestureHandler implements DragGestureListener {
    private final DraggableTabbedPane pane;

    private static final Cursor dragCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

    DragGestureHandler(DraggableTabbedPane pane) {
        this.pane = pane;
    }

    @Override
    public void dragGestureRecognized(DragGestureEvent dge) {
        Point location = dge.getDragOrigin();
        int index = pane.indexAtLocation(location.x, location.y);
        if (index == -1) return;

        Rectangle r = pane.getBoundsAt(index);
        BufferedImage image = new BufferedImage(r.width, r.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.translate(-r.x, -r.y);
        pane.paint(g);
        g.dispose();

        pane.setSourceRect(r);
        pane.repaint();

        Transferable t = new TabbedPaneTransferable(pane, index);
        DragSource ds = dge.getDragSource();

        //That's odd. However Mac and Windows behavior opposite. We need to correct.
        int correction = Util.MAC_OS_X ? -1 : 1;
        Point dragOffset = new Point(correction * (location.x - r.x),correction * (location.y - r.y));

        ds.startDrag(
                dge,
                dragCursor,
                image, dragOffset,
                t,
                new DragSourceHandler());

    }
}
