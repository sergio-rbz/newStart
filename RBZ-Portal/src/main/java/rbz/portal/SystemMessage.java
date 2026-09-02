/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbz.portal;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import java.sql.*;
import java.util.Enumeration;
import java.util.List;

/**
 * @author EDV-PC-Andreas
 */
@ManagedBean(name = "SystemMessage")
@RequestScoped
public class SystemMessage {

    private final ini i;
    private String message;
    private String display;

    /**
     * Creates a new instance of SystemMessage
     */
    public SystemMessage() {
        this.i = new ini();
        systemMessage();
    }

    private Connection SQLConnect() throws Exception {

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

    public void systemMessage() {
        Connection connection;
        try {
            connection = SQLConnect();
            Statement statement = connection.createStatement();
            String sqlQuery = "SELECT * FROM systemmessage";
            ResultSet result = statement.executeQuery(sqlQuery);
            while (result.next()) {
                display = (result.getString("display"));
                message = (result.getString("message"));
            }
            connection.close();
        } catch (Exception ex) {
            //Logging !!!!!!!!!!!!!!!!
        }
    }//SystemMessage

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the display
     */
    public String getDisplay() {
        return display;
    }

    /**
     * @param display the display to set
     */
    public void setDisplay(String display) {
        this.display = display;
    }

    public void setMessage(List<String> message) {
        this.setMessage(message);
    }

    public String getMessage() {
        return message;
    }
}//class
