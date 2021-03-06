package studio.core;

import java.util.Properties;

public interface IAuthenticationMechanism {
    String getMechanismName();

    String[] getMechanismPropertyNames();

    void setProperties(Properties props);

    Credentials getCredentials();
}
