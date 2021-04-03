package org.tsuyoi.edgecomp.identity;

import com.google.gson.Gson;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LookupResult {
    private final Date created = new Date();

    private LookupRequest request;
    private boolean success;
    private String userName;
    private String userEmail;
    private String userFirstName;
    private String userLastName;

    public LookupResult(LookupRequest request) {
        setRequest(request);
        setSuccess(false);
    }

    public LookupResult(LookupRequest request, String username, String email, String firstName, String lastName) {
        this(request);
        setSuccess(true);
        setUserName(username);
        setUserEmail(email);
        setUserFirstName(firstName);
        setUserLastName(lastName);
    }

    public LookupRequest getRequest() {
        return request;
    }
    public void setRequest(LookupRequest request) {
        this.request = request;
    }

    public boolean getSuccess() {
        return success;
    }
    public void setSuccess(boolean success) {
        this.success = success;
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

    public String getUserEmail() {
        return userEmail;
    }
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
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
        properties.put("request", getRequest().toJson());
        properties.put("success", getSuccess());
        properties.put("created", getCreated());
        properties.put("userName", getUserName());
        properties.put("userEmail", getUserEmail());
        properties.put("userFirstName", getUserFirstName());
        properties.put("userLastName", getUserLastName());
        return properties;
    }
}
