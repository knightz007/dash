package com.devops.dash.Dao;

import com.devops.dash.Entity.Server;

import java.util.Collection;

public interface ServerDao {
    Collection<Server> getServers();

    Server getServerById(int id);

    void updateServer(Server server);

    void deleteServerById(int id);

    void addServer(Server server);
}
