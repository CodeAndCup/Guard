package fr.perrier.guard.sql;

import java.sql.*;
import java.util.*;
import java.util.logging.*;

public class Connection {
    private final Credentials credentials;
    private java.sql.Connection connection;

    public Connection(Credentials credentials) {
        this.credentials = credentials;
        this.connect();
    }

    private void connect() {
        try {
            Class.forName("com.mysql.jdbc.Driver");

            Properties properties = new Properties();
            properties.setProperty("user",this.credentials.getUser());
            properties.setProperty("password",this.credentials.getPass());
            properties.setProperty("useSSL","true");
            properties.setProperty("allowPublicKeyRetrieval","true");

            this.connection = DriverManager.getConnection(this.credentials.toURI(),properties);
            Logger.getLogger("Minecraft").info("Connection à la DB réussi.");
        }catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public void close() throws SQLException {
        if(this.connection != null) {
            if(this.connection.isClosed()) {
                this.connection.close();
            }
        }
    }

    public java.sql.Connection getConnection() throws SQLException {
        if(this.connection != null) {
            if(!this.connection.isClosed()) {
                return this.connection;
            }
        }

        connect();
        return this.connection;
    }

}
