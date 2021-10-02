package studio.ui;

import studio.core.AuthenticationManager;
import studio.core.Credentials;
import studio.kdb.Config;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import javax.swing.filechooser.FileFilter;

import static javax.swing.GroupLayout.PREFERRED_SIZE;

public class QPadImportDialog extends EscapeDialog {

    private JTextField txtServers;
    private JRadioButton btnOverwrite, btnAppend;
    private JTextField txtRootName;
    private JComboBox comboBoxAuthMechanism;
    private JTextField txtUser;
    private JPasswordField txtPassword;


    private JFileChooser chooser;

    public enum Location {Overwrite, Append}

    public QPadImportDialog(JFrame parent) {
        super(parent, "QPad Server List Import");
        initComponents();
    }

    public String getServersCfgLocation() {
        return txtServers.getText();
    }

    public String getDefaultAuthenticationMechanism() {
        return comboBoxAuthMechanism.getModel().getSelectedItem().toString();
    }

    public Credentials getCredentials() {
        return new Credentials(txtUser.getText().trim(), new String(txtPassword.getPassword()));
    }

    public Location getImportTo() {
        if (btnOverwrite.isSelected()) return Location.Overwrite;
        if (btnAppend.isSelected()) return Location.Append;

        return null;
    }

    public String getRootName() {
        return txtRootName.getText();
    }

    private void refreshCredentials() {
        Credentials credentials = Config.getInstance().getDefaultCredentials(getDefaultAuthenticationMechanism());

        txtUser.setText(credentials.getUsername());
        txtPassword.setText(credentials.getPassword());
    }

    private void initComponents() {
        JLabel lblServers = new JLabel("Select location of Servers.cfg:     ");
        txtServers = new JTextField();
        JButton btnServers = new JButton("...");

        JLabel lblAuthMechanism = new JLabel("Authentication:");
        JLabel lblUser = new JLabel("  User:");
        JLabel lblPassword = new JLabel("  Password:");
        comboBoxAuthMechanism = new JComboBox(AuthenticationManager.getInstance().getAuthenticationMechanisms());
        comboBoxAuthMechanism.getModel().setSelectedItem(Config.getInstance().getDefaultAuthMechanism());
        comboBoxAuthMechanism.addItemListener(e -> refreshCredentials());
        txtUser = new JTextField();
        txtPassword = new JPasswordField();


        JLabel lblLocation = new JLabel("Location where servers will be imported in the current Server Tree:     ");
        btnOverwrite = new JRadioButton("Overwrite");
        btnAppend = new JRadioButton("Append");
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(btnOverwrite);
        buttonGroup.add(btnAppend);

        JLabel lblRootName = new JLabel("Root name of importer server or leave it empty:     ");
        txtRootName = new JTextField();

        JPanel main = new JPanel();
        GroupLayout layout = new GroupLayout(main);
        main.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(
                layout.createParallelGroup()
                    .addGroup(
                            layout.createSequentialGroup()
                                .addComponent(lblServers)
                                .addComponent(txtServers)
                                .addComponent(btnServers)
                    ).addGroup(
                            layout.createSequentialGroup()
                                .addComponent(lblAuthMechanism)
                                .addComponent(comboBoxAuthMechanism, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
                                .addComponent(lblUser)
                                .addComponent(txtUser, 150, 150,150)
                                .addComponent(lblPassword)
                                .addComponent(txtPassword, 150, 150, 150)
                ).addGroup(
                            layout.createSequentialGroup()
                                .addComponent(lblLocation)
                                .addComponent(btnOverwrite)
                                .addComponent(btnAppend)
                ).addGroup(
                            layout.createSequentialGroup()
                                .addComponent(lblRootName)
                                .addComponent(txtRootName)
                    )
        );

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                    .addGroup(
                        layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(lblServers)
                                .addComponent(txtServers)
                                .addComponent(btnServers)
                    ).addGroup(
                        layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(lblAuthMechanism)
                                .addComponent(comboBoxAuthMechanism)
                                .addComponent(lblUser)
                                .addComponent(txtUser)
                                .addComponent(lblPassword)
                                .addComponent(txtPassword)
                    ).addGroup(
                        layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(lblLocation)
                                .addComponent(btnOverwrite)
                                .addComponent(btnAppend)
                    ).addGroup(
                        layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(lblRootName)
                                .addComponent(txtRootName)
                    )
        );

        JButton btnImport = new JButton("Import");
        JButton btnCancel = new JButton("Cancel");
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(btnImport);
        bottom.add(btnCancel);

        JPanel root = new JPanel(new BorderLayout());
        root.add(main, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);
        setContentPane(root);

        chooser = new JFileChooser();
        FileFilter serversCfgFF = new ServersCfgFileFilter();
        chooser.setFileHidingEnabled(false);
        chooser.addChoosableFileFilter(serversCfgFF);
        chooser.setFileFilter(serversCfgFF);
        btnImport.addActionListener(e->checkAndAccept());
        btnCancel.addActionListener(e->cancel());
        btnServers.addActionListener(e->selectServersCfg());
    }

    private void selectServersCfg() {
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.ERROR_OPTION) {
            JOptionPane.showMessageDialog(this, "Error while selecting Servers.cfg",
                                        "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (result == JFileChooser.CANCEL_OPTION) {
            return;
        }

        String location = chooser.getSelectedFile().getAbsoluteFile().toString();
        txtServers.setText(location);
    }

    private void checkAndAccept() {
        String location = getServersCfgLocation();
        if (location.trim().length() == 0) {
            JOptionPane.showMessageDialog(this, "File to import is not selected",
                    "Not Found Servers.cfg", JOptionPane.ERROR_MESSAGE);
            return;

        }

        if (! new File(location).exists()) {
            JOptionPane.showMessageDialog(this, "File to import doesn't not exist (" + location + ")",
                                "Not Found Servers.cfg", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (getImportTo() == null) {
            JOptionPane.showMessageDialog(this, "Select how current Server Tree should be modified: Overwrite or Append",
                    "Where to Import", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (getRootName().contains("/")) {
            JOptionPane.showMessageDialog(this, "Folder name can't contain /",
                    "Wrong Folder Name", JOptionPane.ERROR_MESSAGE);
            return;
        }

        accept();
    }

    private static class ServersCfgFileFilter extends FileFilter {
        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) return true;
            return f.getName().equals("Servers.cfg");
        }

        @Override
        public String getDescription() {
            return "Servers.cfg";
        }
    }

    public static void main(String[] args) {
        QPadImportDialog d = new QPadImportDialog(null);

        d.pack();
        d.setVisible(true);
    }
}
