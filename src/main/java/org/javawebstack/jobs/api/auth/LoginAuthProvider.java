package org.javawebstack.jobs.api.auth;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LoginAuthProvider implements AuthProvider {

    CredentialBackend credentialBackend;
    TokenBackend tokenBackend;

    public boolean hasLogin() {
        return false;
    }

    public String login(String username, String password) {
        if(credentialBackend.checkCredentials(username, password))
            return tokenBackend.createToken(username);
        return null;
    }

    public String checkToken(String token) {
        return tokenBackend.checkToken(token);
    }

}
