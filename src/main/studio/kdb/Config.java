package studio.kdb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import studio.core.AuthenticationManager;
import studio.core.Credentials;
import studio.core.DefaultAuthenticationMechanism;
import studio.ui.ServerList;
import studio.utils.HistoricalList;
import studio.utils.LineEnding;
import studio.utils.QConnection;
import studio.utils.TableConnExtractor;

import javax.swing.tree.TreeNode;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Config {
    private static final Logger log = LogManager.getLogger();

    private enum ConfigType { STRING, INT, DOUBLE, BOOLEAN, FONT, BOUNDS, COLOR, ENUM, SIZE, FILE_CHOOSER}

    private static final Map<String,? super Object> defaultValues = new HashMap();
    private static final Map<String, ConfigType> configTypes = new HashMap();

    //@TODO migrate all other keys under such approach
    public static final String AUTO_SAVE = configDefault("isAutoSave", ConfigType.BOOLEAN, false);
    public static final String SAVE_ON_EXIT = configDefault("isSaveOnExit", ConfigType.BOOLEAN, true);
    public static final String SERVER_LIST_BOUNDS = configDefault("serverList", ConfigType.BOUNDS, new Dimension(ServerList.DEFAULT_WIDTH, ServerList.DEFAULT_HEIGHT));
    public static final String CHART_BOUNDS = configDefault("chartBounds", ConfigType.BOUNDS, 0.5);
    public static final String CELL_RIGHT_PADDING = configDefault("cellRightPadding", ConfigType.DOUBLE, 0.5);
    public static final String CELL_MAX_WIDTH = configDefault("cellMaxWidth", ConfigType.INT, 200);

    public static final String RSTA_ANIMATE_BRACKET_MATCHING = configDefault("rstaAnimateBracketMatching", ConfigType.BOOLEAN, true);
    public static final String RSTA_HIGHLIGHT_CURRENT_LINE = configDefault("rstaHighlightCurrentLine", ConfigType.BOOLEAN, true);
    public static final String RSTA_WORD_WRAP = configDefault("rstaWordWrap", ConfigType.BOOLEAN, false);

    public static final String DEFAULT_LINE_ENDING = configDefault("defaultLineEnding", ConfigType.ENUM, LineEnding.Unix);

    public static final String COLOR_CHARVECTOR = configDefault("token.CHARVECTOR", ConfigType.COLOR, new Color(0,200,20));
    public static final String COLOR_EOLCOMMENT = configDefault("token.EOLCOMMENT", ConfigType.COLOR,  Color.GRAY);
    public static final String COLOR_IDENTIFIER = configDefault("token.IDENTIFIER", ConfigType.COLOR, new Color(180,160,0));
    public static final String COLOR_OPERATOR = configDefault("token.OPERATOR", ConfigType.COLOR, Color.BLACK);
    public static final String COLOR_BOOLEAN = configDefault("token.BOOLEAN", ConfigType.COLOR, new Color(51,204,255));
    public static final String COLOR_BYTE = configDefault("token.BYTE", ConfigType.COLOR, new Color(51,104,255));
    public static final String COLOR_SHORT = configDefault("token.SHORT", ConfigType.COLOR, new Color(51,104,255));
    public static final String COLOR_LONG = configDefault("token.LONG", ConfigType.COLOR, new Color(51,104,255));
    public static final String COLOR_REAL = configDefault("token.REAL", ConfigType.COLOR, new Color(51,104,255));
    public static final String COLOR_INTEGER = configDefault("token.INTEGER", ConfigType.COLOR, new Color(51,104,255));
    public static final String COLOR_FLOAT = configDefault("token.FLOAT", ConfigType.COLOR, new Color(51,104,255));
    public static final String COLOR_TIMESTAMP = configDefault("token.TIMESTAMP", ConfigType.COLOR, new Color(184,138,0));
    public static final String COLOR_TIMESPAN = configDefault("token.TIMESPAN", ConfigType.COLOR, new Color(184,138,0));
    public static final String COLOR_DATETIME = configDefault("token.DATETIME", ConfigType.COLOR, new Color(184,138,0));
    public static final String COLOR_DATE = configDefault("token.DATE", ConfigType.COLOR, new Color(184,138,0));
    public static final String COLOR_MONTH = configDefault("token.MONTH", ConfigType.COLOR, new Color(184,138,0));
    public static final String COLOR_MINUTE = configDefault("token.MINUTE", ConfigType.COLOR, new Color(184,138,0));
    public static final String COLOR_SECOND = configDefault("token.SECOND", ConfigType.COLOR, new Color(184,138,0));
    public static final String COLOR_TIME = configDefault("token.TIME", ConfigType.COLOR, new Color(184,138,0));
    public static final String COLOR_SYMBOL = configDefault("token.SYMBOL", ConfigType.COLOR, new Color(179,0,134));
    public static final String COLOR_KEYWORD = configDefault("token.KEYWORD", ConfigType.COLOR, new Color(0,0,255));
    public static final String COLOR_COMMAND = configDefault("token.COMMAND", ConfigType.COLOR, new Color(240,180,0));
    public static final String COLOR_SYSTEM = configDefault("token.SYSTEM", ConfigType.COLOR, new Color(240,180,0));
    public static final String COLOR_WHITESPACE = configDefault("token.WHITESPACE", ConfigType.COLOR, Color.BLACK);
    public static final String COLOR_DEFAULT = configDefault("token.DEFAULT", ConfigType.COLOR, Color.BLACK);
    public static final String COLOR_BRACKET = configDefault("token.BRACKET", ConfigType.COLOR, Color.BLACK);

    public static final String COLOR_ERROR = configDefault("token.ERROR", ConfigType.COLOR, Color.RED);

    public static final String COLOR_BACKGROUND = configDefault("token.BACKGROUND", ConfigType.COLOR, Color.WHITE);

    public static final String FONT_EDITOR = configDefault("font", ConfigType.FONT, new Font("Monospaced", Font.PLAIN, 14));
    public static final String FONT_TABLE = configDefault("fontTable", ConfigType.FONT, new Font("Monospaced", Font.PLAIN, 14));

    public static final String MAX_FRACTION_DIGITS = configDefault("maxFractionDigits", ConfigType.INT, 7);
    public static final String EMULATED_DOUBLE_CLICK_TIMEOUT = configDefault("emulatedDoubleClickTimeout", ConfigType.INT, 500);

    public static final String OPEN_FILE_CHOOSER = configDefault("openFileChooser", ConfigType.FILE_CHOOSER, new FileChooserConfig());
    public static final String SAVE_FILE_CHOOSER = configDefault("saveFileChooser", ConfigType.FILE_CHOOSER, new FileChooserConfig());
    public static final String EXPORT_FILE_CHOOSER = configDefault("exportFileChooser", ConfigType.FILE_CHOOSER, new FileChooserConfig());

    private enum FontStyle {
        Plain(Font.PLAIN), Bold(Font.BOLD), Italic(Font.ITALIC), ItalicAndBold(Font.BOLD|Font.ITALIC);
        private int style;

        FontStyle(int style) {
            this.style = style;
        }
        public int getStyle() {
            return style;
        }
    }

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

    public String getEncoding() {
        return p.getProperty("encoding", "UTF-8");
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
        if (p.size() == 0) {
            log.info("Found no or empty config");
            p.setProperty("version", VERSION);
            return;
        }

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
        return p.getProperty("auth", DefaultAuthenticationMechanism.NAME);
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
        Color backgroundColor = get("server." + key + ".backgroundColor", Color.WHITE);
        String authenticationMechanism = p.getProperty("server." + key + ".authenticationMechanism", DefaultAuthenticationMechanism.NAME);
        boolean useTLS = Boolean.parseBoolean(p.getProperty("server." + key + ".useTLS", "false"));
        return new Server("", host, port, username, password, backgroundColor, authenticationMechanism, useTLS);
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

    private static String configDefault(String key, ConfigType type, Object defaultValue) {
        defaultValues.put(key, defaultValue);
        configTypes.put(key, type);

        //Looks like a hack? How to make it more elegant?
        if (type == ConfigType.FILE_CHOOSER) {
            FileChooserConfig config = (FileChooserConfig) defaultValue;
            configDefault(key + ".filename", ConfigType.STRING, config.getFilename());
            configDefault(key + ".prefSize", ConfigType.SIZE, config.getPreferredSize());
        }

        return key;
    }

    private String get(String key, String defaultValue) {
        String value = p.getProperty(key);
        return value == null ? defaultValue : value;
    }

    public String getString(String key) {
        return get(key, (String) checkAndGetDefaultValue(key, ConfigType.STRING));
    }

    // Returns whether the value was changed
    public boolean setString(String key, String value) {
        String currentValue = getString(key);
        if (currentValue.equals(value)) return false;

        p.setProperty(key, value);
        save();
        return true;
    }

    private boolean get(String key, boolean defaultValue) {
        String value = p.getProperty(key);
        if (value == null) return defaultValue;

        return Boolean.parseBoolean(value);
    }

    public boolean getBoolean(String key) {
        return get(key, (Boolean) checkAndGetDefaultValue(key, ConfigType.BOOLEAN));
    }

    // Returns whether the value was changed
    public boolean setBoolean(String key, boolean value) {
        boolean currentValue = getBoolean(key);
        if (currentValue == value) {
            return false;
        }

        p.setProperty(key, "" + value);
        save();
        return true;
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

    // Returns whether the value was changed
    public boolean setDouble(String key, double value) {
        double currentValue = getDouble(key);
        if (currentValue == value) {
            return false;
        }

        p.setProperty(key, "" + value);
        save();
        return true;
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

    // Returns whether the value was changed
    public boolean setInt(String key, int value) {
        int currentValue = getInt(key);
        if (currentValue == value) {
            return false;
        }

        p.setProperty(key, "" + value);
        save();
        return true;
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

    private Dimension get(String key, Dimension defaultValue) {
        try {
            String strWidth = p.getProperty(key + ".width");
            String strHeight = p.getProperty(key + ".height");

            if (strWidth != null && strHeight != null) {
                return new Dimension(Integer.parseInt(strWidth), Integer.parseInt(strHeight));
            }

        } catch (NumberFormatException e) {
            log.error("Failed to parse dimension from config key " + key, e);
        }
        return defaultValue;
    }

    public Dimension getSize(String key) {
        return get(key, (Dimension) checkAndGetDefaultValue(key, ConfigType.SIZE));
    }
    public boolean setSize(String key, Dimension value) {
        Dimension currentValue = getSize(key);
        if (currentValue.equals(value)) return false;

        p.setProperty(key + ".width", "" + value.width);
        p.setProperty(key + ".height", "" + value.height);
        save();
        return true;
    }

    private FileChooserConfig get(String key, FileChooserConfig defaultValue) {
        String filename = get(key + ".filename", (String) null);
        Dimension size = get(key + ".prefSize", (Dimension) null);

        if (size != null && filename != null) {
            return new FileChooserConfig(filename, size);
        }

        return defaultValue;
    }

    public FileChooserConfig getFileChooserConfig(String key) {
        return get(key, (FileChooserConfig) checkAndGetDefaultValue(key, ConfigType.FILE_CHOOSER));
    }

    public boolean setFileChooserConfig(String key, FileChooserConfig value) {
        FileChooserConfig currentValue = getFileChooserConfig(key);
        if (currentValue.equals(value)) return false;

        setString(key + ".filename", value.getFilename());
        setSize(key + ".prefSize", value.getPreferredSize());
        save();
        return true;
    }

    private Color get(String key, Color defaultValue) {
        String value = p.getProperty(key);
        if (value == null) return defaultValue;

        try {
            return new Color(Integer.parseInt(value, 16));
        } catch (NumberFormatException e) {
            log.error("Failed to parse {} for config key {}", value, key, e);
        }
        return defaultValue;
    }

    public Color getColor(String key) {
        return get(key, (Color) checkAndGetDefaultValue(key, ConfigType.COLOR));
    }

    // Returns whether the value was changed
    public boolean setColor(String key, Color value) {
        Color currentValue = getColor(key);
        if (currentValue.equals(value)) {
            return false;
        }
        p.setProperty(key, Integer.toHexString(value.getRGB()).substring(2));
        save();
        return true;
    }

    private <T extends Enum<T>> T get(String key, T defaultValue) {
        String value = p.getProperty(key);
        if (value == null) return defaultValue;

        try {
            return (T) Enum.valueOf(defaultValue.getClass(), value);
        } catch (IllegalArgumentException e) {
            log.error("Failed to parse {} for config key {}", value, key, e);
        }
        return defaultValue;
    }

    public <T extends Enum<T>> T getEnum(String key) {
        return get(key, (T) checkAndGetDefaultValue(key, ConfigType.ENUM));
    }

    public <T extends Enum<T>> boolean setEnum(String key, T value) {
        T currentValue = getEnum(key);
        if (currentValue == value) {
            return false;
        }

        p.setProperty(key, value.name());
        save();
        return true;
    }

    private Font get(String key, Font defaultValue) {
        String name = p.getProperty(key + ".name");
        if (name == null) return defaultValue;

        int size = get(key + ".size", 14);
        int style = get(key +".style", FontStyle.Plain).getStyle();

        return new Font(name, style, size);
    }

    public Font getFont(String key) {
        return get(key, (Font) checkAndGetDefaultValue(key, ConfigType.FONT));
    }

    // Returns whether the value was changed
    public boolean setFont(String key, Font value) {
        Font currentValue = getFont(key);
        if (currentValue.equals(value))
        if (currentValue == value) {
            return false;
        }
        p.setProperty(key + ".name", value.getName());
        setInt(key + ".size", value.getSize());

        int style = value.getStyle();
        if (style < 0 || style > 3) style = 0; // Not sure if it is posible
        FontStyle fontStyle = FontStyle.values()[style];
        setEnum(key + ".style", fontStyle);

        save();
        return true;
    }
}
