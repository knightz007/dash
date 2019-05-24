package com.devops.dash.Entity;

public class Server {

    private int id;
    private String ip;
    private String server_function;

    public Server(int id, String ip, String server_function) {
        this.id = id;
        this.ip = ip;
        this.server_function = server_function;
    }

    public Server(){}

    public int getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }

    public String getServer_function() {
        return server_function;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setServer_function(String server_function) {
        this.server_function = server_function;
    }
}