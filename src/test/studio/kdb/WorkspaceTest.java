package studio.kdb;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import studio.core.DefaultAuthenticationMechanism;

import java.awt.*;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class WorkspaceTest {

    private Workspace workspace;
    private Server server;

    @BeforeEach
    public void setup() {
        Server server = new Server("testName","someHost",1111, "", "",
                                    Color.red, "auth", false);
        server.setFolder(new ServerTreeNode("").add("testFolder"));

        workspace = new Workspace();
        Workspace.Window w1 = workspace.addWindow(false);
        Workspace.Window w2 = workspace.addWindow(true);


        w1.addTab(true)
                .addServer(Config.getInstance().getServerByConnectionString("`:server.com:12345:user:password"))
                .addFilename("test.q");

        w2.addTab(false)
                .addServer(server)
                .addContent("Test content");

        w2.addTab(true)
                .addServer(server)
                .addFilename("test1.q")
                .addContent("Test content");

        w2.addTab(false)
                .addContent("another content");
    }

    @Test
    public void testGetter() {
        assertEquals(1, workspace.getSelectedWindow());
        Workspace.Window[] windows = workspace.getWindows();
        assertEquals(2, windows.length);

        assertEquals(0, windows[0].getSelectedTab());
        assertEquals(1, windows[1].getSelectedTab());

        Workspace.Tab[] tabs = windows[0].getTabs();
        assertEquals(1, tabs.length);
        assertNull(tabs[0].getContent());
        assertEquals("test.q", tabs[0].getFilename());
        assertNull(tabs[0].getServerFullName());
        assertNotNull(tabs[0].getServerConnection());
        assertNull(tabs[0].getContent());
        assertEquals(DefaultAuthenticationMechanism.NAME, tabs[0].getServerAuth());
        assertFalse(tabs[0].isModified());

        tabs = windows[1].getTabs();
        assertEquals(3, tabs.length);

        assertNotNull(tabs[0].getContent());
        assertNull(tabs[0].getFilename());
        assertEquals("testFolder/testName", tabs[0].getServerFullName());
        assertEquals("auth", tabs[0].getServerAuth());
        assertTrue(tabs[0].isModified());

        assertNotNull(tabs[2].getContent());
        assertNull(tabs[2].getFilename());
        assertNull(tabs[2].getServerFullName());
        assertNull(tabs[2].getServerConnection());
        assertTrue(tabs[2].isModified());
    }

    @Test
    public void testSave() {
        Properties p = new Properties();
        workspace.save(p);

        workspace = new Workspace();
        workspace.load(p);
        testGetter();
    }
}
