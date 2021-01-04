package org.tsuyoi.edgecomp.identity;

import com.google.gson.Gson;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LookupRequest {
    private final String id;
    private final Date created = new Date();

    private final String userId;

    public LookupRequest(String userId) {
        this.id = java.util.UUID.randomUUID().toString();
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public Date getCreated() {
        return created;
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(toJson());
    }

    public Map<String, Object> toJson() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("id", getId());
        properties.put("created", getCreated());
        properties.put("userId", getUserId());
        return properties;
    }
}
