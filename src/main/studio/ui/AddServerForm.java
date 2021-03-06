package studio.ui;

import java.awt.Window;
import studio.kdb.Server;

public class AddServerForm extends ServerForm {
    public AddServerForm(Window owner) {
        super(owner, "Add a new server", new Server());
    }
}
