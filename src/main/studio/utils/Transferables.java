package studio.utils;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Transferables implements Transferable {

    private Map<DataFlavor, Transferable> transferableMap;
    private DataFlavor[] dataFlavors;

    public Transferables(Transferable... transferables) {
        transferableMap = new HashMap<>();
        List<DataFlavor> dataFlavorList = new ArrayList<>();
        for (Transferable transferable: transferables) {
            for (DataFlavor dataFlavor: transferable.getTransferDataFlavors()) {
                if (transferableMap.containsKey(dataFlavor)) continue;
                transferableMap.put(dataFlavor, transferable);
                dataFlavorList.add(dataFlavor);
            }
        }
        dataFlavors = dataFlavorList.toArray(new DataFlavor[0]);
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return dataFlavors;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return transferableMap.containsKey(flavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        Transferable transferable = transferableMap.get(flavor);
        if (transferable == null) throw new UnsupportedFlavorException(flavor);

        return transferable.getTransferData(flavor);
    }
}
