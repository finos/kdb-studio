package studio.ui.dndtabbedpane;

public class DragEvent {
    private final int sourceIndex;
    private final int targetIndex;
    private final DraggableTabbedPane targetPane;

    public DragEvent(int sourceIndex, DraggableTabbedPane targetPane, int targetIndex) {
        this.sourceIndex = sourceIndex;
        this.targetIndex = targetIndex;
        this.targetPane = targetPane;
    }

    public int getSourceIndex() {
        return sourceIndex;
    }

    public int getTargetIndex() {
        return targetIndex;
    }

    public DraggableTabbedPane getTargetPane() {
        return targetPane;
    }
}
