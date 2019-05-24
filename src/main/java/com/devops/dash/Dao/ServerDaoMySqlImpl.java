package com.devops.dash.Dao;

import com.devops.dash.Entity.Server;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;

@Repository
@Qualifier("MySqlData")
public class ServerDaoMySqlImpl implements ServerDao{



    @Override
    public Collection<Server> getServers() {
        return new ArrayList<Server>() {
            {
                add(new Server(1, "11.120.1.6", "Nexus"));
            }
        };
    }

    @Override
    public Server getServerById(int id) {
        return null;
    }

    @Override
    public void updateServer(Server server) {

    }

    @Override
    public void deleteServerById(int id) {

    }

    @Override
    public void addServer(Server server) {

    }
}
