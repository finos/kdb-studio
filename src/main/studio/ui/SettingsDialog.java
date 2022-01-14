package studio.ui;

import studio.core.AuthenticationManager;
import studio.core.Credentials;
import studio.kdb.Config;
import studio.utils.LineEnding;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import static javax.swing.GroupLayout.PREFERRED_SIZE;

public class SettingsDialog extends EscapeDialog {
    private JComboBox<String> comboBoxAuthMechanism;
    private JTextField txtUser;
    private JPasswordField txtPassword;
    private JCheckBox chBoxShowServerCombo;
    private JCheckBox chBoxAutoSave;
    private JCheckBox chBoxSaveOnExit;
    private JCheckBox chBoxRTSAAnimateBracketMatching;
    private JCheckBox chBoxRTSAHighlightCurrentLine;
    private JCheckBox chBoxRTSAWordWrap;
    private JComboBox<CustomiszedLookAndFeelInfo> comboBoxLookAndFeel;
    private JFormattedTextField txtTabsCount;
    private JFormattedTextField txtMaxCharsInResult;
    private JFormattedTextField txtMaxCharsInTableCell;
    private JFormattedTextField txtCellRightPadding;
    private JFormattedTextField txtCellMaxWidth;
    private JFormattedTextField txtMaxFractionDigits;
    private JFormattedTextField txtEmulateDoubleClickTimeout;
    private JComboBox<Config.ExecAllOption> comboBoxExecAll;
    private JComboBox<LineEnding> comboBoxLineEnding;
    private JButton btnOk;
    private JButton btnCancel;

    private final static int FIELD_SIZE = 150;

    public SettingsDialog(JFrame owner) {
        super(owner, "Settings");
        initComponents();
    }

    public String getDefaultAuthenticationMechanism() {
        return comboBoxAuthMechanism.getModel().getSelectedItem().toString();
    }

    public String getUser() {
        return txtUser.getText().trim();
    }

    public String getPassword() {
        return new String(txtPassword.getPassword());
    }

    public boolean isShowServerComboBox() {
        return chBoxShowServerCombo.isSelected();
    }

    public String getLookAndFeelClassName() {
        return ((CustomiszedLookAndFeelInfo)comboBoxLookAndFeel.getSelectedItem()).getClassName();
    }

    public int getResultTabsCount() {
        return (Integer) txtTabsCount.getValue();
    }

    public int getMaxCharsInResult() {
        return (Integer) txtMaxCharsInResult.getValue();
    }

    public int getMaxCharsInTableCell() {
        return (Integer) txtMaxCharsInTableCell.getValue();
    }

    public double getCellRightPadding() {
        return (Double) txtCellRightPadding.getValue();
    }

    public int getCellMaxWidth() {
        return (Integer) txtCellMaxWidth.getValue();
    }

    public Config.ExecAllOption getExecAllOption() {
        return (Config.ExecAllOption) comboBoxExecAll.getSelectedItem();
    }

    public boolean isAutoSave() {
        return chBoxAutoSave.isSelected();
    }

    public boolean isSaveOnExit() {
        return chBoxSaveOnExit.isSelected();
    }

    public boolean isAnimateBracketMatching() {
        return chBoxRTSAAnimateBracketMatching.isSelected();
    }

    public boolean isHighlightCurrentLine() {
        return chBoxRTSAHighlightCurrentLine.isSelected();
    }

    public boolean isWordWrap() {
        return chBoxRTSAWordWrap.isSelected();
    }

    public LineEnding getDefaultLineEnding() {
        return (LineEnding) comboBoxLineEnding.getSelectedItem();
    }

    public int getMaxFractionDigits() {
        return (Integer) txtMaxFractionDigits.getValue();
    }

    public int getEmulatedDoubleClickTimeout() {
        return (Integer) txtEmulateDoubleClickTimeout.getValue();
    }

    private void refreshCredentials() {
        Credentials credentials = Config.getInstance().getDefaultCredentials(getDefaultAuthenticationMechanism());

        txtUser.setText(credentials.getUsername());
        txtPassword.setText(credentials.getPassword());
    }

    @Override
    public void align() {
        super.align();
        btnOk.requestFocusInWindow();
    }

    private void initComponents() {
        txtUser = new JTextField(12);
        txtPassword = new JPasswordField(12);
        comboBoxAuthMechanism = new JComboBox<>(AuthenticationManager.getInstance().getAuthenticationMechanisms());
        comboBoxAuthMechanism.getModel().setSelectedItem(Config.getInstance().getDefaultAuthMechanism());
        comboBoxAuthMechanism.addItemListener(e -> refreshCredentials());
        refreshCredentials();

        JLabel lblLookAndFeel = new JLabel("Look and Feel:");

        LookAndFeels lookAndFeels = new LookAndFeels();
        comboBoxLookAndFeel = new JComboBox<>(lookAndFeels.getLookAndFeels());
        CustomiszedLookAndFeelInfo lf = lookAndFeels.getLookAndFeel(Config.getInstance().getLookAndFeel());
        if (lf == null) {
            lf = lookAndFeels.getLookAndFeel(UIManager.getLookAndFeel().getClass().getName());
        }

        chBoxRTSAAnimateBracketMatching = new JCheckBox("Animate bracket matching");
        chBoxRTSAAnimateBracketMatching.setSelected(Config.getInstance().getBoolean(Config.RSTA_ANIMATE_BRACKET_MATCHING));

        chBoxRTSAHighlightCurrentLine = new JCheckBox("Highlight current line");
        chBoxRTSAHighlightCurrentLine.setSelected(Config.getInstance().getBoolean(Config.RSTA_HIGHLIGHT_CURRENT_LINE));

        chBoxRTSAWordWrap = new JCheckBox("Word wrap");
        chBoxRTSAWordWrap.setSelected(Config.getInstance().getBoolean(Config.RSTA_WORD_WRAP));

        comboBoxLookAndFeel.setSelectedItem(lf);
        JLabel lblResultTabsCount = new JLabel("Result tabs count");
        NumberFormatter formatter = new NumberFormatter();
        formatter.setMinimum(1);
        txtTabsCount = new JFormattedTextField(formatter);
        txtTabsCount.setValue(Config.getInstance().getResultTabsCount());
        chBoxShowServerCombo = new JCheckBox("Show server drop down list in the toolbar");
        chBoxShowServerCombo.setSelected(Config.getInstance().isShowServerComboBox());
        JLabel lblMaxCharsInResult = new JLabel("Max chars in result");
        txtMaxCharsInResult = new JFormattedTextField(formatter);
        txtMaxCharsInResult.setValue(Config.getInstance().getMaxCharsInResult());
        JLabel lblMaxCharsInTableCell = new JLabel("Max chars in table cell");
        txtMaxCharsInTableCell = new JFormattedTextField(formatter);
        txtMaxCharsInTableCell.setValue(Config.getInstance().getMaxCharsInTableCell());

        JLabel lblMaxFractionDigits = new JLabel("Max number of fraction digits in output");
        formatter = new NumberFormatter();
        formatter.setMinimum(1);
        formatter.setMaximum(20);
        txtMaxFractionDigits = new JFormattedTextField(formatter);
        txtMaxFractionDigits.setValue(Config.getInstance().getInt(Config.MAX_FRACTION_DIGITS));

        JLabel lblEmulatedDoubleClickTimeout = new JLabel("Emulated double-click speed (for copy action), ms");
        formatter = new NumberFormatter();
        formatter.setMinimum(0);
        formatter.setMaximum(2000);
        txtEmulateDoubleClickTimeout = new JFormattedTextField(formatter);
        txtEmulateDoubleClickTimeout.setValue(Config.getInstance().getInt(Config.EMULATED_DOUBLE_CLICK_TIMEOUT));

        JLabel lblCellRightPadding = new JLabel("Right padding in table cell");

        NumberFormat doubleFormat = DecimalFormat.getInstance();
        doubleFormat.setMaximumFractionDigits(1);
        doubleFormat.setRoundingMode(RoundingMode.HALF_UP);
        NumberFormatter doubleFormatter = new NumberFormatter(doubleFormat);
        doubleFormatter.setMinimum(0.0);
        txtCellRightPadding = new JFormattedTextField(doubleFormatter);
        txtCellRightPadding.setValue(Config.getInstance().getDouble(Config.CELL_RIGHT_PADDING));

        JLabel lblCellMaxWidth = new JLabel("Max width of table columns");
        NumberFormatter maxWidthFormatter = new NumberFormatter();
        maxWidthFormatter.setMinimum(10);
        txtCellMaxWidth = new JFormattedTextField(maxWidthFormatter);
        txtCellMaxWidth.setValue(Config.getInstance().getInt(Config.CELL_MAX_WIDTH));

        JLabel lblExecAll = new JLabel ("Execute the script when nothing is selected:");
        comboBoxExecAll = new JComboBox<>(Config.ExecAllOption.values());
        comboBoxExecAll.setSelectedItem(Config.getInstance().getExecAllOption());
        chBoxAutoSave = new JCheckBox("Auto save files");
        chBoxAutoSave.setSelected(Config.getInstance().getBoolean(Config.AUTO_SAVE));
        chBoxSaveOnExit = new JCheckBox("Ask save file on exit");
        chBoxSaveOnExit.setSelected(Config.getInstance().getBoolean(Config.SAVE_ON_EXIT));

        JLabel lblDefaultLineEnding = new JLabel ("Default line ending:");
        comboBoxLineEnding = new JComboBox<>(LineEnding.values());
        comboBoxLineEnding.setSelectedItem(Config.getInstance().getEnum(Config.DEFAULT_LINE_ENDING));

        JLabel lblAuthMechanism = new JLabel("Authentication:");
        JLabel lblUser = new JLabel("  User:");
        JLabel lblPassword = new JLabel("  Password:");

        btnOk = new JButton("OK");
        btnCancel = new JButton("Cancel");

        btnOk.addActionListener(e->accept());
        btnCancel.addActionListener(e->cancel());


        JPanel pnlGeneral = new JPanel();
        GroupLayoutSimple layout = new GroupLayoutSimple(pnlGeneral);
        layout.setStacks(
                new GroupLayoutSimple.Stack()
                        .addLineAndGlue(lblLookAndFeel, comboBoxLookAndFeel)
                        .addLineAndGlue(chBoxShowServerCombo, chBoxAutoSave, chBoxSaveOnExit)
                        .addLine(lblAuthMechanism, comboBoxAuthMechanism, lblUser, txtUser, lblPassword, txtPassword)
        );

        JPanel pnlEditor = new JPanel();
        layout = new GroupLayoutSimple(pnlEditor);
        layout.setStacks(
                new GroupLayoutSimple.Stack()
                        .addLineAndGlue(chBoxRTSAAnimateBracketMatching, chBoxRTSAHighlightCurrentLine, chBoxRTSAWordWrap)
                        .addLineAndGlue(lblDefaultLineEnding, comboBoxLineEnding)
                        .addLineAndGlue(lblExecAll, comboBoxExecAll)
        );

        JPanel pnlResult = new JPanel();
        layout = new GroupLayoutSimple(pnlResult);
        layout.setStacks(
                new GroupLayoutSimple.Stack()
                        .addLineAndGlue(lblMaxFractionDigits, txtMaxFractionDigits)
                        .addLineAndGlue(lblEmulatedDoubleClickTimeout, txtEmulateDoubleClickTimeout)
                        .addLineAndGlue(lblResultTabsCount, txtTabsCount)
                        .addLine(lblMaxCharsInResult, txtMaxCharsInResult, lblMaxCharsInTableCell, txtMaxCharsInTableCell)
                        .addLine(lblCellRightPadding, txtCellRightPadding, lblCellMaxWidth, txtCellMaxWidth)
        );
        layout.linkSize(SwingConstants.HORIZONTAL, lblCellRightPadding, txtMaxFractionDigits, txtEmulateDoubleClickTimeout, txtTabsCount,
                txtMaxCharsInResult, txtMaxCharsInTableCell, txtCellRightPadding, txtCellMaxWidth);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("General", getTabComponent(pnlGeneral));
        tabs.addTab("Editor", getTabComponent(pnlEditor));
        tabs.addTab("Result", getTabComponent(pnlResult));

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlButtons.add(btnOk);
        pnlButtons.add(btnCancel);

        JPanel root = new JPanel(new BorderLayout());
        root.add(tabs, BorderLayout.CENTER);
        root.add(pnlButtons, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JComponent getTabComponent(JComponent panel) {
        Box container = Box.createVerticalBox();
        container.add(panel);
        container.add(Box.createGlue());
        container.setBackground(panel.getBackground());
        container.setOpaque(true);
        return new JScrollPane(container);
    }

    private static class LookAndFeels {
        private Map<String, CustomiszedLookAndFeelInfo> mapLookAndFeels;

        public LookAndFeels() {
            mapLookAndFeels = new HashMap<>();
            for (UIManager.LookAndFeelInfo lf: UIManager.getInstalledLookAndFeels()) {
                mapLookAndFeels.put(lf.getClassName(), new CustomiszedLookAndFeelInfo(lf));
            }
        }
        public CustomiszedLookAndFeelInfo[] getLookAndFeels() {
            return mapLookAndFeels.values().toArray(new CustomiszedLookAndFeelInfo[0]);
        }
        public CustomiszedLookAndFeelInfo getLookAndFeel(String className) {
            return mapLookAndFeels.get(className);
        }
    }

    private static class CustomiszedLookAndFeelInfo extends UIManager.LookAndFeelInfo {
        public CustomiszedLookAndFeelInfo(UIManager.LookAndFeelInfo lfInfo) {
            super(lfInfo.getName(), lfInfo.getClassName());
        }

        @Override
        public String toString() {
            return getName();
        }
    }
}
