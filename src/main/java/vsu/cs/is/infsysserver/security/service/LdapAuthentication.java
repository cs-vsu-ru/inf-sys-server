package vsu.cs.is.infsysserver.security.service;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vsu.cs.is.infsysserver.security.entity.dto.request.AuthenticationRequest;

@Service
public class LdapAuthentication {
    @Value("${application.security.ldap.host}")
    private String host;
    @Value("${application.security.ldap.port}")
    private int port;
    @Value("${application.security.ldap.email-host}")
    private String emailHost;

    public boolean isConnectionSuccess(AuthenticationRequest authenticationRequest) {

        String username = authenticationRequest.getUsername() + "@" + emailHost;
        String password = authenticationRequest.getPassword();

        try {
            LDAPConnection ldapConnection = new LDAPConnection(host, port, username, password);
            ldapConnection.close();
            return true;
        } catch (LDAPException e) {
            return false;
        }
    }
}
