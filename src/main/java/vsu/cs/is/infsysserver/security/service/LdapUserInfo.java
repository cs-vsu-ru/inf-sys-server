package vsu.cs.is.infsysserver.security.service;

public record LdapUserInfo(
        String surname,
        String givenName,
        String mail
) {
}
