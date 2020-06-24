package org.tsuyoi.edgecomp.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SwipeRecord {
    private String crescoRegion;
    private String crescoAgent;
    private String crescoPlugin;

    private String id;
    private String date;
    private String site;

    private String swipe;
    private String linkBlue;
    private String fullName;
    private String error;

    public SwipeRecord() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ");
        setDate(dateFormat.format(new Date()));
    }

    public SwipeRecord(String site, String swipe, String id) {
        this();
        setSite(site);
        setSwipe(swipe);
        setId(id);
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

    public String getDate() {
        return date;
    }
    public Date getDateAsDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ");
        try {
            return dateFormat.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }
    public void setDate(String date) {
        this.date = date;
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

    public String getLinkBlue() {
        return linkBlue;
    }
    public void setLinkBlue(String linkBlue) {
        this.linkBlue = linkBlue;
    }

    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getError() {
        return error;
    }
    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "SwipeRecord: "
                + " On " + getDate()
                + " from " + getSite()
                + " swipped " + getSwipe()
                + ((getId() != null) ? " with ID " + getId() : "")
                ;
    }
}
