package studio.ui.dndtabbedpane;

import java.util.EventListener;

// We need a separate listener which is invoked from DragSourceHandler.
// Removal of DraggableTabbedPane component from UI should happened from this listener
public interface DragCompleteListener extends EventListener {

    void dragComplete(boolean success);
}
