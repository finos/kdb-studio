package studio.ui.dndtabbedpane;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

class TabbedPaneTransferable implements Transferable {

    public static final DataFlavor TABBED_PANE_DATA_FLAVOR = new DataFlavor(TabbedPaneTransferable.class, "TabbedPane");
    private final DraggableTabbedPane pane;
    private final int index;

    public TabbedPaneTransferable(DraggableTabbedPane pane, int index) {
        this.pane = pane;
        this.index = index;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{TABBED_PANE_DATA_FLAVOR};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return TABBED_PANE_DATA_FLAVOR.equals(flavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (!isDataFlavorSupported(flavor)) throw new UnsupportedFlavorException(flavor);

        return this;
    }

    public int getIndex() {
        return index;
    }

    public DraggableTabbedPane getPane() {
        return  pane;
    }

}