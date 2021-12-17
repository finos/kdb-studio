package studio.ui;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.fife.ui.rtextarea.SearchResult;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.event.KeyEvent;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class SearchPanel extends JPanel {

    private final JLabel lblReplace;
    private final JButton btnReplace;
    private final JButton btnReplaceAll;
    private JToggleButton tglWholeWord;
    private JToggleButton tglRegex;
    private JToggleButton tglCaseSensitive;
    private JTextField txtFind;
    private JTextField txtReplace;

    private final RSyntaxTextArea textArea;
    private final EditorPane editorPane;

    private enum SearchAction {Find, Replace, ReplaceAll};

    private static final Border ICON_BORDER = BorderFactory.createCompoundBorder(
            BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
            BorderFactory.createEmptyBorder(1,1,1,1)

    );

    private JToggleButton getButton(Icon icon, Icon selectedIcon, String tooltip) {
        JToggleButton button = new JToggleButton(icon);
        button.setSelectedIcon(selectedIcon);
        button.setBorder(ICON_BORDER);
        button.setToolTipText(tooltip);
        button.setFocusable(false);
        return button;
    }

    public SearchPanel(EditorPane editorPane) {
        this.editorPane = editorPane;
        this.textArea = editorPane.getTextArea();

        tglWholeWord = getButton(Util.SEARCH_WHOLE_WORD_SHADED_ICON, Util.SEARCH_WHOLE_WORD_ICON,"Whole word");
        tglRegex = getButton(Util.SEARCH_REGEX_SHADED_ICON, Util.SEARCH_REGEX_ICON, "Regular expression");
        tglCaseSensitive = getButton(Util.SEARCH_CASE_SENSITIVE_SHADED_ICON, Util.SEARCH_CASE_SENSITIVE_ICON, "Case sensitive");

        txtFind = new JTextField();
        txtReplace = new JTextField();

        JLabel lblFind = new JLabel("Find: ");
        lblReplace = new JLabel("Replace: " );

        Action findAction = UserAction.create("Find", e -> find(true));
        Action findBackAction = UserAction.create("Find Back", e -> find(false));
        Action markAllAction = UserAction.create("Mark All", e -> markAll());
        Action replaceAction = UserAction.create("Replace", e -> replace());
        Action replaceAllAction = UserAction.create("Replace All", e -> replaceAll());
        Action closeAction = UserAction.create("Close", e -> close());

        JButton btnFind = new JButton(findAction);
        JButton btnFindBack = new JButton(findBackAction);
        JButton btnMarkAll = new JButton(markAllAction);
        btnReplace = new JButton(replaceAction);
        btnReplaceAll = new JButton(replaceAllAction);
        JButton btnClose = new JButton(closeAction);

        ActionMap am = txtFind.getActionMap();
        InputMap im = txtFind.getInputMap();
        am.put("findAction", findAction);
        am.put("closeAction", closeAction);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0),"findAction");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),"closeAction");

        am = txtReplace.getActionMap();
        im = txtReplace.getInputMap();
        am.put("replaceAction", replaceAction);
        am.put("closeAction", closeAction);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0),"replaceAction");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),"closeAction");

        GroupLayoutSimple layout = new GroupLayoutSimple(this);
        layout.setAutoCreateGaps(false);
        layout.setStacks(
                new GroupLayoutSimple.Stack()
                        .addLine(lblFind)
                        .addLine(lblReplace),
                new GroupLayoutSimple.Stack()
                        .addLine(txtFind)
                        .addLine(txtReplace),
                new GroupLayoutSimple.Stack()
                        .addLine(tglWholeWord, tglRegex, tglCaseSensitive, btnFind, btnFindBack, btnMarkAll, btnClose)
                        .addLine(btnReplace, btnReplaceAll)

        );
    }

    public void setReplaceVisible(boolean visible) {
        lblReplace.setVisible(visible);
        txtReplace.setVisible(visible);
        btnReplace.setVisible(visible);
        btnReplaceAll.setVisible(visible);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            txtFind.requestFocus();
        }
    }

    private SearchContext buildSearchContext() {
        SearchContext context = new SearchContext();
        String text = txtFind.getText();
        context.setSearchFor(text);
        context.setMatchCase(tglCaseSensitive.isSelected());
        context.setRegularExpression(tglRegex.isSelected());
        context.setSearchForward(true);
        context.setWholeWord(tglWholeWord.isSelected());
        context.setMarkAll(false);
        context.setSearchWrap(true);
        return context;
    }

    private void doSearch(SearchContext context, SearchAction action) {
        if (context.isRegularExpression()) {
            try {
                Pattern.compile(context.getSearchFor());
            } catch (PatternSyntaxException e) {
                editorPane.setTemporaryStatus("Error in regular expression: " + e.getMessage());
                return;
            }
        }

        int pos = textArea.getCaretPosition();
        textArea.setSelectionStart(pos);
        textArea.setSelectionEnd(pos);
        SearchResult result;
        if (action == SearchAction.Find) {
            result = SearchEngine.find(textArea, context);
        } else {
            try {
                if (action == SearchAction.Replace) {
                    result = SearchEngine.replace(textArea, context);
                } else { //ReplaceAll
                    result = SearchEngine.replaceAll(textArea, context);
                }
            } catch (IndexOutOfBoundsException e) {
                editorPane.setTemporaryStatus("Error during replacement: " + e.getMessage());
                return;
            }
        }

        String status;
        if (! result.wasFound()) {
            status = "Nothing was found";
        } else if (result.getMarkedCount() > 0) {
            status = "Marked " + result.getMarkedCount() + " occurrence(s)";
        } else if (action == SearchAction.Find) {
            status = "Selected the first occurrence";
        } else {
            status = "Replaced " + result.getCount() + " occurrence(s)";
        }
        editorPane.setTemporaryStatus(status);
    }

    private void find(boolean forward) {
        SearchContext context = buildSearchContext();
        context.setSearchForward(forward);
        doSearch(context, SearchAction.Find);
    }

    private void markAll() {
        SearchContext context = buildSearchContext();
        context.setMarkAll(true);
        doSearch(context, SearchAction.Find);
    }

    private void replace()  {
        SearchContext context = buildSearchContext();
        context.setReplaceWith(txtReplace.getText());
        doSearch(context, SearchAction.Replace);
    }

    private void replaceAll() {
        SearchContext context = buildSearchContext();
        context.setReplaceWith(txtReplace.getText());
        doSearch(context, SearchAction.ReplaceAll);
    }

    private void close() {
        editorPane.hideSearchPanel();
        editorPane.getTextArea().requestFocus();
    }
}
