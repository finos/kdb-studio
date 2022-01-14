package studio.ui.action;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import studio.kdb.Config;
import studio.kdb.Workspace;
import studio.ui.StudioPanel;

import javax.swing.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WorkspaceSaver {

    private final static int PERIOD_IN_SEC = 10;

    private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static boolean enabled = true;
    private static Workspace workspace;

    private final static Logger log = LogManager.getLogger();

    public static void init () {
        scheduler.scheduleAtFixedRate(WorkspaceSaver::timer, PERIOD_IN_SEC, PERIOD_IN_SEC, TimeUnit.SECONDS);
    }

    public synchronized static void setEnabled(boolean value) {
        enabled = value;
    }

    private static void timer() {
        try {
            SwingUtilities.invokeAndWait(() -> setWorkspace(StudioPanel.getWorkspace()));
            save(workspace);
        } catch (Exception e) {
            log.error("Exception during getting workspace", e);
        }
    }

    public static synchronized void save(Workspace workspace) {
        if (!enabled) return;
        Config.getInstance().saveWorkspace(workspace);
    }

    private static synchronized void setWorkspace(Workspace w) {
        workspace = w;
    }

}
