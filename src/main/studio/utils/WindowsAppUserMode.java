package studio.utils;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.WString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import studio.kdb.Config;
import studio.kdb.Lm;
import studio.ui.Util;

public class WindowsAppUserMode {
    private static final Logger log = LogManager.getLogger();

    private final static boolean initialized = init();

    private final static String mainID = "kdbStudioAppID" + Config.getEnvironment() + Lm.version;
    private final static String chartID = mainID + "Chart";

    private static boolean init() {
        if (! Util.WINDOWS) return false;

        try {
            Native.register("shell32");
            return true;
        } catch (Throwable e) {
            log.error("Failed to initialized shell32 library",e);
            e.printStackTrace();
            return false;
        }

    }

    private static void setAppUserModelID(final String appID){
        if (!initialized) return;

        try {
            long result = SetCurrentProcessExplicitAppUserModelID(new WString(appID)).longValue();
            if (result != 0) {
                log.error("SetCurrentProcessExplicitAppUserModelID with argument {} return non zero result: {}", appID, result);
            }
        } catch (Throwable e) {
            log.error("SetCurrentProcessExplicitAppUserModelID with argument {} throws error", appID, e);
        }
    }

    public static void setMainId() {
        setAppUserModelID(mainID);
    }

    public static void setChartId() {
        setAppUserModelID(chartID);
    }

    private static native NativeLong SetCurrentProcessExplicitAppUserModelID(WString appID);

}
