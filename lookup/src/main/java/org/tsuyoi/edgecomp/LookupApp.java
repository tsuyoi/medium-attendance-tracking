package org.tsuyoi.edgecomp;

import org.tsuyoi.edgecomp.identity.LookupResult;
import org.tsuyoi.edgecomp.lookup.LookupClient;

public class LookupApp {
    public static void main( String... args ) {
        if (args.length != 1) {
            System.out.println("Usage: java -jar lookup-1.0-SNAPSHOT.jar <id>");
            System.exit(1);
        }
        String id = args[0];
        LookupClient client = new LookupClient("localhost", 8500,
                "lookup", "id",
                "user", "password");
        LookupResult result = client.lookupUserInfo(id);
        System.out.println("Result: " + result);
        System.out.println("Result Created: " + result.getCreated().getTime());
    }
}
