package studio.ui;

import javax.swing.JFrame;
import studio.kdb.Server;

public class EditServerForm extends ServerForm {
    public EditServerForm(JFrame owner, Server server) {
        super(owner, "Edit Server Details", server);
    }
}
