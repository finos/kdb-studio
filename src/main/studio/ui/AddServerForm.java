package studio.ui;

import java.awt.Window;
import studio.kdb.Config;
import studio.kdb.Server;
import studio.kdb.ServerTreeNode;

public class AddServerForm extends ServerForm {

    private static Server newServer(ServerTreeNode parent) {
        Server server = new Server();
        server.setFolder(parent);
        server.setBackgroundColor(Config.getInstance().getColor(Config.getThemeEntry(Config.ThemeEntry.BACKGROUND)));
        return server;
    }

    public AddServerForm(Window owner, ServerTreeNode parent) {
        super(owner, "Add a new server", newServer(parent));
    }
}
