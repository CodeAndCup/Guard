package fr.perrier.guard.sql;

import fr.perrier.guard.*;

import java.sql.*;
import java.sql.Connection;
import java.util.Date;

public class Setup {

    public static void initDB() {
        Connection connection = null;
        Statement statement = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("Connection en cours..");
            final StringBuilder sb = new StringBuilder();
            connection = DriverManager.getConnection(
                    String.valueOf(sb.append("jdbc:mysql://")
                            .append(Guard.getDefaultConfig().getString("database.host"))
                            .append(":").append(Guard.getDefaultConfig().getInt("database.port"))
                            .append("/").append(Guard.getDefaultConfig().getString("database.name"))
                            .append("?characterEncoding=utf8")),
                    Guard.getDefaultConfig().getString("database.user"),
                    Guard.getDefaultConfig().getString("database.password")
            );

            statement = connection.createStatement();

            String sql_vanish = "CREATE TABLE IF NOT EXISTS player_vanish (uuid VARCHAR(36) not null, vanish BOOLEAN not null)";
            //String sql_report = "CREATE TABLE IF NOT EXISTS report (date DATE not null, reportId VARCHAR(36) not null, uuid VARCHAR(36) not null, report VARCHAR(36) not null, message VARCHAR(256), resolved BOOLEAN not null)";

            statement.execute(sql_vanish);
            //statement.execute(sql_report);

            System.out.println("DataBase setup.");
        }catch (Exception e1) {
            e1 .printStackTrace();
        }
    }
}
