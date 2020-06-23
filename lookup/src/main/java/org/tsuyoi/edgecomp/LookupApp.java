package org.tsuyoi.edgecomp;

import org.tsuyoi.edgecomp.identity.LookupResult;
import org.tsuyoi.edgecomp.lookup.LookupClient;

public class LookupApp {
    public static void main( String[] args ) {
        LookupClient client = new LookupClient("localhost", 8500,
                "lookup", "id",
                "user", "password");
        LookupResult result = client.lookupUserInfo("1234");
        System.out.println("Result: " + result);
    }
}
