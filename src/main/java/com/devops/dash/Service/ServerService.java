package com.devops.dash.Service;

import com.devops.dash.Dao.ServerDao;
import com.devops.dash.Entity.Server;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class ServerService  {

    @Autowired
    @Qualifier("MySqlRepo")
    private ServerDao serverDao;

    public Collection<Server> getServers()
    {
        return this.serverDao.getServers();
    }

    public Server getServerById(int id){
        return this.serverDao.getServerById(id);
    }

    public void updateServer(Server server){
        this.serverDao.updateServer(server);
    }

    public void deleteServerById(int id)
    {
        serverDao.deleteServerById(id);
    }
    public void addServer(Server server)
    {
        serverDao.addServer(server);
    }
}
