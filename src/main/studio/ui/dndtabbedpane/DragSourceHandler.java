package studio.ui.dndtabbedpane;

import java.awt.*;
import java.awt.dnd.*;

class DragSourceHandler implements DragSourceListener {

    public void dragEnter(DragSourceDragEvent dsde) {
    }

    public void dragOver(DragSourceDragEvent dsde) {
    }

    public void dropActionChanged(DragSourceDragEvent dsde) {
    }

    public void dragExit(DragSourceEvent dse) {
    }

    public void dragDropEnd(DragSourceDropEvent dsde) {

        Object source = dsde.getSource();
        if (! (source instanceof DragSourceContext)) return;

        Component component = ((DragSourceContext)source).getComponent();
        if (! (component instanceof DraggableTabbedPane)) return;

        DraggableTabbedPane tabbedPane = (DraggableTabbedPane) component;
        tabbedPane.setSourceRect(null);
        // Ideally I'd like to have a listener from DropTargetHandler.drop with full details
        // Unfortunately trying to close the panel from the drop method resulted
        // InvalidDnDOperationException Drag and drop in progress
        tabbedPane.fireDragComplete(dsde.getDropSuccess());
    }
}