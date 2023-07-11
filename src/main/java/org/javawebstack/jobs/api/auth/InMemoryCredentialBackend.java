package org.javawebstack.jobs.api.auth;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class InMemoryCredentialBackend implements CredentialBackend {

    final Map<String, String> users = new HashMap<>();

    public InMemoryCredentialBackend addUser(String username, String password) {
        this.users.put(username, password);
        return this;
    }

    public boolean checkCredentials(String username, String password) {
        if(!users.containsKey(username))
            return false;
        return users.get(username).equals(password);
    }

    /**
     *
     * @return unmodifiable map of known users
     */
    public Map<String, String> getUsers() {
        return Collections.unmodifiableMap(users);
    }
}
