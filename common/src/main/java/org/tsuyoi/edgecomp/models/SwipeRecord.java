package org.tsuyoi.edgecomp.models;

import org.tsuyoi.edgecomp.identity.LookupResult;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
@Table( name = "swipe_record" )
public class SwipeRecord {
    @Id
    private String id;
    @Column( name = "ts" )
    private Long ts;
    @Column( name = "site" )
    private String site;

    @Column( name = "swipe" )
    private String swipe;
    @Column( name = "user_id" )
    private String userId;
    @Column( name = "user_first_name" )
    private String userFirstName;
    @Column( name = "user_last_name" )
    private String userLastName;
    @Column( name = "user_email" )
    private String userEmail;
    @Column( name = "error_msg" )
    private String error;


    @Column( name = "cresco_region" )
    private String crescoRegion;
    @Column( name = "cresco_agent" )
    private String crescoAgent;
    @Column( name = "cresco_plugin" )
    private String crescoPlugin;

    public SwipeRecord() {
        this.id = java.util.UUID.randomUUID().toString();
        this.ts = new Date().getTime();
    }
    
    public SwipeRecord(String site, String swipe) {
        this();
        setSite(site);
        setSwipe(swipe);
    }

    public SwipeRecord(String site, String swipe, String id) {
        this(site, swipe);
        setUserId(id);
    }

    public SwipeRecord(String site, String swipe, String id, String region, String agent, String plugin) {
        this(site, swipe, id);
        setCrescoRegion(region);
        setCrescoAgent(agent);
        setCrescoPlugin(plugin);
    }

    public String getCrescoRegion() {
        return crescoRegion;
    }
    public void setCrescoRegion(String region) {
        this.crescoRegion = region;
    }

    public String getCrescoAgent() {
        return crescoAgent;
    }
    public void setCrescoAgent(String agent) {
        this.crescoAgent = agent;
    }

    public String getCrescoPlugin() {
        return crescoPlugin;
    }
    public void setCrescoPlugin(String plugin) {
        this.crescoPlugin = plugin;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public Long getTs() {
        return ts;
    }
    public Date getTsAsDate() {
        return new Date(ts);
    }
    public void setTs(Long ts) {
        this.ts = ts;
    }
    public void setTs(String ts) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ");
        try {
            this.ts = dateFormat.parse(ts).getTime();
        } catch (ParseException e) {
            this.ts = null;
        }
    }

    public String getSite() {
        return site;
    }
    public void setSite(String site) {
        this.site = site;
    }

    public String getSwipe() {
        return swipe;
    }
    public void setSwipe(String swipe) {
        this.swipe = swipe;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getUserEmail() {
        return userEmail;
    }
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getError() {
        return error;
    }
    public void setError(String error) {
        this.error = error;
    }

    public void addLookupResult(LookupResult lookupResult) {
        if (lookupResult != null) {
            setUserEmail(lookupResult.getUserEmail());
            setUserFirstName(lookupResult.getUserFirstName());
            setUserLastName(lookupResult.getUserLastName());
        } else {
            setError("LookupResult was null");
        }
    }

    @Override
    public String toString() {
        return "SwipeRecord: "
                + " On " + getTsAsDate()
                + " from " + getSite()
                + " swipped " + getSwipe()
                + ((getUserId() != null) ? " with ID " + getUserId() : "")
                ;
    }
}
