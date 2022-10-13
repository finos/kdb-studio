package studio.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.io.CharArrayWriter;
import java.io.PrintWriter;

public class Util {
    private final static String IMAGE_BASE = "/images/";

    public final static ImageIcon LOGO_ICON = getImage(IMAGE_BASE + "logo_32.png");
    public final static ImageIcon BLANK_ICON = getImage(IMAGE_BASE + "blank.png");
    public final static ImageIcon QUESTION_ICON = getImage(IMAGE_BASE + "question_32.png");
    public final static ImageIcon INFORMATION_ICON = getImage(IMAGE_BASE + "information_32.png");
    public final static ImageIcon WARNING_ICON = getImage(IMAGE_BASE + "warning_32.png");
    public final static ImageIcon ERROR_ICON = getImage(IMAGE_BASE + "error_32.png");
    public final static ImageIcon ERROR_SMALL_ICON = getImage(IMAGE_BASE + "error.png");
    public final static ImageIcon CHECK_ICON = getImage(IMAGE_BASE + "check.png");
    public final static ImageIcon CLOSE_ONE_ICON = getImage(IMAGE_BASE + "close_one.png");
    public final static ImageIcon CLOSE_ALL_ICON = getImage(IMAGE_BASE + "close_all.png");
    public final static ImageIcon SETTINGS_ICON = getImage(IMAGE_BASE + "settings.png");
    public final static ImageIcon UNDO_ICON = getImage(IMAGE_BASE + "undo.png");
    public final static ImageIcon REDO_ICON =getImage(IMAGE_BASE + "redo.png");
    public final static ImageIcon COPY_ICON = getImage(IMAGE_BASE + "copy.png");
    public final static ImageIcon CUT_ICON = getImage(IMAGE_BASE + "cut.png");
    public final static ImageIcon PASTE_ICON = getImage(IMAGE_BASE + "paste.png");
    public final static ImageIcon NEW_DOCUMENT_ICON = getImage(IMAGE_BASE + "document_new.png");
    public final static ImageIcon FIND_ICON = getImage(IMAGE_BASE + "find.png");
    public final static ImageIcon REPLACE_ICON = getImage(IMAGE_BASE + "replace.png");
    public final static ImageIcon OPEN_ICON = getImage(IMAGE_BASE + "open.png");
    public final static ImageIcon SERVER_TREE_ICON = getImage(IMAGE_BASE + "server_tree.png");
    public final static ImageIcon SERVER_EDIT_ICON = getImage(IMAGE_BASE + "server_edit.png");
    public final static ImageIcon ADD_SERVER_ICON = getImage(IMAGE_BASE + "server_add.png");
    public final static ImageIcon DELETE_SERVER_ICON = getImage(IMAGE_BASE + "server_delete.png");
    public final static ImageIcon SAVE_ICON = getImage(IMAGE_BASE + "save.png");
    public final static ImageIcon SAVE_AS_ICON = getImage(IMAGE_BASE + "save_as.png");
    public final static ImageIcon EXPORT_ICON = getImage(IMAGE_BASE + "export.png");
    public final static ImageIcon CHART_ICON = getImage(IMAGE_BASE + "chart.png");
    public final static ImageIcon STOP_ICON = getImage(IMAGE_BASE + "stop.png");
    public final static ImageIcon EXCEL_ICON = getImage(IMAGE_BASE + "excel.png");
    public final static ImageIcon EXECUTE_ICON = getImage(IMAGE_BASE + "execute.png");
    public final static ImageIcon EXECUTE_LINE_ICON = getImage(IMAGE_BASE + "execute_line.png");
    public final static ImageIcon REFRESH_ICON = getImage(IMAGE_BASE + "refresh.png");
    public final static ImageIcon ABOUT_ICON = getImage(IMAGE_BASE + "about.png");
    public final static ImageIcon SERVER_LIST_ICON = getImage(IMAGE_BASE + "server_list.png");
    public final static ImageIcon CODE_KX_COM_ICON = getImage(IMAGE_BASE + "code_kx_com.png");
    public final static ImageIcon TABLE_ICON = getImage(IMAGE_BASE + "table.png");
    public final static ImageIcon CONSOLE_ICON = getImage(IMAGE_BASE + "console.png");
    public final static ImageIcon SERVER_CLONE_ICON = getImage(IMAGE_BASE + "server_clone.png");
    public final static ImageIcon CHART_BIG_ICON = Util.getImage(IMAGE_BASE + "chart_24.png");
    public final static ImageIcon COLUMN_ICON = getImage(IMAGE_BASE +"column.png");
    public final static ImageIcon SORT_ASC_ICON = getImage(IMAGE_BASE + "sort_ascending.png");
    public final static ImageIcon SORT_AZ_ASC_ICON = getImage(IMAGE_BASE + "sort_az_ascending.png");
    public final static ImageIcon SORT_DESC_ICON = getImage(IMAGE_BASE + "sort_descending.png");
    public final static ImageIcon SORT_AZ_DESC_ICON = Util.getImage(IMAGE_BASE + "sort_az_descending.png");
    public final static ImageIcon MAXIMIZE_EDITOR_ICON = Util.getImage(IMAGE_BASE + "maximize_editor.png");
    public final static ImageIcon DIVIDER_ORIENTATION_ICON = Util.getImage(IMAGE_BASE + "divider_orientation.png");
    public final static ImageIcon NEW_WINDOW_ICON = Util.getImage(IMAGE_BASE + "new_window.png");
    public final static ImageIcon ARRANGE_WINDOWS_ICON = Util.getImage(IMAGE_BASE + "arrange_windows.png");

    public static boolean MAC_OS_X = (System.getProperty("os.name").toLowerCase().startsWith("mac os x"));

    public static ImageIcon getImage(String strFilename) {
        if (!strFilename.startsWith("/")) {
            strFilename = "/toolbarButtonGraphics/" + strFilename;
        }

        URL url = Util.class.getResource(strFilename);
        if (url == null) {
            System.err.println("unable to get resource: " + strFilename);
            return null;
        }

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Image image = toolkit.getImage(url);
        return new ImageIcon(image);
    }

    public static void centerChildOnParent(Component child,Component parent) {
        Point parentlocation = parent.getLocation();
        Dimension oursize = child.getPreferredSize();
        Dimension parentsize = parent.getSize();

        int x = parentlocation.x + (parentsize.width - oursize.width) / 2;
        int y = parentlocation.y + (parentsize.height - oursize.height) / 2;

        x = Math.max(0,x);  // keep the corner on the screen
        y = Math.max(0,y);  //

        child.setLocation(x,y);
    }

    public static String getAcceleratorString(KeyStroke keyStroke) {
        return KeyEvent.getKeyModifiersText(keyStroke.getModifiers()) + (MAC_OS_X ? "": "+") +
                KeyEvent.getKeyText(keyStroke.getKeyCode());
    }

    public static String extractStackTrace(Throwable e) {
        CharArrayWriter caw = new CharArrayWriter();
        e.printStackTrace(new PrintWriter(caw));
        return caw.toString();
    }

}
