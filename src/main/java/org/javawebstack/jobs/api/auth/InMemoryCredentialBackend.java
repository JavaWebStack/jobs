package org.javawebstack.jobs.api.auth;

import java.util.HashMap;
import java.util.Map;

public class InMemoryCredentialBackend implements CredentialBackend {

    Map<String, String> users = new HashMap<>();

    public InMemoryCredentialBackend addUser(String username, String password) {
        this.users.put(username, password);
        return this;
    }

    public boolean checkCredentials(String username, String password) {
        if(!users.containsKey(username))
            return false;
        return users.get(username).equals(password);
    }

}
