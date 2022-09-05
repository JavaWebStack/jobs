package org.javawebstack.jobs.api.auth;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NoAuthProvider implements AuthProvider {

    String username;

    public boolean hasLogin() {
        return false;
    }

    public String login(String username, String password) {
        return null;
    }

    public String checkToken(String token) {
        return username;
    }

}
