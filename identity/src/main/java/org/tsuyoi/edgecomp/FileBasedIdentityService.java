package org.tsuyoi.edgecomp;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.tsuyoi.edgecomp.identity.IdentityService;
import org.tsuyoi.edgecomp.identity.LookupRequest;
import org.tsuyoi.edgecomp.identity.LookupResult;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class FileBasedIdentityService implements IdentityService {
    private final Map<String, Identity> identities;

    public FileBasedIdentityService(Path identitiesFile) throws IOException {
        Gson gson = new Gson();
        Type identityMapType = new TypeToken<Map<String, Identity>>() {}.getType();
        identities = gson.fromJson(Files.newBufferedReader(identitiesFile), identityMapType);
    }

    @Override
    public LookupResult lookup(LookupRequest request) {
        try {
            Identity identity = identities.get(request.getUserId());
            System.out.println("Identity: " + identity);
            if (identity != null)
                return new LookupResult(request, identity.getUsername(), identity.getEmail(), identity.getFirstName(), identity.getLastName());
            else
                return new LookupResult(request);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static class Identity {
        private final String username;
        private final String email;
        private final String firstName;
        private final String lastName;

        public Identity(String username, String email, String firstName, String lastName) {
            this.username = username;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public String getUsername() {
            return username;
        }

        public String getEmail() {
            return email;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }
    }
}
