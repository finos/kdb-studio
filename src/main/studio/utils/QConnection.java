package studio.utils;

import studio.core.Credentials;
import studio.core.DefaultAuthenticationMechanism;
import studio.kdb.Config;
import studio.kdb.Server;

import java.awt.*;
import java.util.Collection;

public class QConnection {

    public static Server getByConnection(String connection ,String defaultAuth,
                                         Credentials defaultCredentials, Collection<Server> servers) {

        connection = connection.trim();
        if (connection.startsWith("`")) connection = connection.substring(1);
        if (connection.startsWith(":")) connection = connection.substring(1);

        int i0 = connection.indexOf(':');
        if (i0 == -1) {
            throw new IllegalArgumentException("Wrong format of connection string");
        }
        String host = connection.substring(0, i0);

        i0++;
        int i1 = connection.indexOf(':', i0);
        if (i1 == -1) i1 = connection.length();
        int port = Integer.parseInt(connection.substring(i0, i1)); // could throw NumberFormatException

        String auth, user, password;
        if (i1 == connection.length()) {
            auth = defaultAuth;
            user = defaultCredentials.getUsername();
            password = defaultCredentials.getPassword();
        } else {
            i1++;
            int i2 = connection.indexOf(':', i1);
            if (i2 == -1) i2 = connection.length();

            auth = DefaultAuthenticationMechanism.NAME;
            user = connection.substring(i1, i2);
            password = i2 == connection.length() ? "" : connection.substring(i2+1);
        }

        Color bgColor = Config.getInstance().getColor(Config.COLOR_BACKGROUND);

        for (Server s: servers) {
            if (s.getHost().equals(host) && s.getPort() == port && s.getUsername().equals(user) && s.getPassword().equals(password)) {
                return s;
            }
        }

        return new Server("", host, port, user, password, bgColor, auth, false);

    }

}
