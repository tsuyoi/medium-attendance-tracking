package org.tsuyoi.edgecomp.identity;

import com.google.gson.Gson;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LookupResult {
    private final String id;
    private final Date created = new Date();

    private LookupRequest request;
    private String userName;
    private String userFirstName;
    private String userLastName;

    public LookupResult() {
        this.id = java.util.UUID.randomUUID().toString();
    }

    public LookupResult(LookupRequest request, String userName, String userFirstName, String userLastName) {
        this();
        setRequest(request);
        setUserName(userName);
        setUserFirstName(userFirstName);
        setUserLastName(userLastName);
    }

    public String getId() {
        return id;
    }

    public LookupRequest getRequest() {
        return request;
    }
    public void setRequest(LookupRequest request) {
        this.request = request;
    }

    public Date getCreated() {
        return created;
    }

    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserFirstName() {
        return userFirstName;
    }
    public void setUserFirstName(String userFirstName) {
        this.userFirstName = userFirstName;
    }

    public String getUserLastName() {
        return userLastName;
    }
    public void setUserLastName(String userLastName) {
        this.userLastName = userLastName;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(toJson());
    }

    public Map<String, Object> toJson() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("id", getId());
        properties.put("request", getRequest().toJson());
        properties.put("created", getCreated());
        properties.put("user_name", getUserName());
        properties.put("user_first_name", getUserFirstName());
        properties.put("user_last_name", getUserLastName());
        return properties;
    }
}
