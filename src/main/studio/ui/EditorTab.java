package studio.ui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import studio.kdb.Server;
import studio.qeditor.QKit;
import studio.ui.action.QueryExecutor;

import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.Document;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.io.File;

public class EditorTab {

    private static final String TITLE = "title";
    private static final String FILENAME = "filename";
    private static final String SERVER = "server";
    private static final String MODIFIED = "modified";

    private static int scriptNumber = 0;

    private StudioPanel panel;
    private JEditorPane textArea;

    private static final Logger log = LogManager.getLogger();

    public EditorTab(StudioPanel panel) {
        this.panel = panel;
    }

    public JEditorPane init() {
        if (textArea != null) throw new IllegalStateException("The EditorTab has been already initialized");

        textArea = new JEditorPane(QKit.CONTENT_TYPE,"");
        textArea.putClientProperty(QueryExecutor.class, new QueryExecutor(this));
        Document doc = textArea.getDocument();
        doc.putProperty(MODIFIED, false);
        UndoManager um = new UndoManager() {
            public void undoableEditHappened(UndoableEditEvent e) {
                super.undoableEditHappened(e);
                panel.refreshActionState();
            }

            public synchronized void redo() throws CannotRedoException {
                super.redo();
                panel.refreshActionState();
            }

            public synchronized void undo() throws CannotUndoException {
                super.undo();
                panel.refreshActionState();
            }
        };
        doc.putProperty(BaseDocument.UNDO_MANAGER_PROP,um);
        doc.addUndoableEditListener(um);
        return textArea;
    }

    public StudioPanel getPanel() {
        return panel;
    }

    public void setPanel(StudioPanel panel) {
        this.panel = panel;
    }

    public JEditorPane getTextArea() {
        return textArea;
    }

    public String getFilename() {
        if (textArea == null) return null;
        return (String) textArea.getDocument().getProperty(FILENAME);
    }

    public void setFilename(String filename) {
        textArea.getDocument().putProperty(FILENAME, filename);
        String title;
        if (filename == null) {
            title = "Script" + scriptNumber++;
        } else {
            title = new File(filename).getName();
        }
        textArea.getDocument().putProperty(TITLE, title);
        panel.refreshTitle();
    }

    public String getTitle() {
        if (textArea == null) return null;
        return (String) textArea.getDocument().getProperty(TITLE);
    }

    public String getTabTitle() {
        if (getFilename() != null) return getTitle();
        Server server = getServer();
        if (server == null) return getTitle();

        if (server.getName().length() > 0) return server.getName();
        return server.getHost() + ":" + server.getPort();
    }

    public boolean isModified() {
        if (textArea == null) return false;
        return (Boolean)textArea.getDocument().getProperty(MODIFIED);
    }

    public void setModified(boolean value) {
        textArea.getDocument().putProperty(MODIFIED, value);
        panel.refreshTitle();
    }

    public Server getServer() {
        if (textArea == null) return null;
        return (Server)textArea.getDocument().getProperty(SERVER);
    }

    public void setServer(Server server) {
        textArea.getDocument().putProperty(SERVER,server);
        org.netbeans.editor.EditorUI editorUI = Utilities.getEditorUI(textArea);
        if (editorUI == null) {
            log.info("Ups... That wasn't expected. Please send this to an author");
            log.info("textArray.class: " + textArea.getClass());
            log.info("textArray.getUI class: " + textArea.getUI().getClass());
            log.info("textArray text: " + textArea.getText().substring(0, Math.max(30, textArea.getText().length())));
            throw new IllegalStateException("Can't set server to editor");
        }
        editorUI.getComponent().setBackground(server.getBackgroundColor());
    }

    public QueryExecutor getQueryExecutor() {
        return (QueryExecutor) textArea.getClientProperty(QueryExecutor.class);
    }

    public UndoManager getUndoManager() {
        return (UndoManager) textArea.getDocument().getProperty(BaseDocument.UNDO_MANAGER_PROP);
    }

}
