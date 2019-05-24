package com.devops.dash.Controller;

import com.devops.dash.Entity.Server;
import com.devops.dash.Service.ServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/servers")
public class ServerController {

    @Autowired
    private ServerService serverService;

@RequestMapping(method = RequestMethod.GET)
    public Collection<Server> getServers()
    {
        return this.serverService.getServers();
    }

@RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Server getServerById(@PathVariable("id") int id){
        return this.serverService.getServerById(id);
    }

    @RequestMapping(method = RequestMethod.PUT)
    public void updateServer(@RequestBody Server server){
        this.serverService.updateServer(server);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void deleteServerById(@PathVariable("id") int id)
    {
        serverService.deleteServerById(id);
    }
    @RequestMapping(method = RequestMethod.POST)
    public void addServer(@RequestBody Server server)
    {
        serverService.addServer(server);
    }
}
