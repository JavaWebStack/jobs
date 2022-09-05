package org.javawebstack.jobs.api.auth;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class StaticTokenBackend implements TokenBackend {

    String token;
    String username;

    public String createToken(String username) {
        return token;
    }

    public void deleteToken(String token) {

    }

    public String checkToken(String token) {
        if(this.token.equals(token))
            return username;
        return null;
    }

}
