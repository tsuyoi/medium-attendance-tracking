package org.tsuyoi.edgecomp;

import org.tsuyoi.edgecomp.identity.IdentityService;
import org.tsuyoi.edgecomp.identity.LookupRequest;
import org.tsuyoi.edgecomp.identity.LookupResult;

public class IdentityServiceImpl implements IdentityService {
    private String fakeUserName;
    private String fakeFirstName;
    private String fakeLastName;

    public IdentityServiceImpl(String userName, String firstName, String lastName) {
        this.fakeUserName = userName;
        this.fakeFirstName = firstName;
        this.fakeLastName = lastName;
    }

    @Override
    public LookupResult lookup(LookupRequest request) {
        return new LookupResult(request, fakeUserName, fakeFirstName, fakeLastName);
    }
}
