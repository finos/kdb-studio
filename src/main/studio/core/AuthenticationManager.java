package studio.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class AuthenticationManager {

    private static final Logger log = LogManager.getLogger();

    private static AuthenticationManager instance;
    private Map classMap = new HashMap();

    public Class lookup(String autheticationMethod) {
        return (Class) classMap.get(autheticationMethod);
    }

    public String[] getAuthenticationMechanisms() {
        Set s = classMap.keySet();
        return (String[]) s.toArray(new String[0]);
    }

    public static synchronized AuthenticationManager getInstance() {
        if (instance == null) {
            instance = new AuthenticationManager();
        }
        return instance;
    }

    private AuthenticationManager() {
        DefaultAuthenticationMechanism dam = new DefaultAuthenticationMechanism();
        classMap.put(dam.getMechanismName(), dam.getClass());

        String curDir = System.getProperty("user.dir");
        curDir = curDir + "/plugins";

        //   System.out.println("Looking for plugins at " + curDir);

        File dir = new File(curDir);
        if (!dir.exists()) {
            return;
        }

        FilenameFilter filter = (dir1, name) -> name.endsWith(".jar");

        String[] children = dir.list(filter);
        if (children != null) {
            for (int child = 0; child < children.length; child++) {
                String filename = dir.getAbsolutePath() + "/" + children[child];
                try {
                    URL url = new URL("jar:file:" + filename + "/!/");
                    JarURLConnection conn = (JarURLConnection) url.openConnection();
                    JarFile jarFile = conn.getJarFile();

                    Enumeration e = jarFile.entries();
                    while (e.hasMoreElements()) {
                        JarEntry entry = (JarEntry) e.nextElement();
                        String name = entry.getName();
                        if (!entry.isDirectory() && name.endsWith(".class")) {
                            URLClassLoader loader = new URLClassLoader(new URL[] {url});
                            String externalName =
                                name.substring(0, name.indexOf('.')).replace('/', '.');
                            try {
                                Class c = loader.loadClass(externalName);
                                if (IAuthenticationMechanism.class.isAssignableFrom(c)) {
                                    IAuthenticationMechanism am =
                                        (IAuthenticationMechanism) c.newInstance();
                                    classMap.put(am.getMechanismName(), c);
                                }
                            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | Error e1) {
                            }
                        }
                    }
                } catch (IOException e) {
                    log.error("Error loading plugin {}", filename, e);
                }
            }
        }
    }
}
