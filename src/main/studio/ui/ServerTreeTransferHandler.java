package studio.ui;

import studio.kdb.ServerTreeNode;

import javax.swing.TransferHandler;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

class ServerTreeTransferHandler extends TransferHandler {
    private static final DataFlavor nodesFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, "Nodes");
    private JTree tree;

    ServerTreeTransferHandler(JTree tree) {
        this.tree = tree;
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {
        if (!support.isDataFlavorSupported(nodesFlavor)) {
            return false;
        }

        JTree.DropLocation dropLocation = (JTree.DropLocation) support.getDropLocation();
        TreePath path = dropLocation.getPath();
        if (path == null) {
            return false;
        }

        ServerTreeNode targetNode = (ServerTreeNode) path.getLastPathComponent();
        if (!targetNode.isFolder()) {
            return false; // Only allow dropping onto folder nodes
        }

        ServerTreeNode transferableNode = null;
        try {
            transferableNode = (ServerTreeNode) support.getTransferable().getTransferData(nodesFlavor);
        } catch (UnsupportedFlavorException | IOException e) {
            throw new RuntimeException(e);
        }

        // Check if the transferable node is a folder
        if (!transferableNode.isFolder()) {
            return true; // Allow dropping servers onto folders
        }

        // Check if the transferable node is not the target node or a child of the target node
        if (transferableNode == targetNode || targetNode.isNodeDescendant(transferableNode)) {
            return false;
        }

        return true;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return MOVE;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }

        ServerList serverList = (ServerList) tree.getTopLevelAncestor();

        JTree.DropLocation dropLocation = (JTree.DropLocation) support.getDropLocation();
        TreePath path = dropLocation.getPath();
        ServerTreeNode targetNode = (ServerTreeNode) path.getLastPathComponent();

        try {
            ServerTreeNode transferableNode = (ServerTreeNode) support.getTransferable().getTransferData(nodesFlavor);

            serverList.moveNode(targetNode, transferableNode);
            return true;
        } catch (UnsupportedFlavorException | IOException e) {
            return false;
        }
    }

    @Override
    public Transferable createTransferable(JComponent c) {
        JTree tree = (JTree) c;
        TreePath path = tree.getSelectionPath();
        if (path == null) {
            return null;
        }
        ServerTreeNode node = (ServerTreeNode) path.getLastPathComponent();
        return new NodesTransferable(node);
    }

    private static class NodesTransferable implements Transferable {
        private DefaultMutableTreeNode node;

        NodesTransferable(DefaultMutableTreeNode node) {
            this.node = node;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{nodesFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return nodesFlavor.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (isDataFlavorSupported(flavor)) {
                return node;
            }
            throw new UnsupportedFlavorException(flavor);
        }
    }
}
