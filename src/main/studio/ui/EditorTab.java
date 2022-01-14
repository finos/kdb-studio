package studio.ui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import studio.kdb.Server;
import studio.ui.action.QueryExecutor;
import studio.utils.Content;
import studio.utils.FileReaderWriter;
import studio.utils.FileWatcher;
import studio.utils.LineEnding;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public class EditorTab implements FileWatcher.Listener {

    private String title;
    private String filename;
    private Server server;
    private boolean modified = false;
    private LineEnding lineEnding = LineEnding.Unix;

    private static int scriptNumber = 0;

    private StudioPanel panel;
    private EditorPane editorPane;

    private long modifiedTimeOnDisk = 0;
    private boolean watchFile = true;

    private static final Logger log = LogManager.getLogger();

    public EditorTab(StudioPanel panel) {
        this.panel = panel;
        init();
    }

    private void init() {
        if (editorPane != null) throw new IllegalStateException("The EditorTab has been already initialized");

        editorPane = new EditorPane(true);
        JTextComponent textArea = editorPane.getTextArea();

        textArea.putClientProperty(QueryExecutor.class, new QueryExecutor(this));
    }

    public void init(Content content) {
        setContent(content);
        RSyntaxTextArea textArea = getTextArea();
        textArea.discardAllEdits();
        textArea.requestFocus();
    }

    private void setContent(Content content) {
        try {
            RSyntaxTextArea textArea = getTextArea();
            Document doc = textArea.getDocument();
            int caretPosition = textArea.getCaretPosition();
            doc.remove(0, textArea.getDocument().getLength());
            doc.insertString(0, content.getContent(),null);
            textArea.setCaretPosition(Math.min(caretPosition, doc.getLength()));
            setLineEnding(content.getLineEnding());
            setModified(content.hasMixedLineEnding());
            modifiedTimeOnDisk = readModifiedTime();
            StudioPanel.rebuildAll();
        }
        catch (BadLocationException e) {
            log.error("Unexpected exception", e);
        }
    }

    public StudioPanel getPanel() {
        return panel;
    }

    public void setPanel(StudioPanel panel) {
        this.panel = panel;
    }

    public EditorPane getPane() {
        return editorPane;
    }

    public RSyntaxTextArea getTextArea() {
        return editorPane.getTextArea();
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        if (Objects.equals(this.filename, filename)) return;
        stopFileWatching();

        this.filename = filename;
        title = getTitleFromFilename();
        panel.refreshTitle();

        startFileWatching();
    }

    private String getTitleFromFilename() {
        if (filename == null) {
            return "Script" + scriptNumber++;
        } else {
            return new File(filename).getName();
        }

    }

    public String getTitle() {
        if (title == null) {
            title = getTitleFromFilename();
        }
        return title;
    }

    private String getTabTitleInternal() {
        if (getFilename() != null) return getTitle();
        Server server = getServer();
        if (server == null) return getTitle();

        if (server.getName().length() > 0) return server.getName();
        return server.getHost() + ":" + server.getPort();
    }

    public String getTabTitle() {
        String title = getTabTitleInternal();

        if (getFilename() == null) return title;
        if (isModified()) title = title + " *";
        return title;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean value) {
        if (modified == value) return;
        if (! value) {
            watchFile = true;
        }
        modified = value;
        panel.refreshTitle();
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
        getTextArea().setBackground(server.getBackgroundColor());
        if (server.equals(getServer())) return;

        setStatus("Changed server: " + server.getDescription(true));
    }

    public LineEnding getLineEnding() {
        return lineEnding;
    }

    public void setLineEnding(LineEnding lineEnding) {
        if (this.lineEnding == lineEnding) return;

        this.lineEnding = lineEnding;
        setModified(true);
    }

    public void setStatus(String status) {
        editorPane.setStatus(status);
    }

    public QueryExecutor getQueryExecutor() {
        return (QueryExecutor) getTextArea().getClientProperty(QueryExecutor.class);
    }

    public void stopFileWatching() {
        if (filename == null) return;
        FileWatcher.removeListener(this);
    }

    public void startFileWatching() {
        if (filename == null) return;
        FileWatcher.addListener(new File(filename).toPath(), this);
    }

    private long readModifiedTime() {
        if (filename == null) return 0;

        return new File(filename).lastModified();
    }

    // returns true if saved, false if error or cancelled
    public boolean saveFileOnDisk(boolean autoSave) {
        if (filename == null) return false;

        if (autoSave && modifiedTimeOnDisk != readModifiedTime()) return false;

        try {
            FileReaderWriter.write(filename, getTextArea().getText(), lineEnding);
            StudioPanel.rebuildAll();
            setModified(false);
            modifiedTimeOnDisk = readModifiedTime();
            panel.addToMruFiles(filename);
            return true;
        }
        catch (IOException e) {
            log.error("Error during saving file " + filename, e);
            editorPane.setTemporaryStatus("Error during saving file " + filename);
        }

        return false;
    }

    @Override
    public void fileModified(Path path) {
        long nowModifiedTimeOnDisk = readModifiedTime();
        if (modifiedTimeOnDisk == nowModifiedTimeOnDisk) return;

        if (!watchFile) return;

        if (nowModifiedTimeOnDisk == 0) {
            setModified(true);
            editorPane.setTemporaryStatus("File " + filename + " was removed on disk.");
            return;
        }

        if (modified) {
            watchFile = false; // prevent to show new dialogs until this one is answered
            int result = StudioOptionPane.reloadFileDialog(editorPane, "File " + filename +
                                " was modified on disk.\nReload and override local changes?\n\n" +
                    "If you select Cancel, then you will be notified about file modifications again.\n\n" +
                    "However if you select Ignore All, there will be no future notifications\n" +
                    "about file modifications until the file is saved in the Studio.", "Reload");

            if (result != StudioOptionPane.IGNOREALL_RESULT) {
                watchFile = true;
            }
            modifiedTimeOnDisk = readModifiedTime();

            if (result != StudioOptionPane.RELOAD_RESULT) {
                return;
            }
        }

        try {
            Content content = FileReaderWriter.read(filename);
            if (content.hasMixedLineEnding()) {
                log.warn("{} has mixing line ending. Line ending will be update to {}", filename, content.getLineEnding());
            }
            setContent(content);
            editorPane.setTemporaryStatus("Reloaded: " + filename);
        } catch (IOException e) {
            log.error("Can't reload {} with error {}", filename, e.getMessage());
            editorPane.setTemporaryStatus("Reload of " + filename + " failed");
        }
    }
}
