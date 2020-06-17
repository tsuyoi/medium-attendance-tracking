package org.tsuyoi.edgecomp.examples.lookup;

import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.tsuyoi.edgecomp.examples.identity.LookupResult;

import java.io.IOException;

public class LookupClient {
    private String url;
    private int port;
    private String path;
    private String param;
    private String username;
    private String password;

    public LookupClient(String url, int port, String path, String param, String username, String password) {
        setUrl(url);
        setPort(port);
        setPath(path);
        setParam(param);
        setUsername(username);
        setPassword(password);
    }

    private String getUrlForIdQuery(String id) {
        return String.format("http://%s:%d/%s?%s=%s", getUrl(), getPort(), getPath(), getParam(), id);
    }

    public LookupResult lookupUserInfo(String id) {
        LookupResult ret = null;
        HttpGet request = new HttpGet(getUrlForIdQuery(id));
        CredentialsProvider provider = new BasicCredentialsProvider();
        if (getUsername() != null) {
            provider.setCredentials(
                    AuthScope.ANY,
                    new UsernamePasswordCredentials(getUsername(), getPassword())
            );
        }
        try (CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(provider)
                .build();
             CloseableHttpResponse response = httpClient.execute(request)) {

            System.out.println(response.getStatusLine().getStatusCode());

            HttpEntity entity = response.getEntity();
            if (entity != null) {
                Gson gson = new Gson();
                try {
                    ret = gson.fromJson(EntityUtils.toString(entity), LookupResult.class);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println(e.getMessage());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        }
        return ret;
    }

    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }

    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }

    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }

    public String getParam() {
        return param;
    }
    public void setParam(String param) {
        this.param = param;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}
