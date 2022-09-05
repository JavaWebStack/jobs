package org.javawebstack.jobs.api.auth;

public interface CredentialBackend {

    boolean checkCredentials(String username, String password);

}
