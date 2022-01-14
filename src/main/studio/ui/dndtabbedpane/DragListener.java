package studio.ui.dndtabbedpane;

import java.util.EventListener;

public interface DragListener extends EventListener {

    void dragged(DragEvent event);
}
