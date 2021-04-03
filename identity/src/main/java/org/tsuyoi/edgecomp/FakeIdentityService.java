package org.tsuyoi.edgecomp;

import org.tsuyoi.edgecomp.identity.IdentityService;
import org.tsuyoi.edgecomp.identity.LookupRequest;
import org.tsuyoi.edgecomp.identity.LookupResult;

public class FakeIdentityService implements IdentityService {
    private String fakeUserName;
    private String fakeEmail;
    private String fakeFirstName;
    private String fakeLastName;

    public FakeIdentityService(String userName, String email, String firstName, String lastName) {
        this.fakeUserName = userName;
        this.fakeEmail = email;
        this.fakeFirstName = firstName;
        this.fakeLastName = lastName;
    }

    @Override
    public LookupResult lookup(LookupRequest request) {
        return new LookupResult(request, fakeUserName, fakeEmail, fakeFirstName, fakeLastName);
    }
}
