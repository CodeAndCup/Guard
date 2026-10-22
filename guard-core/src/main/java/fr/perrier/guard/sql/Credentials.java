package fr.perrier.guard.sql;

import lombok.*;

public class Credentials {

    private final String host;
    @Getter
    private final String user;
    @Getter
    private final String pass;
    private final String dbname;
    private final int port;

    public Credentials(String host,String user,String pass,String dbname,int port) {
        this.host = host;
        this.user = user;
        this.pass = pass;
        this.dbname = dbname;
        this.port = port;
    }

    public String toURI() {

        return "jdbc:mysql://" +
                host +
                ":" + port +
                "/" + dbname +
                "?characterEncoding=utf8";
    }

}
