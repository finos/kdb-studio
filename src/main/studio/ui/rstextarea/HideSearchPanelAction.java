package studio.ui.rstextarea;

import studio.ui.EditorPane;

import java.awt.event.ActionEvent;

public class HideSearchPanelAction extends EditorPaneAction {

    public static final String action = "kdbStudio.HideSearchPanelAction";

    public HideSearchPanelAction() {
        super(action);
    }

    @Override
    protected void actionPerformed(ActionEvent e, EditorPane pane) {
        pane.hideSearchPanel();
    }
}
