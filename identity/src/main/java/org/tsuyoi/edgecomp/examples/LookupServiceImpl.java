package org.tsuyoi.edgecomp.examples;

import org.tsuyoi.edgecomp.examples.identity.LookupRequest;
import org.tsuyoi.edgecomp.examples.identity.LookupResult;
import org.tsuyoi.edgecomp.examples.identity.LookupService;

public class LookupServiceImpl implements LookupService {
    private String fakeUserName;
    private String fakeFirstName;
    private String fakeLastName;

    public LookupServiceImpl(String userName, String firstName, String lastName) {
        this.fakeUserName = userName;
        this.fakeFirstName = firstName;
        this.fakeLastName = lastName;
    }

    @Override
    public LookupResult lookup(LookupRequest request) {
        return new LookupResult(request, fakeUserName, fakeFirstName, fakeLastName);
    }
}
