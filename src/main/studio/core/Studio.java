package studio.core;

import java.awt.*;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.io.IoBuilder;
import studio.kdb.Config;
import studio.kdb.Lm;
import studio.kdb.Server;
import studio.kdb.Workspace;
import studio.ui.StudioPanel;
import studio.ui.action.WorkspaceSaver;
import studio.utils.Content;
import studio.utils.FileReaderWriter;
import studio.utils.FileWatcher;
import studio.utils.WindowsAppUserMode;

import java.util.TimeZone;
import javax.swing.*;

public class Studio {

    private static final Logger log = LogManager.getLogger();

    private static void initLogger() {
        String env = System.getProperty("env");
        if (env != null) {
            log.info("Set environment to {}", env);
            System.setProperty("log4j.studio.envSuffix", "/" + env);
            ((org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false)).reconfigure();
            Config.setEnvironment(env);
        }

        PrintStream stdoutStream = IoBuilder.forLogger("stdout").setLevel(Level.INFO).buildPrintStream();
        PrintStream stderrStream = IoBuilder.forLogger("stderr").setLevel(Level.ERROR).buildPrintStream();
        System.setOut(stdoutStream);
        System.setErr(stderrStream);
    }

    public static void init0() {
        //called from main and integration tests
        studio.ui.I18n.setLocale(Locale.getDefault());
    }

    public static StudioPanel createPanel(String[] args) {
        //called from main and integration tests
        StudioPanel result = null;
        if (args.length > 0) {
            result = new StudioPanel();
            result.addTab(getInitServer(), args[0]);
        } else {
            Workspace workspace = Config.getInstance().loadWorkspace();
            // Reload files from disk if it was modified somewhere else
            for (Workspace.Window window: workspace.getWindows()) {
                for (Workspace.Tab tab: window.getTabs()) {
                    if (tab.getFilename() != null && !tab.isModified()) {
                        try {
                            Content content = FileReaderWriter.read(tab.getFilename());
                            tab.addContent(content);
                        } catch(IOException e) {
                            log.error("Can't load file " + tab.getFilename() + " from disk", e);
                            tab.setModified(true);
                        }
                    }
                }
            }


            if (workspace.getWindows().length == 0) {
                String[] mruFiles = Config.getInstance().getMRUFiles();
                String filename = mruFiles.length == 0 ? null : mruFiles[0];
                result = new StudioPanel();
                result.addTab(getInitServer(), filename);
            } else {
                StudioPanel.loadWorkspace(workspace);
            }
        }
        return result;
    }

    public static void main(final String[] args) {
        initLogger();
        WindowsAppUserMode.setMainId();

        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        if(System.getProperty("os.name","").contains("OS X")){ 
            System.setProperty("apple.laf.useScreenMenuBar","true");
            //     System.setProperty("apple.awt.brushMetalLook", "true");
            System.setProperty("apple.awt.showGrowBox","true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name","Studio for kdb+");
            System.setProperty("com.apple.mrj.application.live-resize","true");
            System.setProperty("com.apple.macos.smallTabs","true");
            System.setProperty("com.apple.mrj.application.growbox.intrudes","false");
        }

        if(Config.getInstance().getLookAndFeel()!=null){
            try {
                UIManager.setLookAndFeel(Config.getInstance().getLookAndFeel());
            } catch (Exception e) {
                // go on with default one
                log.warn("Can't set LookAndFeel from Config {}", Config.getInstance().getLookAndFeel(), e);
            }
        }

        init0();

        UIManager.put("Table.font",new javax.swing.plaf.FontUIResource("Monospaced",Font.PLAIN,UIManager.getFont("Table.font").getSize()));
        System.setProperty("awt.useSystemAAFontSettings","on");
        System.setProperty("swing.aatext", "true");

        Locale.setDefault(Locale.US);

        SwingUtilities.invokeLater( ()-> init(args) );
    }

    private static Server getInitServer() {
        List<Server> serverHistory = Config.getInstance().getServerHistory();
        return serverHistory.size() == 0 ? null : serverHistory.get(0);
    }

    //Executed on the Event Dispatcher Thread
    private static void init(String[] args) {
        log.info("Start Studio with args {}", Arrays.asList(args));
        FileWatcher.start();

        createPanel(args);

        WorkspaceSaver.init();

        String hash = Lm.getNotesHash();
        if (! Config.getInstance().getNotesHash().equals(hash) ) {
            StudioPanel.getPanels()[0].about();
            Config.getInstance().setNotesHash(hash);
        }
    }
}
