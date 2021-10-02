package studio.kdb;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import studio.core.Credentials;
import studio.core.DefaultAuthenticationMechanism;
import studio.utils.TableConnExtractor;

import java.awt.*;
import java.io.*;
import java.util.Collection;
import java.util.Properties;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigTest {

    private Config config;
    private File tmpFile;
    private Server server;

    @BeforeEach
    public void init() throws IOException {
        tmpFile = File.createTempFile("studioforkdb", ".tmp");
        tmpFile.deleteOnExit();
        config = Config.getByFilename(tmpFile.getPath());
        System.out.println("temp file " + tmpFile.getPath());

        server = new Server("testServer", "localhost",1111,
                "user", "pwd", Color.WHITE, DefaultAuthenticationMechanism.NAME, false);
        server.setFolder(config.getServerTree().add("testFolder"));
    }

    @Test
    public void addServerDifferentTreeNode() {
        Server server1 = new Server("testServer1", "localhost",1112,
                "user", "pwd", Color.WHITE, DefaultAuthenticationMechanism.NAME, false);
        server1.setFolder(new ServerTreeNode().add("addServerTestFolder"));

        Server server2 = new Server("testServer2", "localhost",1113,
                "user", "pwd", Color.WHITE, DefaultAuthenticationMechanism.NAME, false);
        server2.setFolder(new ServerTreeNode().add("addServerTestFolder"));

        config.addServers(false, server1, server2);
        assertEquals(2, config.getServerNames().size());
        assertEquals(2, config.getServerTree().getChild("addServerTestFolder").getChildCount() );
    }

    @Test
    public void addServerSameName() {
        Server server1 = new Server("testServer1", "localhost",1112,
                "user", "pwd", Color.WHITE, DefaultAuthenticationMechanism.NAME, false);
        server1.setFolder(config.getServerTree().add("sameNameTestFolder"));

        Server server2 = new Server("testServer1", "localhost",1113,
                "user", "pwd", Color.WHITE, DefaultAuthenticationMechanism.NAME, false);
        server2.setFolder(config.getServerTree().add("sameNameTestFolder"));

        config.addServers(false, server1);
        assertThrows(IllegalArgumentException.class, ()->config.addServer(server2) );

        assertEquals(1, config.getServerNames().size());
        assertEquals(1, config.getServerTree().getChild("sameNameTestFolder").getChildCount() );
        assertEquals(server1.getPort(), config.getServer("sameNameTestFolder/testServer1").getPort());
    }

    @Test
    public void testDifferentConfigs() throws IOException{
        Config config1 = config;
        init();

        int value = config1.getResultTabsCount();
        assertEquals(value, config.getResultTabsCount());

        config.setResultTabsCount(value+1);
        assertEquals(value+1, config.getResultTabsCount());
        assertEquals(value, config1.getResultTabsCount());

        assertEquals(value+1, Config.getByFilename(tmpFile.getPath()).getResultTabsCount());
    }

    @Test
    public void testServerHistoryDepth() {
        int depth = config.getServerHistoryDepth();
        config.setServerHistoryDepth(depth+1);
        assertEquals(depth+1, config.getServerHistoryDepth());
    }

    @Test
    public void testServerHistory() {
        assertEquals(0, config.getServerHistory().size());

        config.addServer(server);
        assertEquals(0, config.getServerHistory().size());
        config.addServerToHistory(server);
        assertEquals(1, config.getServerHistory().size());
        assertEquals(server, config.getServerHistory().get(0));
        config.addServerToHistory(server);
        assertEquals(1, config.getServerHistory().size());


        Server server1 = new Server(server);
        server1.setName("testServer1");
        Server server2 = new Server(server);
        server2.setName("testServer2");
        Server server3 = new Server(server);
        server3.setName("testServer3");

        config.addServers(false, server1, server2, server3);
        config.addServerToHistory(server1);
        config.addServerToHistory(server2);
        assertEquals(3, config.getServerHistory().size());
        assertEquals(server2, config.getServerHistory().get(0));
        assertEquals(server1, config.getServerHistory().get(1));
        assertEquals(server, config.getServerHistory().get(2));

        config.addServerToHistory(server1);
        assertEquals(3, config.getServerHistory().size());
        assertEquals(server1, config.getServerHistory().get(0));
        assertEquals(server2, config.getServerHistory().get(1));
        assertEquals(server, config.getServerHistory().get(2));

        config.addServerToHistory(server);
        assertEquals(3, config.getServerHistory().size());
        assertEquals(server, config.getServerHistory().get(0));
        assertEquals(server1, config.getServerHistory().get(1));
        assertEquals(server2, config.getServerHistory().get(2));

        config.addServerToHistory(server);
        assertEquals(3, config.getServerHistory().size());
        assertEquals(server, config.getServerHistory().get(0));
        assertEquals(server1, config.getServerHistory().get(1));
        assertEquals(server2, config.getServerHistory().get(2));

        config.setServerHistoryDepth(3);
        config.addServerToHistory(server3);
        assertEquals(3, config.getServerHistory().size());
        assertEquals(server3, config.getServerHistory().get(0));
        assertEquals(server, config.getServerHistory().get(1));
        assertEquals(server1, config.getServerHistory().get(2));
    }

    private Config getConfig(Properties properties) throws IOException {
        File newFile = File.createTempFile("studioforkdb", ".tmp");
        newFile.deleteOnExit();
        OutputStream out = new FileOutputStream(newFile);
        properties.store(out, null);
        out.close();

        return Config.getByFilename(newFile.getPath());
    } 
    
    private Config copyConfig(Config config, Consumer<Properties> propsModification) throws IOException {
        Properties p = new Properties();
        p.load(new FileInputStream(config.getFilename()));
        propsModification.accept(p);
        return getConfig(p);
    }

    @Test
    public void upgrade13Test() throws IOException {
        config.addServer(server);

        Config newConfig = copyConfig(config, p -> {
            p.setProperty("version", "1.2");
        });
        assertEquals(0, newConfig.getServerHistory().size());

        newConfig = copyConfig(config, p -> {
            p.setProperty("version", "1.2");
            p.setProperty("lruServer", server.getFullName());
        });
        assertEquals(1, newConfig.getServerHistory().size());
        assertEquals(server, newConfig.getServerHistory().get(0));
    }

    @Test
    public void upgradeFromOldConfig() throws IOException {
        Properties p = new Properties();
        p.setProperty("Servers", "server1");
        p.setProperty("server.server1.host", "host.com");
        p.setProperty("server.server1.port", "2000");
        p.setProperty("server.server1.user", "user");
        p.setProperty("server.server1.password", "password");
        p.setProperty("server.server1.backgroundColor", "001122");
        p.setProperty("server.server1.authenticationMechanism", DefaultAuthenticationMechanism.NAME);

        Config config = getConfig(p);
        assertEquals(1, config.getServers().length);

        //duplicate server
        p.setProperty("Servers", "server1,server1");
        config = getConfig(p);
        assertEquals(1, config.getServers().length);

        //undefined servers should be filled with defaults
        p.setProperty("Servers", "server2,server1");
        config = getConfig(p);
        assertEquals(2, config.getServers().length);

        //errors should not fail the whole process
        p.setProperty("server.server1.port", "not a number");
        p.setProperty("server.server1.backgroundColor", "very strange");
        p.setProperty("server.server1.authenticationMechanism", "unknown auth");
        config = getConfig(p);
        assertEquals(1, config.getServers().length);
        assertEquals("server2", config.getServers()[0].getFullName());

    }
    
    @Test
    public void addServersTest() {
        Server server1 = new Server(server);
        server1.setName("testServer1");
        Server server2 = new Server(server);
        server2.setName("comma,name");
        Server server3 = new Server(server);
        server3.setName("testServer1");

        assertThrows(IllegalArgumentException.class, ()->config.addServers(false, server1, server2, server3));
        assertEquals(0, config.getServers().length);

        String[] errors = config.addServers(true, server1, server2, server3);
        assertNull(errors[0]);
        assertNotNull(errors[1]);
        assertNotNull(errors[2]);
        Collection<String> names = config.getServerNames();
        assertEquals(1, names.size());
        assertTrue(names.contains(server1.getFullName()));
        ServerTreeNode serverTree = config.getServerTree();
        assertEquals(1, serverTree.getChildCount());
        assertEquals(1, serverTree.getChild(0).getChildCount());
    }

    @Test
    public void testExecAllOptions() throws IOException {
        assertEquals(Config.ExecAllOption.Ask, config.getExecAllOption());

        config.setExecAllOption(Config.ExecAllOption.Ignore);
        assertEquals(Config.ExecAllOption.Ignore, config.getExecAllOption());

        Config newConfig = copyConfig(config, p -> {});
        assertEquals(Config.ExecAllOption.Ignore, newConfig.getExecAllOption());

        newConfig = copyConfig(config, p -> {
            p.setProperty("execAllOption", "testValue");
        });

        assertEquals(Config.ExecAllOption.Ask, newConfig.getExecAllOption());

    }

    @Test
    public void testConnExractor() throws IOException {
        TableConnExtractor extractor = config.getTableConnExtractor();
        assertNotNull(extractor);

        String conn = "someWords, words";
        String host = "hostWords, words";
        String port = "someWords, ports";

        config.setConnColWords(conn);
        config.setHostColWords(host);
        config.setPortColWords(port);
        config.setTableMaxConnectionPopup(100);
        assertNotEquals(extractor, config.getTableConnExtractor());

        Config newConfig = copyConfig(config, p -> {});
        assertEquals(conn, newConfig.getConnColWords());
        assertEquals(host, newConfig.getHostColWords());
        assertEquals(port, newConfig.getPortColWords());
        assertEquals(100, newConfig.getTableMaxConnectionPopup());
    }

    @Test
    public void testAutoSaveFlags() throws IOException {
        assertFalse(config.getBoolean(Config.AUTO_SAVE));
        assertTrue(config.getBoolean(Config.SAVE_ON_EXIT));

        config.setBoolean(Config.AUTO_SAVE, true);
        config.setBoolean(Config.SAVE_ON_EXIT, false);
        Config newConfig = copyConfig(config, p -> {});
        assertTrue(newConfig.getBoolean(Config.AUTO_SAVE));
        assertFalse(newConfig.getBoolean(Config.SAVE_ON_EXIT));
    }

    @Test
    public void testGetServerByConnectionString() {
        config.setDefaultAuthMechanism("testAuth");
        config.setDefaultCredentials("testAuth", new Credentials("testUser", "testPassword"));

        Server server = config.getServerByConnectionString("host:123");
        assertEquals("host", server.getHost());
        assertEquals(123, server.getPort());
        assertEquals("testAuth", server.getAuthenticationMechanism());
        assertEquals("testUser", server.getUsername());
        assertEquals("testPassword", server.getPassword());

        server = config.getServerByConnectionString("host:123:uu:pp");
        assertEquals(DefaultAuthenticationMechanism.NAME, server.getAuthenticationMechanism());
        assertEquals("uu", server.getUsername());
        assertEquals("pp", server.getPassword());

        server = config.getServerByConnectionString("host:123:uu");
        assertEquals(DefaultAuthenticationMechanism.NAME, server.getAuthenticationMechanism());
        assertEquals("uu", server.getUsername());
        assertEquals("", server.getPassword());

        server = config.getServerByConnectionString("host:123:uu:");
        assertEquals(DefaultAuthenticationMechanism.NAME, server.getAuthenticationMechanism());
        assertEquals("uu", server.getUsername());
        assertEquals("", server.getPassword());

        server = config.getServerByConnectionString("host:123:uu:pp:pp1:");
        assertEquals(DefaultAuthenticationMechanism.NAME, server.getAuthenticationMechanism());
        assertEquals("uu", server.getUsername());
        assertEquals("pp:pp1:", server.getPassword());

        server = config.getServerByConnectionString("host:123:uu::pp:pp1:");
        assertEquals(DefaultAuthenticationMechanism.NAME, server.getAuthenticationMechanism());
        assertEquals("uu", server.getUsername());
        assertEquals(":pp:pp1:", server.getPassword());
    }
}
