package studio.ui;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import studio.kdb.Config;
import studio.ui.rstextarea.RSTextAreaFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class EditorPane extends JPanel {

    private final RSyntaxTextArea textArea;
    private final MinSizeLabel lblRowCol;
    private final MinSizeLabel lblInsStatus;
    private final JLabel lblStatus;
    private final Box statusBar;

    private final SearchPanel searchPanel;

    private Timer tempStatusTimer = new Timer(3000, this::tempStatusTimerAction);
    private String oldStatus = "";

    private final int yGap;
    private final int xGap;


    public EditorPane(boolean editable) {
        super(new BorderLayout());
        FontMetrics fm = getFontMetrics(UIManager.getFont("Label.font"));
        yGap = Math.round(0.1f * fm.getHeight());
        xGap = Math.round(0.25f * SwingUtilities.computeStringWidth(fm, "x"));

        textArea = RSTextAreaFactory.newTextArea(editable);
        textArea.addCaretListener(e -> updateRowColStatus());
        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateTextModeStatus();
            }
        });

        lblRowCol = new MinSizeLabel("");
        lblRowCol.setHorizontalAlignment(JLabel.CENTER);
        lblRowCol.setMinimumWidth("9999:9999");
        setBorder(lblRowCol);

        lblInsStatus = new MinSizeLabel("INS");
        lblInsStatus.setHorizontalAlignment(JLabel.CENTER);
        lblInsStatus.setMinimumWidth("INS", "OVR");
        setBorder(lblInsStatus);
        lblStatus = new JLabel("Ready");
        Box boxStatus = Box.createHorizontalBox();
        boxStatus.add(lblStatus);
        boxStatus.add(Box.createHorizontalGlue());
        setBorder(boxStatus);

        statusBar = Box.createHorizontalBox();
        statusBar.add(boxStatus);
        statusBar.add(lblInsStatus);
        statusBar.add(lblRowCol);
        statusBar.setVisible(editable);

        Font font = Config.getInstance().getFont(Config.FONT_EDITOR);
        textArea.setFont(font);
        RTextScrollPane scrollPane = new RTextScrollPane(textArea);
        scrollPane.getGutter().setLineNumberFont(font);

        searchPanel = new SearchPanel(this);
        hideSearchPanel();

        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
    }

    public void hideSearchPanel() {
        searchPanel.setVisible(false);
        textArea.setHighlighter(null); // workaround to clear all marks
    }

    public void showSearchPanel(boolean showReplace) {
        searchPanel.setReplaceVisible(showReplace);
        searchPanel.setVisible(true);
    }

    public RSyntaxTextArea getTextArea() {
        return textArea;
    }

    public void setStatus(String status) {
        lblStatus.setText(status);
    }

    public void setTemporaryStatus(String status) {
        if (!tempStatusTimer.isRunning()) {
            oldStatus = lblStatus.getText();
        }
        setStatus(status);
        tempStatusTimer.restart();
    }

    private void tempStatusTimerAction(ActionEvent event) {
        setStatus(oldStatus);
    }

    private void updateRowColStatus() {
        int row = textArea.getCaretLineNumber() + 1;
        int col = textArea.getCaretPosition() - textArea.getLineStartOffsetOfCurrentLine() + 1;
        lblRowCol.setText("" + row + ":" + col);
    }

    private void updateTextModeStatus() {
        String text = textArea.getTextMode() == RSyntaxTextArea.INSERT_MODE ? "INS" : "OVR";
        lblInsStatus.setText(text);
    }

    private void setBorder(JComponent component) {
        component.setBorder(
                BorderFactory.createCompoundBorder(
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createEmptyBorder(yGap,xGap,yGap,xGap),
                            BorderFactory.createLineBorder(Color.LIGHT_GRAY)
                    ),
                    BorderFactory.createEmptyBorder(2*yGap, 2*xGap, yGap, 2*xGap)
                )
        );
    }
}
