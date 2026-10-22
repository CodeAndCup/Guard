package fr.perrier.guard.sql;

import fr.perrier.guard.*;
import lombok.*;

@Getter
public class Manager {
    private final Connection connection;

    public Manager() {

        String host = Guard.getDefaultConfig().getString("database.host");
        String user = Guard.getDefaultConfig().getString("database.user");
        String pass = Guard.getDefaultConfig().getString("database.password");
        String dbname = Guard.getDefaultConfig().getString("database.name");
        int port = Guard.getDefaultConfig().getInt("database.port");

        this.connection = new Connection(new Credentials(host,user,pass,dbname,port));


    }

    public void close() {
        try {
            this.connection.close();
        }catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
