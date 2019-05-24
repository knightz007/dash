package com.devops.dash.Dao;

import com.devops.dash.Entity.Server;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Repository
@Qualifier("CustomData")
public class ServerDaoCustomImpl implements ServerDao{

    private static Map<Integer, Server> servers;

    static
    {
        servers = new HashMap<Integer, Server>(){
            {
                put(1, new Server(1,"11.120.1.0", "Chef"));
                put(2, new Server(2, "11.120.1.1", "Jenkins"));
            }
        };
    }

    @Override
    public Collection<Server> getServers() {
        return servers.values();
    }

    @Override
    public Server getServerById(int id) {
        return servers.get(id);
    }

    @Override
    public void updateServer(Server server) {
        Server s = servers.get(server.getId());
        s.setIp(server.getIp());
        s.setServer_function(server.getServer_function());
        servers.put(s.getId(), s);
    }

    @Override
    public void deleteServerById(int id) {
        servers.remove(id);
    }

    @Override
    public void addServer(Server server) {
        servers.put(server.getId(), server);
    }
}
