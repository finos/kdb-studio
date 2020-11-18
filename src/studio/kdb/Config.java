package studio.kdb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import studio.core.AuthenticationManager;
import studio.core.Credentials;
import studio.core.DefaultAuthenticationMechanism;
import studio.ui.ServerList;
import studio.utils.HistoricalList;
import studio.utils.QConnection;
import studio.utils.TableConnExtractor;

import javax.swing.tree.TreeNode;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.JsonNode;

public class Config {
    private static final Logger log = LogManager.getLogger();

    private enum ConfigType { STRING, INT, DOUBLE, BOOLEAN, FONT, BOUNDS}

    private static final Map<String,? super Object> defaultValues = new HashMap();
    private static final Map<String, ConfigType> configTypes = new HashMap();

    //@TODO migrate all other keys under such approach
    public static final String AUTO_SAVE = configDefault("isAutoSave", ConfigType.BOOLEAN, false);
    public static final String SAVE_ON_EXIT = configDefault("isSaveOnExit", ConfigType.BOOLEAN, true);
    public static final String SERVER_LIST_BOUNDS = configDefault("serverList", ConfigType.BOUNDS, new Dimension(ServerList.DEFAULT_WIDTH, ServerList.DEFAULT_HEIGHT));
    public static final String CHART_BOUNDS = configDefault("chartBounds", ConfigType.BOUNDS, 0.5);
    public static final String CELL_RIGHT_PADDING = configDefault("cellRightPadding", ConfigType.DOUBLE, 0.5);
    public static final String CELL_MAX_WIDTH = configDefault("cellMaxWidth", ConfigType.INT, 200);

    // The folder is also referenced in lon4j2.xml config
    private static final String PATH = System.getProperties().getProperty("user.home") + "/.studioforkdb";
    private static final String CONFIG_FILENAME = "studio.properties";
    private static final String WORKSPACE_FILENAME = "workspace.properties";
    private static String environment = null;

    private static final String VERSION13 = "1.3";
    private static final String VERSION12 = "1.2";
    private static final String OLD_VERSION = "1.1";

    private static final String VERSION = VERSION13;


    private String env;
    private String filename;
    private Properties p = new Properties();
    private Map<String, Server> servers;
    private Collection<String> serverNames;
    private ServerTreeNode serverTree;
    private HistoricalList<Server> serverHistory;

    private static final String CONN_COL_WORDS = "server, host, connection, handle";
    private static final String HOST_COL_WORDS = "server, host";
    private static final String PORT_COL_WORDS = "port";

    private TableConnExtractor tableConnExtractor;

    private final static Map<String, Config> instances = new ConcurrentHashMap<>();

    public enum ExecAllOption {Execute, Ask, Ignore}

    private Config(String env, String filename, Properties properties) {
        this.env = env;
        this.filename = filename;
        init(filename, properties);
    }

    private Config(String env, String filename) {
        this(env, filename, null);
    }

    private static String getConfigFilename(String env, String filename) {
        return PATH + (env == null ? "" : "/" + env) + "/" + filename;
    }

    private String getWorkspaceFilename() {
        return getConfigFilename(env, WORKSPACE_FILENAME);
    }

    public synchronized static String getEnvironment() {
        return environment;
    }

    public synchronized static void setEnvironment(String env) {
        Config.environment = env;
        String configFileName = getConfigFilename(env, CONFIG_FILENAME);

        if (! Files.exists(Paths.get(configFileName))) {
            log.info("Config for environment {} is not found. Copying from default...", env);
            Config defaultConfig = Config.getByEnvironment(null);
            Config config = new Config(env, configFileName, defaultConfig.p);
            config.save();
            config.saveWorkspace(defaultConfig.loadWorkspace());
        }
    }

	public Workspace loadWorkspace() {
		Workspace workspace = new Workspace();
		File workspaceFile = new File(getWorkspaceFilename());
		if (workspaceFile.exists()) {
			try (InputStream inp = new FileInputStream(workspaceFile)) {
				Properties p = new Properties();
				p.load(inp);
				workspace.load(p);
			} catch (IOException e) {
				log.error("Can't load workspace", e);
			}
		}
		return workspace;
	}

    public void saveWorkspace(Workspace workspace) {
        try {
            Properties p = new Properties();
            workspace.save(p);
            OutputStream out = new FileOutputStream(getWorkspaceFilename());
            p.store(out, "Auto-generated by Studio for kdb+");
            out.close();
        } catch (IOException e) {
            log.error("Error during workspace save", e);
        }
    }

    private String[] getWords(String value) {
        return Stream.of(value.split(","))
                        .map(String::trim)
                        .map(String::toLowerCase)
                        .toArray(String[]::new);
    }

    private void initTableConnExtractor() {
        tableConnExtractor = new TableConnExtractor();
        tableConnExtractor.setMaxConn(getTableMaxConnectionPopup());
        tableConnExtractor.setConnWords(getWords(getConnColWords()));
        tableConnExtractor.setHostWords(getWords(getHostColWords()));
        tableConnExtractor.setPortWords(getWords(getPortColWords()));
    }

    public TableConnExtractor getTableConnExtractor() {
        return tableConnExtractor;
    }

    public int getTableMaxConnectionPopup() {
        return Integer.parseInt(p.getProperty("tableMaxConnectionPopup","5"));
    }

    public String getConnColWords() {
        return p.getProperty("connColWords", CONN_COL_WORDS);
    }

    public String getHostColWords() {
        return p.getProperty("hostColWords", HOST_COL_WORDS);

    }

    public String getPortColWords() {
        return p.getProperty("portColWords", PORT_COL_WORDS);
    }

    public void setTableMaxConnectionPopup(int maxConn) {
        p.setProperty("tableMaxConnectionPopup", "" + maxConn);
        save();
        initTableConnExtractor();
    }

    public void setConnColWords(String words) {
        p.setProperty("connColWords", words);
        save();
        initTableConnExtractor();
    }

    public void setHostColWords(String words) {
        p.setProperty("hostColWords", words);
        save();
        initTableConnExtractor();
    }

    public void setPortColWords(String words) {
        p.setProperty("portColWords", words);
        save();
        initTableConnExtractor();
    }

    public ExecAllOption getExecAllOption() {
        String value = p.getProperty("execAllOption", "Ask");
        try {
            return ExecAllOption.valueOf(value);
        } catch (IllegalArgumentException e) {
            log.info(value + " - can't parse execAllOption from Config. Reset to default: Ask");
            return ExecAllOption.Ask;
        }
    }

    public void setExecAllOption(ExecAllOption option) {
        p.setProperty("execAllOption", option.toString());
        save();
    }

    public String getNotesHash() {
        return p.getProperty("notesHash","");
    }

    public void setNotesHash(String notesHash) {
        p.setProperty("notesHash", notesHash);
        save();
    }

    public String getLineEnding() {
        String r = p.getProperty("lineEnding", "");
        if (r.length() == 0) {
            String ls = System.getProperty("line.separator");
            if (ls.equals("\n")) r = "LF";
            else if (ls.equals("\r\n")) r = "CRLF";
            else throw new RuntimeException("unknown line separator setting");
        }
        return r;
    }

    public void setLineEnding(String v) {
        p.setProperty("lineEnding", v);
        save();
    }

    public String getFontName() {
        return p.getProperty("font.name", "Monospaced");
    }

    public int getFontSize() {
        return Integer.parseInt(p.getProperty("font.size","14"));
    }

    public Font getFont() {
        String name = getFontName();
        int  size = getFontSize();

        Font f = new Font(name, Font.PLAIN, size);
        setFont(f);

        return f;
    }

    public String getEncoding() {
        return p.getProperty("encoding", "UTF-8");
    }

    public void setFont(Font f) {
        p.setProperty("font.name", f.getFamily());
        p.setProperty("font.size", "" + f.getSize());
        save();
    }

    public Color getColorForToken(String tokenType, Color defaultColor) {
        String s = p.getProperty("token." + tokenType);
        if (s != null) {
            return new Color(Integer.parseInt(s, 16));
        }

        setColorForToken(tokenType, defaultColor);
        return defaultColor;
    }

    public void setColorForToken(String tokenType, Color c) {
        p.setProperty("token." + tokenType, Integer.toHexString(c.getRGB()).substring(2));
        save();
    }

    public Color getDefaultBackgroundColor() {
        return getColorForToken("BACKGROUND", Color.white);
    }

    public static Config getInstance() {
        return getByEnvironment(environment);
    }

    public static Config getByEnvironment(String env) {
        return getInstance(env, getConfigFilename(env, CONFIG_FILENAME));
    }

    public static Config getByFilename(String filename) {
        return getInstance(environment, filename);
    }

    private static Config getInstance(String env, String filename) {
        return instances.computeIfAbsent(filename, name -> new Config(env, name));
    }

    private void init(String filename, Properties properties) {
        Path file = Paths.get(filename);
        Path dir = file.getParent();
        if (Files.notExists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                log.error("Can't create configuration folder {}", dir, e);
            }
        }

        if (properties != null) {
            p =  (Properties) properties.clone();
        } else {
            if (Files.exists(file)) {
                try {
                    InputStream in = Files.newInputStream(file);
                    p.load(in);
                    in.close();
                } catch (IOException e) {
                    log.error("Can't read configuration from file {}", filename, e);
                }
            }
        }
        checkForUpgrade();
        initServers();
        initServerHistory();
        initTableConnExtractor();
    }

    public String getFilename() {
        return filename;
    }

    private void upgradeTo12() {
        try {
            log.info("Found old config. Converting...");
            String[] names = p.getProperty("Servers", "").split(",");
            List<Server> list = new ArrayList<>();
            for (String name : names) {
                name = name.trim();
                if (name.equals("")) continue;
                try {
                    Server server = initServerFromKey(name);
                    server.setName(name);
                    list.add(server);
                } catch (IllegalArgumentException e) {
                    log.warn("Error during parsing server " + name, e);
                }
            }
            p.remove("Servers");
            p.entrySet().removeIf(e -> e.getKey().toString().startsWith("server."));
            p.setProperty("version", VERSION12);
            initServers();
            String[] results = addServers(true, list.toArray(new Server[0]));
            boolean error = false;
            for(String result: results) {
                if (result == null) continue;
                if (!error) {
                    error = true;
                    log.warn("Found errors during conversion");
                }
                log.warn(result);
            }
            log.info("Done");
        } catch (IllegalArgumentException e) {
            log.error("Ups... Can't convert", e);
        }
    }

    private void upgradeTo13() {
        String fullName = p.getProperty("lruServer", "");
        p.remove("lruServer");
        if (! fullName.equals("")) {
            Server server = getServer(fullName);
            if (server != null) addServerToHistory(server);
        }
        save();
    }

    private void checkForUpgrade() {
        if (p.getProperty("version", OLD_VERSION).equals(OLD_VERSION)) {
            upgradeTo12();
            p.setProperty("version", VERSION12);
        }

        initServers();
        if (p.getProperty("version").equals(VERSION12)) {
            initServerHistory();
            upgradeTo13();
            p.setProperty("version", VERSION13);
        }
        initServerHistory();
    }

    public void save() {
        try {
            OutputStream out = new FileOutputStream(filename);
            p.put("version", VERSION);
            p.store(out, "Auto-generated by Studio for kdb+");
            out.close();
        } catch (IOException e) {
            log.error("Can't save configuration to {}", filename, e);
        }
    }

    public Object serverTreeToObj(ServerTreeNode root) {
        //converts the server tree to an object that can be saved into JSON
        LinkedHashMap<String,Object> result = new LinkedHashMap<>();
        result.put("name", root.getName());
        if(root.isFolder()) {
            ArrayList<Object> children = new ArrayList<>();
            result.put("children", children);
            for (Enumeration<ServerTreeNode> e = root.children(); e.hasMoreElements();) {
                children.add(serverTreeToObj(e.nextElement()));
            }
        }
        return result;
    }

    public void exportServerListToJSON(File f) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        Map<String,Object> cfg = new LinkedHashMap<>();
        ArrayList<Map<String,Object>> svs = new ArrayList<>();
        for (Server s : servers.values()) {
            LinkedHashMap<String,Object> ps = new LinkedHashMap<>();
            svs.add(ps);
            ps.put("name", s.getName());
            ps.put("host", s.getHost());
            ps.put("port", s.getPort());
            ps.put("username", s.getUsername());
            ps.put("password", s.getPassword());
            ps.put("useTls", s.getUseTLS());
            ps.put("authMethod", s.getAuthenticationMechanism());
            ArrayList<Integer> color = new ArrayList<>(3);
            Color bgc = s.getBackgroundColor();
            color.add(bgc.getRed());
            color.add(bgc.getGreen());
            color.add(bgc.getBlue());
            ps.put("color", color);
        }
        cfg.put("servers",svs);
        cfg.put("serverTree", serverTreeToObj(serverTree));
        try {
            FileWriter sw = new FileWriter(f);
            objectMapper.writeValue(sw, cfg);
        } catch(IOException e) {
            e.printStackTrace(System.err);
        }
    }

    private void importServerTreeFromJSON(HashMap<String, Server> serverMap, boolean isRoot, JsonNode jn, ServerTreeNode tn) {
        if (jn.has("children")) {   //is a folder
            ServerTreeNode ntn = tn;
            if (!isRoot) {
                String folderName = jn.get("name").asText("");
                ntn = tn.getChild(folderName);
                if (ntn == null) {
                    ntn = new ServerTreeNode(folderName);
                    tn.add(ntn);
                }
            };
            JsonNode children = jn.get("children");
            if (children.isArray()) {
                for (JsonNode child : (Iterable<JsonNode>) ()->children.elements()) {
                    importServerTreeFromJSON(serverMap, false, child, ntn);
                }
            }
        } else {
            if (jn.has("name")) {
                String name = jn.get("name").asText("");
                if (name.length() > 0) {
                    if (serverMap.containsKey(name)) {
                        Server s = serverMap.get(name);
                        s.setFolder(tn);
                        addServer(s);
                        serverMap.remove(s);
                    }
                }
            }
        }
    }

    public String importServerListFromJSON(File f) {
        ObjectMapper objectMapper = new ObjectMapper();
        StringBuilder sb = new StringBuilder();
        ArrayList<String> alreadyExist = new ArrayList<>();
        ArrayList<Integer> noName = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(f);
            if (!root.isObject()) return "JSON root node is not an object";
            if (!root.has("servers")) return "JSON root node doesn't have a \"servers\" property";
            if (!root.has("serverTree")) return "JSON root node doesn't have a \"serverTree\" property";
            JsonNode serversNode = root.get("servers");
            JsonNode serverTreeNode = root.get("serverTree");
            if (!serversNode.isArray()) return "\"servers\" node is not an array";
            HashSet<String> existingServers = new HashSet<>();
            for (Server s : servers.values()) existingServers.add(s.getName());
            HashMap<String, Server> serverMap = new HashMap<>();
            int i=0;
            for (JsonNode serverNode : (Iterable<JsonNode>) ()->serversNode.elements()) {
                if (!serverNode.isObject()) {
                    sb.append("Non-object found inside \"servers\" array at index "+i+"\n");
                } else if (!serverNode.has("name")) {
                    sb.append("Server at index "+i+" has no name\n");
                } else {
                    String sname = serverNode.get("name").asText();
                    if (sname.length() == 0) {
                        noName.add(i);
                    } else if (existingServers.contains(sname)) {
                        alreadyExist.add(sname);
                    } else {
                        Server s = new Server();
                        s.setName(sname);
                        if (serverNode.has("host")) s.setHost(serverNode.get("host").asText(""));
                        if (serverNode.has("port")) s.setPort(serverNode.get("port").asInt(0));
                        if (serverNode.has("username")) s.setUsername(serverNode.get("username").asText(""));
                        if (serverNode.has("password")) s.setPassword(serverNode.get("password").asText(""));
                        if (serverNode.has("useTls")) s.setUseTLS(serverNode.get("useTls").asBoolean(false));
                        if (serverNode.has("authMethod")) s.setAuthenticationMechanism(serverNode.get("authMethod").asText(""));
                        if (serverNode.has("color")) {
                            JsonNode color = serverNode.get("color");
                            if (color.isArray() && color.size() >= 3) {
                                s.setBackgroundColor(new Color(color.get(0).asInt(255),color.get(1).asInt(255),color.get(2).asInt(255)));
                            }
                        }
                        serverMap.put(sname, s);
                    }
                }
                ++i;
            }
            if (serverTreeNode.isObject()) {
                importServerTreeFromJSON(serverMap, true, serverTreeNode, serverTree);
            }
            if (0<noName.size()) sb.append("The servers at the following indices have no names: "+noName);
            if (0<alreadyExist.size()) sb.append("The following servers already exist and were not imported: "+alreadyExist);
        } catch(IOException e) {
            return e.toString();
        }
        int i = 0;
        final int wordLength = 150;
        while (i + wordLength < sb.length() && (i = sb.lastIndexOf(" ", i + wordLength)) != -1) {
            sb.replace(i, i + 1, "\n");
        }
        return sb.toString();
    }

    // "".split(",") return {""}; we need to get zero length array
    private String[] split(String str) {
        str = str.trim();
        if (str.length() == 0) return new String[0];
        return str.split(",");
    }

    public int getServerHistoryDepth() {
        return Integer.parseInt(p.getProperty("serverHistoryDepth", "20"));
    }

    public void setServerHistoryDepth(int depth) {
        serverHistory.setDepth(depth);
        p.setProperty("serverHistoryDepth", "" + depth);
        save();
    }

    private void initServerHistory() {
        int depth = getServerHistoryDepth();
        serverHistory = new HistoricalList<>(depth);
        for (int i=depth-1; i>=0; i--) {
            String key = "serverHistory." + i;
            if (! p.containsKey(key)) continue;
            Server server = getServer(p.getProperty(key));
            if (server == null) continue;
            serverHistory.add(server);
        }
    }

    public List<Server> getServerHistory() {
        return Collections.unmodifiableList(serverHistory);
    }

    public void addServerToHistory(Server server) {
        serverHistory.add(server);
        for (int i=serverHistory.size()-1; i>=0; i--) {
            String key = "serverHistory." + i;
            p.setProperty(key, serverHistory.get(i).getFullName());
        }
        save();
    }

    public void setAcceptedLicense(Date d) {
        p.put("licenseAccepted", d.toString());
        save();
    }

    public String[] getMRUFiles() {
        String mru = p.getProperty("mrufiles", "");
        return split(mru);
    }


    public void saveMRUFiles(String[] mruFiles) {
        String value = Stream.of(mruFiles).limit(9).collect(Collectors.joining(","));
        p.put("mrufiles", value);
        save();
    }

    public String getLookAndFeel() {
        return p.getProperty("lookandfeel");
    }

    public void setLookAndFeel(String lf) {
        p.put("lookandfeel", lf);
        save();
    }

    // Resolve or create a new server by connection string.
    // Accept possible various connectionString such as:
    // `:host:port:user:password
    // host:port
    // If user and password are not found, defaults form default AuthenticationMechanism are used
    public Server getServerByConnectionString(String connectionString) {
        String defaultAuth = getDefaultAuthMechanism();
        return QConnection.getByConnection(connectionString, defaultAuth, getDefaultCredentials(defaultAuth), servers.values());
    }

    public Credentials getDefaultCredentials(String authenticationMechanism) {
        String user = p.getProperty("auth." + authenticationMechanism + ".user", "");
        String password = p.getProperty("auth." + authenticationMechanism + ".password", "");
        return new Credentials(user, password);
    }

    public void setDefaultCredentials(String authenticationMechanism, Credentials credentials) {
        p.setProperty("auth." + authenticationMechanism + ".user", credentials.getUsername());
        p.setProperty("auth." + authenticationMechanism + ".password", credentials.getPassword());
        save();
    }

    public String getDefaultAuthMechanism() {
        String result = p.getProperty("auth", null);
        if (result == null) result = System.getenv().getOrDefault("KDB_STUDIO_AUTH_METHOD", DefaultAuthenticationMechanism.NAME);
        return result;
    }

    public void setDefaultAuthMechanism(String authMechanism) {
        p.setProperty("auth", authMechanism);
        save();
    }

    public boolean isShowServerComboBox() {
        return Boolean.parseBoolean(p.getProperty("showServerComboBox","true"));
    }

    public void setShowServerComboBox(boolean value) {
        p.setProperty("showServerComboBox", "" + value);
        save();
    }

    public int getResultTabsCount() {
        return Integer.parseInt(p.getProperty("resultTabsCount","5"));
    }

    public void setResultTabsCount(int value) {
        p.setProperty("resultTabsCount", "" + value);
        save();
    }

    public int getMaxCharsInResult() {
        return Integer.parseInt(p.getProperty("maxCharsInResult", "50000"));
    }

    public void setMaxCharsInResult(int value) {
        p.setProperty("maxCharsInResult", "" + value);
        save();
    }

    public int getMaxCharsInTableCell() {
        return Integer.parseInt(p.getProperty("maxCharsInTableCell", "256"));
    }

    public void setMaxCharsInTableCell(int value) {
        p.setProperty("maxCharsInTableCell", "" + value);
        save();
    }

    public Collection<String> getServerNames() {
        return Collections.unmodifiableCollection(serverNames);
    }

    public Server[] getServers() {
        return servers.values().toArray(new Server[servers.size()]);
    }

    public Server getServer(String name) {
        return servers.get(name);
    }

    public ServerTreeNode getServerTree() {
        return serverTree;
    }

    private Server initServerFromKey(String key) {
        String host = p.getProperty("server." + key + ".host", "");
        int port = Integer.parseInt(p.getProperty("server." + key + ".port", "-1"));
        String username = p.getProperty("server." + key + ".user", "");
        String password = p.getProperty("server." + key + ".password", "");
        String backgroundColor = p.getProperty("server." + key + ".backgroundColor", "FFFFFF");
        String authenticationMechanism = p.getProperty("server." + key + ".authenticationMechanism", null);
        if (authenticationMechanism == null)
            authenticationMechanism = System.getenv().getOrDefault("KDB_STUDIO_AUTH_METHOD", DefaultAuthenticationMechanism.NAME);
        boolean useTLS = Boolean.parseBoolean(p.getProperty("server." + key + ".useTLS", "false"));
        Color c = new Color(Integer.parseInt(backgroundColor, 16));
        return new Server("", host, port, username, password, c, authenticationMechanism, useTLS);
    }

    private Server initServerFromProperties(int number) {
        return initServerFromKey("" + number);
    }

    private void initServers() {
        serverNames = new ArrayList<>();
        serverTree = new ServerTreeNode();
        servers = new HashMap<>();
        initServerTree("serverTree.", serverTree, 0);
    }

    private int initServerTree(String keyPrefix, ServerTreeNode parent, int number) {
        for (int index = 0; ; index++) {
            String key = keyPrefix + index;
            String folderKey = key + "folder";
            if (p.containsKey(folderKey)) {
                ServerTreeNode node = parent.add(p.getProperty(folderKey));
                number = initServerTree(key + ".", node, number);
            } else if (p.containsKey(key)) {
                Server server = initServerFromProperties(number);
                server.setFolder(parent);
                String name = p.getProperty(key);
                server.setName(name);
                String fullName = server.getFullName();
                servers.put(fullName, server);
                serverNames.add(fullName);
                parent.add(server);
                number++;
            } else {
                break;
            }
        }
        return number;
    }

    private void saveAllServers() {
        p.entrySet().removeIf(e -> e.getKey().toString().startsWith("serverTree."));
        p.entrySet().removeIf(e -> e.getKey().toString().startsWith("server."));
        saveServerTree("serverTree.", serverTree, 0);

        save();
    }

    private void saveServerDetails(Server server, int number) {
        p.setProperty("server." + number + ".host", server.getHost());
        p.setProperty("server." + number + ".port", "" + server.getPort());
        p.setProperty("server." + number + ".user", "" + server.getUsername());
        p.setProperty("server." + number + ".password", "" + server.getPassword());
        p.setProperty("server." + number + ".backgroundColor", "" + Integer.toHexString(server.getBackgroundColor().getRGB()).substring(2));
        p.setProperty("server." + number + ".authenticationMechanism", server.getAuthenticationMechanism());
        p.setProperty("server." + number + ".useTLS", "" + server.getUseTLS());
    }

    private int saveServerTree(String keyPrefix, ServerTreeNode node, int number) {
        int count = node.getChildCount();
        for(int index = 0; index<count; index++) {
            String key = keyPrefix + index;
            ServerTreeNode child = node.getChild(index);
            if (child.isFolder()) {
                p.setProperty(key + "folder", child.getFolder());
                number = saveServerTree(key + ".", child, number);
            } else {
                Server server = child.getServer();
                p.setProperty(key, server.getName());
                saveServerDetails(server, number);
                number++;
            }
        }
        return number;
    }

    public void removeServer(Server server) {
        String name = server.getFullName();
        serverNames.remove(name);
        servers.remove(name);
        ServerTreeNode folder = server.getFolder();
        if (folder != null) {
            folder.remove(server);
        }

        saveAllServers();
    }

    private void purgeAll() {
        servers.clear();
        serverNames.clear();
        serverTree = new ServerTreeNode();
    }

    public void removeAllServers() {
        purgeAll();
        saveAllServers();
    }

    private void addServerInternal(Server server) {
        String name = server.getName();
        String fullName = server.getFullName();
        if (serverNames.contains(fullName)) {
            throw new IllegalArgumentException("Server with full name " + fullName + " already exists");
        }
        if (name.trim().length() == 0) {
            throw new IllegalArgumentException("Server name can't be empty");
        }
        if (name.contains(",")) {
            throw new IllegalArgumentException("Server name can't contains ,");
        }
        if (name.contains("/")) {
            throw new IllegalArgumentException("Server name can't contains /");
        }
        if (AuthenticationManager.getInstance().lookup(server.getAuthenticationMechanism()) == null) {
            throw new IllegalArgumentException("Unknown Authentication Mechanism: " + server.getAuthenticationMechanism());
        }

        TreeNode[] path = server.getFolder().getPath();
        for (int index = 1; index<path.length; index++) {
            ServerTreeNode node = (ServerTreeNode) path[index];
            if (node.getFolder().trim().length() == 0) {
                throw new IllegalArgumentException("Folder name can't be empty");
            }
        }
        servers.put(fullName, server);
        serverNames.add(fullName);
    }


    public void addServer(Server server) {
        addServers(false, server);
    }

    public String[] addServers(boolean tryAll, Server... newServers) {
        String[] result = tryAll ? new String[newServers.length] : null;
        Properties backup = new Properties();
        backup.putAll(p);
        try {
            int index = -1;
            for (Server server : newServers) {
                index++;
                try {
                    ServerTreeNode folder = server.getFolder();
                    if (folder == null) {
                        server.setFolder(serverTree);
                        folder = serverTree;
                    }
                    addServerInternal(server);
                    serverTree.findPath(folder.getPath(), true).add(server);
                } catch (IllegalArgumentException e) {
                    if (tryAll) {
                        result[index] = e.getMessage();
                    } else {
                        throw e;
                    }
                }
            }

            saveAllServers();
        } catch (IllegalArgumentException e) {
            p = backup;
            initServers();
            throw e;
        }
        return result;
    }

    public void setServerTree(ServerTreeNode serverTree) {
        Properties backup = new Properties();
        backup.putAll(p);
        try {
            purgeAll();
            this.serverTree = serverTree;

            for(Enumeration e = serverTree.depthFirstEnumeration(); e.hasMoreElements();) {
                ServerTreeNode node = (ServerTreeNode) e.nextElement();
                if (node.isRoot()) continue;

                if (node.isFolder()) {
                    String folder = node.getFolder();
                    if (folder.trim().length()==0) {
                        throw new IllegalArgumentException("Can't add folder with empty name");
                    }
                    if (folder.contains("/")) {
                        throw new IllegalArgumentException("Folder can't contain /");
                    }
                    if ( ((ServerTreeNode)node.getParent()).getChild(node.getFolder())!= node ) {
                        throw new IllegalArgumentException("Duplicate folder is found: " + node.fullPath());
                    }
                } else {
                    Server server = node.getServer();
                    server.setFolder((ServerTreeNode) node.getParent());
                    addServerInternal(server);
                }
            }

            saveAllServers();
        } catch (IllegalArgumentException e) {
            p = backup;
            initServers();
            throw e;
        }
    }

    private Object checkAndGetDefaultValue(String key, ConfigType passed) {
        ConfigType type = configTypes.get(key);
        if (type == null) {
            throw new IllegalStateException("Ups... Wrong access to config " + key + ". The key wasn't defined");
        }
        if (type != passed) {
            throw new IllegalStateException("Ups... Wrong access to config " + key + ". Expected type: " + type + "; passed: " + passed);
        }
        return defaultValues.get(key);
    }

    private static String configDefault(String key, ConfigType type, Object value) {
        defaultValues.put(key, value);
        configTypes.put(key, type);
        return key;
    }

    private boolean get(String key, boolean defaultValue) {
        String value = p.getProperty(key);
        if (value == null) return defaultValue;

        return Boolean.parseBoolean(value);
    }

    public boolean getBoolean(String key) {
        return get(key, (Boolean) checkAndGetDefaultValue(key, ConfigType.BOOLEAN));
    }

    public void setBoolean(String key, boolean value) {
        checkAndGetDefaultValue(key, ConfigType.BOOLEAN);
        p.setProperty(key, "" + value);
        save();
    }

    private double get(String key, double defaultValue) {
        String value = p.getProperty(key);
        if (value == null) return defaultValue;

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            log.error("Failed to parse config key " + key + " from config", e);
        }
        return defaultValue;
    }

    public double getDouble(String key) {
        return get(key, (Double) checkAndGetDefaultValue(key, ConfigType.DOUBLE));
    }

    public void setDouble(String key, double value) {
        checkAndGetDefaultValue(key, ConfigType.DOUBLE);
        p.setProperty(key, "" + value);
        save();
    }

    private int get(String key, int defaultValue) {
        String value = p.getProperty(key);
        if (value == null) return defaultValue;

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.error("Failed to parse config key " + key + " from config", e);
        }
        return defaultValue;
    }

    public int getInt(String key) {
        return get(key, (Integer) checkAndGetDefaultValue(key, ConfigType.INT));
    }

    public void setInt(String key, int value) {
        checkAndGetDefaultValue(key, ConfigType.INT);
        p.setProperty(key, "" + value);
        save();
    }

    private Rectangle getBounds(String key, Object defaultBoundOrScale) {
        try {
            String strX = p.getProperty(key + ".x");
            String strY = p.getProperty(key + ".y");
            String strWidth = p.getProperty(key + ".width");
            String strHeight = p.getProperty(key + ".height");

            if (strX != null && strY != null && strWidth != null && strHeight != null) {
                Rectangle bounds = new Rectangle(Integer.parseInt(strX), Integer.parseInt(strY),
                        Integer.parseInt(strWidth), Integer.parseInt(strHeight));
                boolean fitToScreen = false;
                GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
                for (GraphicsDevice device : devices) {
                    fitToScreen |= device.getDefaultConfiguration().getBounds().contains(bounds);
                }
                if (fitToScreen) return bounds;

                log.info("Bounds of {} doesn't fit to any of current monitors - falling back to a default value", key);
            }

        } catch (NumberFormatException e) {
            log.error("Failed to parse bounds from config key " + key, e);
        }

        DisplayMode displayMode = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDisplayMode();

        int width = displayMode.getWidth();
        int height = displayMode.getHeight();

        int w,h;

        if (defaultBoundOrScale instanceof Dimension) {
            Dimension defaultSize = (Dimension)defaultBoundOrScale;
            w = Math.min(width / 2, defaultSize.width);
            h = Math.min(height / 2, defaultSize.height);
        } else {
            double scale = 0.5;
            if (defaultBoundOrScale instanceof Double) {
                scale = (Double) defaultBoundOrScale;
            } else {
                log.error("Internal error. Wrong default value passed to getBounds - key = {}; value = {}", key, defaultBoundOrScale);
            }
            w = (int) (width * scale);
            h = (int) (height * scale);
        }

        int x = (width - w) / 2;
        int y = (height - h) / 2;
        return new Rectangle(x,y,w,h);

    }

    public Rectangle getBounds(String key) {
        return getBounds(key, checkAndGetDefaultValue(key, ConfigType.BOUNDS));
    }

    public void setBounds(String key, Rectangle bound) {
        p.setProperty(key + ".x", "" + bound.x);
        p.setProperty(key + ".y", "" + bound.y);
        p.setProperty(key + ".width", "" + bound.width);
        p.setProperty(key + ".height", "" + bound.height);
        save();
    }

}
