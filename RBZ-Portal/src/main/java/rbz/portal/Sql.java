/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbz.portal;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

/**
 * @author andreas
 */
public class Sql {
    private final ini i = new ini();


    public Connection GetSQLConnect() {

        Connection connect = null;
        try {
            // This will load the MySQL driver, each DB has its own driver
            for (Enumeration<Driver> e = DriverManager.getDrivers(); e.hasMoreElements(); ) {
                System.out.println(e.nextElement().getClass().getName());
            }
            Class.forName("org.mariadb.jdbc.Driver");
            // Setup the connection with the DB
            // jdbc:mysql://host/database
            String database = i.getMySQLDatabase();
            String host = i.getMySQLHost() + "/" + database;
            String user = i.getMySQLUser();
            String pwd = i.getMySQLPassword();
            //String connectionString = host + "/" + database + "?user=" + user + "&password=" + "LK18#global";
            connect = DriverManager.getConnection(host, user, pwd);
            return connect;
        } catch (ClassNotFoundException e) {
            return connect = null;
        } catch (SQLException e) {
            return connect = null;
        }
    }
}
