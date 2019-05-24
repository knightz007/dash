package com.devops.dash.Dao;

import com.devops.dash.Entity.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

@Repository("MySqlRepo")
public class DaoDashMySql implements ServerDao{

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static class ServerRowMapper implements RowMapper<Server>{
        @Override
        public Server mapRow(ResultSet resultSet, int i) throws SQLException {
            Server server = new Server();
            server.setId(resultSet.getInt("id"));
            server.setIp(resultSet.getString("ip"));
            server.setServer_function(resultSet.getString("server_function"));
            return server;
        }
    }

    @Override
    public Collection<Server> getServers() {
        final String sql = "select id, ip, server_function from Servers";
        List<Server> servers = jdbcTemplate.query(sql, new ServerRowMapper());

        return servers;
    }

    @Override
    public Server getServerById(int id) {

        final String sql = "Select id, ip, server_function from Servers where id = ?";
        Server server = jdbcTemplate.queryForObject(sql, new ServerRowMapper(), id);
        return server;
    }

    @Override
    public void updateServer(Server server) {
        final String sql = "update Servers set ip=?, server_function=? where id=?";
        int id = server.getId();
        String ip = server.getIp();
        String server_function = server.getServer_function();
        jdbcTemplate.update(sql, new Object[] { ip, server_function, id } );
    }

    @Override
    public void deleteServerById(int id) {

        final String sql = "delete from Servers where id=?";
        jdbcTemplate.update(sql, new Object[]{id});
    }

    @Override
    public void addServer(Server server) {
        final String sql = "Insert into Servers (ip, server_function) values (?, ?)";
        String ip = server.getIp();
        String server_function = server.getServer_function();
        jdbcTemplate.update(sql, new Object[] {ip, server_function});
    }
}
