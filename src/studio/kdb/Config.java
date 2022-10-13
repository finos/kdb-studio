package studio.kdb;

import studio.core.Credentials;
import studio.core.DefaultAuthenticationMechanism;
import studio.ui.ServerList;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.JsonNode;

public class Config {
    private final static String PATH = System.getProperties().getProperty("user.home") + "/.studioforkdb/";
    private final static String FILENAME = PATH + "studio.properties";
    private final static String VERSION = "1.2";
    private final static String OLD_VERSION = "1.1";

    private Properties p = new Properties();
    private final Map<String, Server> servers = new HashMap<>();
    private Collection<String> serverNames;
    private ServerTreeNode serverTree;

    private final static Config instance = new Config();

    private Config() {
        init();
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

    public synchronized NumberFormat getNumberFormat() {
        String key = p.getProperty("DecimalFormat", "#.#######");

        return new DecimalFormat(key);
    }

    public static Config getInstance() {
        return instance;
    }

    private void init() {
        Path file = Paths.get(FILENAME);
        Path dir = file.getParent();
        if (Files.notExists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                System.err.println("Can't create configuration folder: " + PATH);
            }
            return;
        }

        if (!Files.exists(file) && System.getProperty("os.name").startsWith("Windows")) {
            System.out.println("Config not found in userprofile. Trying legacy path.");
            //Old Java versions returned a different place for user.home on Windows.
            //A user upgrading from such old directory would suddenly "lose" their config.
            String oldpath = null;
            try {
                Process process = Runtime.getRuntime().exec("reg query \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders\" /v Desktop");
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("Desktop") && line.contains("REG_SZ")) {
                        //    Desktop    REG_SZ    \\path\to\Desktop
                        String[] tokens = line.split("[ \t]");
                        int tc=0;
                        for (int i=0; i<tokens.length; ++i) {
                            if (tokens[i].length() > 0) ++tc;
                            if (tc==3) oldpath = tokens[i];
                        }
                    }
                }
            } catch (IOException e) {
                //ignore
            }
            System.out.println("Old path: "+oldpath);
            if (oldpath != null) file = Paths.get(oldpath.substring(0,oldpath.lastIndexOf('\\'))+"\\.studioforkdb\\studio.properties");
        }
        if (Files.exists(file)) {
            try {
                InputStream in = Files.newInputStream(file);
                p.load(in);
                in.close();
            } catch (IOException e) {
                System.err.println("Cant't read configuration from file " + FILENAME);
                e.printStackTrace(System.err);
            }
        }
        initServers();
    }


    public void save() {
        try {
            OutputStream out = new FileOutputStream(FILENAME);
            p.put("version", VERSION);
            p.store(out, "Auto-generated by Studio for kdb+");
            out.close();
        } catch (IOException e) {
            System.err.println("Can't save configuration to " + FILENAME);
            e.printStackTrace(System.err);  //To change body of catch statement use Options | File Templates.
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

    public String[] getQKeywords() {
        String key = p.getProperty("qkeywords", "");
        return split(key);
    }

    public String getLRUServer() {
        return p.getProperty("lruServer", "");
    }

    public void setLRUServer(Server s) {
        if (s == null) return; // May be it should be an exception ?

        p.put("lruServer", s.getFullName());
        save();
    }


    public void saveQKeywords(String[] keywords) {
        p.put("qkeywords", String.join(",",keywords));
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
        connectionString = connectionString.trim();
        if (connectionString.startsWith("`")) connectionString = connectionString.substring(1);
        if (connectionString.startsWith(":")) connectionString = connectionString.substring(1);

        String[] nodes = connectionString.split(":");
        if (nodes.length < 2) {
            throw new IllegalArgumentException("Wrong format of connection string");
        }

        String host = nodes[0];
        int port = Integer.parseInt(nodes[1]); // could throw NumberFormatException

        String auth = getDefaultAuthMechanism();
        String user, password;
        if (nodes.length == 2) {
            Credentials credentials = getDefaultCredentials(auth);
            user = credentials.getUsername();
            password = credentials.getPassword();
        } else {
            user = nodes[2];
            password = nodes.length > 3 ? Stream.of(nodes).skip(3).collect(Collectors.joining(":")) : "";
        }

        Color bgColor = Config.getInstance().getDefaultBackgroundColor();

        for (Server s: getServers()) {
            if (s.getHost().equals(host) && s.getPort() == port && s.getUsername().equals(user) && s.getPassword().equals(password)) {
                return s;
            }
        }

        return new Server("", host, port, user, password, bgColor, auth, false);
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

    public void setServerListBounds(Rectangle rectangle) {
        p.setProperty("serverList.x", "" + (int)rectangle.getX());
        p.setProperty("serverList.y", "" + (int)rectangle.getY());
        p.setProperty("serverList.width", "" + (int)rectangle.getWidth());
        p.setProperty("serverList.height", "" + (int)rectangle.getHeight());
        save();
    }

    public Rectangle getServerListBounds() {
        String strX = p.getProperty("serverList.x");
        String strY = p.getProperty("serverList.y");
        String strWidth = p.getProperty("serverList.width");
        String strHeight = p.getProperty("serverList.height");

        if (strX != null && strY != null && strWidth != null && strHeight != null) {
            return new Rectangle(Integer.parseInt(strX), Integer.parseInt(strY),
                                Integer.parseInt(strWidth), Integer.parseInt(strHeight));
        }

        DisplayMode displayMode = GraphicsEnvironment.getLocalGraphicsEnvironment()
                                            .getDefaultScreenDevice().getDisplayMode();

        int width = displayMode.getWidth();
        int height = displayMode.getHeight();

        int w = Math.min(width / 2, ServerList.DEFAULT_WIDTH);
        int h = Math.min(height / 2, ServerList.DEFAULT_HEIGHT);
        int x = (width - w) / 2;
        int y = (height - h) / 2;
        return new Rectangle(x,y,w,h);
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
        String authenticationMechanism = p.getProperty("server." + key + ".authenticationMechanism", DefaultAuthenticationMechanism.NAME);
        boolean useTLS = Boolean.parseBoolean(p.getProperty("server." + key + ".useTLS", "false"));
        Color c = new Color(Integer.parseInt(backgroundColor, 16));
        return new Server("", host, port, username, password, c, authenticationMechanism, useTLS);
    }

    private Server initServerFromProperties(int number) {
        return initServerFromKey("" + number);
    }

    private void convertFromOldVerion() {
        try {
            System.out.println("Found old config. Converting...");
            String[] names = p.getProperty("Servers","").split(",");
            List<Server> list = new ArrayList<>();
            for (String name : names) {
                Server server = initServerFromKey(name);
                server.setName(name);
                list.add(server);
            }
            p.remove("Servers");
            p.entrySet().removeIf(e -> e.getKey().toString().startsWith("server."));
            p.setProperty("version", VERSION);
            initServers();
            addServers(list.toArray(new Server[0]));
            System.out.println("Done");
        } catch (IllegalArgumentException e) {
            System.err.println("Ups... Can't convert: " + e);
            e.printStackTrace(System.err);
        }
    }

    private void initServers() {
        if (p.getProperty("version","").equals(OLD_VERSION)) {
            convertFromOldVerion();
        }
        serverNames = new ArrayList<>();
        serverTree = new ServerTreeNode();
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
            throw new IllegalArgumentException("Server with full name " + fullName + " already exist");
        }
        if (name.trim().length() == 0) {
            throw new IllegalArgumentException("Server name can't be empty");
        }
        //this would break if the user's config happened to contain these characters, causing them to lose their config
        //if (name.contains(",")) {
        //    throw new IllegalArgumentException("Server name can't contain ,");
        //}
        //if (name.contains("/")) {
        //    throw new IllegalArgumentException("Server name can't contain /");
        //}
        servers.put(fullName, server);
        serverNames.add(fullName);
    }


    public void addServer(Server server) {
        addServers(server);
    }

    public void addServers(Server... newServers) {
        Properties backup = new Properties();
        backup.putAll(p);
        try {
            for (Server server : newServers) {
                ServerTreeNode folder = server.getFolder();
                if (folder == null) {
                    server.setFolder(serverTree);
                    serverTree.add(server);
                } else {
                    folder.add(server);
                }
                addServerInternal(server);
            }

            saveAllServers();
        } catch (IllegalArgumentException e) {
            p = backup;
            initServers();
            throw e;
        }

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

}
