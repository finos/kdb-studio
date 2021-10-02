package studio.utils;

import org.junit.jupiter.api.Test;
import studio.core.Credentials;
import studio.core.DefaultAuthenticationMechanism;
import studio.kdb.Server;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class QPadConverterTest {

    @Test
    public void testConvert() {
        Credentials cred = new Credentials("aUser", "aPassword");

        Server server = QPadConverter.convert("`:server.com:11223`name", "authMethod", cred);
        assertEquals("server.com", server.getHost());
        assertEquals(11223, server.getPort());
        assertEquals("name", server.getName());
        assertEquals("authMethod", server.getAuthenticationMechanism());
        assertEquals(cred.getUsername(), server.getUsername());
        assertEquals(cred.getPassword(), server.getPassword());

        server = QPadConverter.convert("`:server.com:11223`Parent`folder`name", "auth", cred);
        assertEquals("Parent/folder/name", server.getFullName());
        assertEquals("name", server.getName());

        server = QPadConverter.convert("`:server.com:11223:user:password`Parent`folder`name", "auth", cred);
        assertEquals("Parent/folder/name", server.getFullName());
        assertEquals("user", server.getUsername());
        assertEquals("password", server.getPassword());
        assertEquals(DefaultAuthenticationMechanism.NAME, server.getAuthenticationMechanism());

        server = QPadConverter.convert("`:server.com:11223:user:testAuth?password`Parent`folder`name", "auth", cred);
        assertEquals("Parent/folder/name", server.getFullName());
        assertEquals("user", server.getUsername());
        assertEquals("testAuth?password", server.getPassword());
        assertEquals("testAuth", server.getAuthenticationMechanism());


        server = QPadConverter.convert("#`:server.com:11223:user:auth?password`Parent`folder`name", "auth", cred);
        assertEquals(null, server);

        server = QPadConverter.convert("","auth", cred);
        assertEquals(null, server);

        server = QPadConverter.convert("`:server.com:11223","auth", cred);
        assertEquals(null, server);

        server = QPadConverter.convert("server.com:11223`root`folder`somename","auth", cred);
        assertEquals(null, server);

        server = QPadConverter.convert("`:server.com:port`name","auth", cred);
        assertEquals(null, server);

        server = QPadConverter.convert("`:server.com`name","auth", cred);
        assertEquals(null, server);

        server = QPadConverter.convert("`:server.com:11223:user`name","auth", cred);
        assertEquals("", server.getPassword());
        assertEquals("user", server.getUsername());

        server = QPadConverter.convert("`:server.com:11223`folder``server","auth", cred);
        assertEquals("[empty]", server.getFolder().getFolder());

        server = QPadConverter.convert("`:server.com:11223:user:password:something`name", "auth", cred);
        assertEquals("password:something", server.getPassword());
    }

}
