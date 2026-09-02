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
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Andreas
 */

@ManagedBean(name = "loggingBean")
@SessionScoped
public class loggingBean {

    private final ini i;
    private String userLogin;
    //rivate String[] logs;

    /**
     * Creates a new instance of loggingBean
     */
    public loggingBean() {
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

    public List<logs> Loggs() {
        List allLogs = new ArrayList();
        String ts;
        PreparedStatement preparedStatement = null;
        Connection connect = null;
        try {
            connect = SQLConnect();
            preparedStatement = connect
                    .prepareStatement("Select * FROM portal.logging");

            ResultSet r = preparedStatement.executeQuery();
            while (r.next()) {
                logs l = new logs();
                l.id = r.getInt("id");
                l.currentUser = r.getString("currentUser");
                l.fault = r.getBoolean("fault");
                l.message = r.getString("message");
                l.exception = r.getString("exception");
                l.timestamp = r.getString("time");
                allLogs.add(l);
            }
            connect.close();

            //Collections.sort(allLogs, Collections.reverseOrder());
            return allLogs;
        } catch (Exception ex) {
            Logger.getLogger(loggingBean.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }


    public void message(boolean fault, String message, String exception) {

        PreparedStatement preparedStatement = null;
        String now = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date());
        if (i.isLogEnable()) {
            HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
            String ipAddress = request.getHeader("X-FORWARDED-FOR");
            if (ipAddress == null) {
                ipAddress = request.getRemoteAddr();
            }
            String currentUserName = LoggedInUser();
            String currentRemoteUser = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
            if (currentRemoteUser == null) {
                currentRemoteUser = "Portal";
            }
            String CurrentUser = currentUserName + " : " + currentRemoteUser;
            if (CurrentUser == null) {
                CurrentUser = "Portal";
            }
            //DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG);
            Connection connect = null;
            try {
                connect = SQLConnect();
                preparedStatement = connect
                        .prepareStatement("insert into portal.logging values (default, ?, ? , ?, ?, ?, ?)");
                // "uid, message, pc");
                // Parameters start with 1
                preparedStatement.setBoolean(1, fault);
                preparedStatement.setString(2, CurrentUser);
                preparedStatement.setString(3, message);
                preparedStatement.setString(4, ipAddress);
                preparedStatement.setString(5, now);
                preparedStatement.setString(6, exception);
                preparedStatement.executeUpdate();
                connect.close();
            } catch (Exception ex) {
                Logger.getLogger(loggingBean.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
