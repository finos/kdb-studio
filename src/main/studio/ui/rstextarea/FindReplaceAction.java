package studio.ui.rstextarea;

import studio.ui.EditorPane;

import java.awt.event.ActionEvent;

public class FindReplaceAction extends EditorPaneAction {

    public static final String findAction = "kdbStudio.FindAction";
    public static final String replaceAction = "kdbStudio.ReplaceAction";

    private final boolean replace;

    public FindReplaceAction(boolean replace) {
        super(replace ? replaceAction : findAction);
        this.replace = replace;
    }

    @Override
    protected void actionPerformed(ActionEvent e, EditorPane pane) {
        if (replace && !pane.getTextArea().isEditable()) return;
        pane.showSearchPanel(replace);
    }
}
