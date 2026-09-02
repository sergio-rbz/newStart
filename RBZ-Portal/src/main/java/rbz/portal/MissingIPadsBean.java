/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbz.portal;

import org.wildfly.security.http.oidc.OidcPrincipal;
import org.wildfly.security.http.oidc.OidcSecurityContext;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.security.Principal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Andreas
 */

@ManagedBean(name = "missingIpadsBean")
@SessionScoped
public class MissingIPadsBean {

    private final ini i;
    private String userLogin;
    //rivate String[] logs;

    /**
     * Creates a new instance of loggingBean
     */
    public MissingIPadsBean() {
        this.i = new ini();

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
            connect = DriverManager
                    .getConnection(i.getMySQLHost(), i.getMySQLUser(), i.getMySQLPassword());
            return connect;
        } catch (ClassNotFoundException e) {
            return connect = null;
        } catch (SQLException e) {
            return connect = null;
        }
    }

    public String LoggedInUser() {
        Principal userid = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal();
        if (userid != null) {
            OidcPrincipal<OidcSecurityContext> kp = (OidcPrincipal<OidcSecurityContext>) userid;
            userLogin = kp.getOidcSecurityContext().getIDToken().getPreferredUsername();
            userLogin = userLogin.toLowerCase().trim();
            LDAP ldap = new LDAP();
            LDAPResult r = ldap.getUserData(userLogin, "", "");
            //LDAP CLOSE
            ldap.closeLDAPContext();
            return r.getUser().getUserFullname();
        } else {
            return "Registrierung";
        }
    }

//    public List<logs> missingIpads() {
//        List allLogs = new ArrayList();
//        String ts;
//        PreparedStatement preparedStatement = null;
//        Connection connect = null;
//        try {
//            connect = SQLConnect();
//            preparedStatement = connect
//                    .prepareStatement("Select * FROM portal.logging");
//
//            ResultSet r = preparedStatement.executeQuery();
//            while (r.next()) {
//                logs l = new logs();
//                l.id = r.getInt("id");
//                l.currentUser = r.getString("currentUser");
//                l.fault = r.getBoolean("fault");
//                l.message = r.getString("message");
//                l.exception = r.getString("exception");
//                l.timestamp = r.getString("time");
//                allLogs.add(l);
//            }
//            connect.close();
//
//            //Collections.sort(allLogs, Collections.reverseOrder());
//            return allLogs;
//        } catch (Exception ex) {
//            Logger.getLogger(missingIPadsBean.class.getName()).log(Level.SEVERE, null, ex);
//            return null;
//        }
//    }


    public void MissingIpadsToDatabase(userData user) {

        PreparedStatement preparedStatement = null;
        String now = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date());
        String currentUserName = LoggedInUser();
//            String currentRemoteUser = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
//            if (currentRemoteUser == null) {
//                currentRemoteUser = "Portal";
//            }
//            String CurrentUser = currentUserName + " : " + currentRemoteUser;

        Connection connect = null;
        try {
            connect = SQLConnect();
            preparedStatement = connect
                    .prepareStatement("insert into portal.missingIPads values (default, ?, ?, ?, ?, ?, ?)");
            // Parameters start with 1
            preparedStatement.setString(1, user.userUidNumber);
            preparedStatement.setString(2, user.userLogin);
            preparedStatement.setString(3, user.userClass);
            preparedStatement.setString(4, currentUserName);
            preparedStatement.setString(5, now);
            preparedStatement.setString(6, user.leavingDate);
            preparedStatement.executeUpdate();
            connect.close();
        } catch (Exception ex) {
            Logger.getLogger(MissingIPadsBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

