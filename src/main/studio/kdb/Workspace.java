package studio.kdb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import studio.utils.Content;
import studio.utils.LineEnding;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Workspace {

    private final List<Window> windows = new ArrayList<>();
    private int selectedWindow = -1;

    private final static String SELECTED_WINDOW = "selectedWindow";
    private final static String WINDOW = "window";
    private final static String TAB = "tab";
    private final static String SELECTED_TAB = "selectedTab";
    private final static String FILENAME = "filename";
    private final static String SERVER_FULLNAME = "serverFullname";
    private final static String SERVER_CONNECTION = "serverConnection";
    private final static String SERVER_AUTH = "serverAuth";
    private final static String CONTENT = "content";
    private final static String MODIFIED = "modified";
    private final static String LINE_ENDING = "lineEnding";
    private final static String CARET = "caret";


    private final static Logger log = LogManager.getLogger();

    public int getSelectedWindow() {
        return selectedWindow;
    }

    public Window[] getWindows() {
        return windows.toArray(new Window[0]);
    }

    public void save(Properties p) {
        p.setProperty(SELECTED_WINDOW, "" + selectedWindow);
        for (int index = 0; index<windows.size(); index++) {
            Window window = windows.get(index);
            window.save(WINDOW + "." + index + ".", p);
        }
    }

    private static int getInt(Properties p, String key, int defValue) {
        try {
            return Integer.parseInt(p.getProperty(key, "" + defValue));
        } catch (NumberFormatException e) {
            return  defValue;
        }
    }

    public void load(Properties p) {
        windows.clear();
        for (int index = 0; ; index++) {
            Workspace.Window window = new Workspace.Window();
            window.load(WINDOW + "." + index + ".", p);
            if (window.getTabs().length == 0) break;
            windows.add(window);
        }
        selectedWindow = getInt(p, SELECTED_WINDOW, -1);
    }

    public Window addWindow(boolean selected) {
        Window window = new Window();
        windows.add(window);
        if (selected) {
            selectedWindow = windows.size()-1;
        }
        return window;
    }

    public static class Window {

        private final List<Tab> tabs = new ArrayList<>();
        private int selectedTab = -1;

        public int getSelectedTab() {
            return selectedTab;
        }

        public Tab[] getTabs() {
            return tabs.toArray(new Tab[0]);
        }

        public Tab addTab(boolean selected) {
            Tab tab = new Tab();
            tabs.add(tab);
            if (selected) {
                selectedTab = tabs.size()-1;
            }
            return tab;
        }

        private void save(String prefix, Properties p) {
            p.setProperty(prefix + SELECTED_TAB, "" + selectedTab);
            for (int index = 0; index<tabs.size(); index++) {
                Tab tab = tabs.get(index);
                tab.save(prefix + TAB + "." + index + ".", p);
            }
        }

        private void load(String prefix, Properties p) {
            tabs.clear();
            for (int index = 0; ; index++) {
                Workspace.Tab tab = new Workspace.Tab();
                tab.load(prefix + TAB + "." + index + ".", p);
                if (tab.getFilename() == null &&
                    tab.getContent() == null &&
                    tab.getServerFullName() == null &&
                    tab.getServerConnection() == null) break;
                tabs.add(tab);
            }
            selectedTab = getInt(p, prefix + SELECTED_TAB, -1);
        }
    }

    public static class Tab {
        private String filename = null;
        private String serverFullName = null;
        private String serverConnection = null;
        private String serverAuth = null;
        private String content = null;
        private boolean modified = false;
        private LineEnding lineEnding = LineEnding.Unix;
        private int caret = 0;

        public String getFilename() {
            return filename;
        }

        public String getServerFullName() {
            return serverFullName;
        }

        public String getServerConnection() {
            return serverConnection;
        }

        public String getServerAuth() {
            return serverAuth;
        }

        public String getContent() {
            return content;
        }

        public boolean isModified() {
            return modified;
        }

        public LineEnding getLineEnding() {
            return lineEnding;
        }

        public int getCaret() {
            return caret;
        }

        public Tab addFilename(String filename) {
            this.filename = filename;
            return this;
        }

        public Tab addServer(Server server) {
            if (server == null) return this;

            if (server.getFolder() == null) {
                serverFullName = null;
            } else {
                serverFullName = server.getFullName();
            }
            serverConnection = server.getConnectionStringWithPwd();
            serverAuth = server.getAuthenticationMechanism();
            return this;
        }

        public Tab addContent(String content) {
            modified = true;
            this.content = content;
            return this;
        }

        public Tab addContent(Content content) {
            return addContent(content.getContent())
                    .setLineEnding(content.getLineEnding())
                    .setModified(content.hasMixedLineEnding());
        }

        public Tab setModified(boolean modified) {
            this.modified = modified;
            return this;
        }

        public Tab setLineEnding(LineEnding lineEnding) {
            this.lineEnding = lineEnding;
            return this;
        }

        public Tab setCaret(int caret) {
            this.caret = caret;
            return this;
        }

        private void save(String prefix, Properties p) {
            if (filename != null) p.setProperty(prefix + FILENAME, filename);
            if (serverFullName != null) p.setProperty(prefix + SERVER_FULLNAME, serverFullName);
            if (serverConnection != null) p.setProperty(prefix + SERVER_CONNECTION, serverConnection);
            if (serverAuth != null) p.setProperty(prefix + SERVER_AUTH, serverAuth);
            if (content != null) p.setProperty(prefix + CONTENT, content);
            p.setProperty(prefix + MODIFIED, Boolean.toString(modified));
            p.setProperty(prefix + LINE_ENDING, lineEnding.toString());
            p.setProperty(prefix + CARET, Integer.toString(caret));
        }

        private void load(String prefix, Properties p) {
            filename = p.getProperty(prefix + FILENAME);
            serverFullName = p.getProperty(prefix + SERVER_FULLNAME);
            serverConnection = p.getProperty(prefix + SERVER_CONNECTION);
            serverAuth = p.getProperty(prefix + SERVER_AUTH);
            content = p.getProperty(prefix + CONTENT);
            modified = Boolean.parseBoolean(p.getProperty(prefix + MODIFIED, "false"));

            try {
                lineEnding = LineEnding.valueOf(p.getProperty(prefix + LINE_ENDING, "Unix"));
            } catch (IllegalArgumentException e) {
                log.error("Failed to parse {} of key {}", p.getProperty(prefix + LINE_ENDING, "Unix"), prefix + LINE_ENDING, e);
            }

            try {
                caret = Integer.parseInt(p.getProperty(prefix + CARET, "0"));
            } catch (NumberFormatException e) {
                log.error("Failed to parse {} of key {}", p.getProperty(prefix + CARET, "0"), prefix + CARET, e);
            }
        }
    }
}
