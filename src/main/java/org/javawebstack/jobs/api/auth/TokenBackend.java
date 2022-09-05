package org.javawebstack.jobs.api.auth;

public interface TokenBackend {

    String createToken(String username);
    void deleteToken(String token);
    String checkToken(String token);

}
