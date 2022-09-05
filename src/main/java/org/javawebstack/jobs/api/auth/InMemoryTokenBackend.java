package org.javawebstack.jobs.api.auth;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class InMemoryTokenBackend implements TokenBackend {

    final Map<String, String> tokens = new HashMap<>();

    public String createToken(String username) {
        String token = generateToken();
        tokens.put(token, username);
        return token;
    }

    public void deleteToken(String token) {
        tokens.remove(token);
    }

    public String checkToken(String token) {
        if(token == null)
            return null;
        return tokens.get(token);
    }

    protected String generateToken() {
        String charset = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<20; i++)
            sb.append(charset.charAt(random.nextInt(charset.length())));
        return sb.toString();
    }

}
