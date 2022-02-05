package studio.kdb;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import studio.core.AuthenticationManager;
import studio.core.Credentials;
import studio.core.IAuthenticationMechanism;

public class ConnectionPool {

    private static final Logger log = LogManager.getLogger();

    private final static ConnectionPool instance = new ConnectionPool();
    private final Map<Server, List<Connection>> freeMap = new HashMap<>();
    private final Map<Server, List<Connection>> busyMap = new HashMap<>();

    public static ConnectionPool getInstance() {
        return instance;
    }

    private ConnectionPool() {
    }

    public synchronized void purge(Server s) {
        List<Connection> list = freeMap.computeIfAbsent(s, k -> new LinkedList<>());
        for (Connection c : list) {
            c.c.close();
        }
        list.clear();
        busyMap.put(s, new LinkedList<>());
    }

    public synchronized kx.c leaseConnection(Server s) {
        List<Connection> list = freeMap.computeIfAbsent(s, k -> new LinkedList<>());
        List<Connection> dead = new LinkedList<>();

        Connection connection = null;
        long now = System.currentTimeMillis();
        for (Connection value : list) {
            if (!value.c.isClosed() && now < value.expiration) {
                connection = value;
                break;
            }
            dead.add(value);
        }

        list.removeAll(dead);

        if (connection == null) {
            try {
                kx.c c;
                Class<?> clazz = AuthenticationManager.getInstance().lookup(s.getAuthenticationMechanism());
                IAuthenticationMechanism authenticationMechanism = (IAuthenticationMechanism) clazz.getDeclaredConstructor().newInstance();

                authenticationMechanism.setProperties(s.getAsProperties());
                Credentials credentials = authenticationMechanism.getCredentials();
                if (credentials.getUsername().length() > 0) {
                    String p = credentials.getPassword();
                    c = new kx.c(s.getHost(), s.getPort(),
                        credentials.getUsername() + ((p.length() == 0) ? "" : ":" + p),
                        s.getUseTLS());
                } else {
                    c = new kx.c(s.getHost(), s.getPort(), "", s.getUseTLS());
                }
                c.setEncoding(Config.getInstance().getEncoding());
                connection = new Connection(c, authenticationMechanism.getExpiration());
            } catch (Exception ex) {
                log.error("Failed to initialize connection", ex);
                return null;
            }
        } else {
            list.remove(connection);
        }

        list = busyMap.computeIfAbsent(s, k -> new LinkedList<>());
        list.add(connection);

        return connection.c;
    }

    public synchronized void freeConnection(Server s, kx.c c) {
        if (c == null) {
            return;
        }

        List<Connection> list = busyMap.computeIfAbsent(s, k -> new LinkedList<>());

        Connection connection = list.stream().filter(o -> o.c == c).findFirst().orElse(null);
        if (connection == null) { // If c not in our busy list it has been purged, so close it
            c.close();
        } else if (!c.isClosed()) {
            list = freeMap.get(s);
            if (list == null) {
                c.close();
            } else {
                list.add(connection);
            }
        }
    }

    private static class Connection {
        final kx.c c;
        final long expiration;

        private Connection(kx.c c, long expiration) {
            this.c = c;
            this.expiration = expiration;
        }
    }

}
