package org.javawebstack.jobs.api.auth;

import java.util.Map;

public interface AuthProvider {

    /**
     * Decides whether users can log in
     * @return true if login is supported, false if not
     */
    boolean hasLogin();

    /**
     * Implements the login using username+password credentials
     * @param username Provided username
     * @param password Provided password
     * @return If the login was successful returns an authentication token, else returns null
     */
    String login(String username, String password);

    /**
     * Checks whether the provided authentication token is valid and returns the user's identification (username/id)
     * @param token The provided bearer token
     * @return If the token is valid the username of the user, else null
     */
    String checkToken(String token);

    static AuthProvider noAuth() {
        return noAuth("Anonymous");
    }

    static AuthProvider noAuth(String username) {
        return new NoAuthProvider(username);
    }

    static AuthProvider token(String token) {
        return token(token, "Anonymous");
    }

    static AuthProvider token(String token, String username) {
        return new TokenAuthProvider(new StaticTokenBackend(token, username));
    }

    static AuthProvider usernameAndPassword(String username, String password) {
        return new LoginAuthProvider(new InMemoryCredentialBackend().addUser(username, password), new InMemoryTokenBackend());
    }

    static AuthProvider usernameAndPassword(Map<String, String> credentials) {
        InMemoryCredentialBackend backend = new InMemoryCredentialBackend();
        credentials.forEach(backend::addUser);
        return new LoginAuthProvider(backend, new InMemoryTokenBackend());
    }

}
