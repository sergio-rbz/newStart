/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbz.portal;

import lombok.Data;
import org.wildfly.security.http.oidc.OidcPrincipal;
import org.wildfly.security.http.oidc.OidcSecurityContext;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.management.relation.Role;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.mashape.unirest.http.utils.URLParamEncoder.encode;

/**
 * @author EDV-PC-Andreas
 */
@ManagedBean(name = "Portal")
//@SessionScoped
@ViewScoped
@Data
public final class Portal implements Serializable {

    private String pwreset;
    private final ini i = new ini();
    private String redirectUrl = i.getRedirectUrl();
    private final String LogoutUrl = i.getLogoutUrl();


    private List<WebLink> loggedInWebLinks = new ArrayList();
    private List<WebLink> guestWebLinks = new ArrayList();
    private String loggedInUserRole = new String();
    private String LinkError;
    private String userLogin;
    private String birthday;


    private String gender;


    private String helpLinkTeacher;


    private String requestLink;


    private String helpLinkStudents;
    private String helpLinkAll;

    //public static boolean isDevPlatform;

    public static boolean isDevPlatform() {
        //# semar - is development platform ?
        ini myIni = new ini();
        if (myIni.getLDAPip().equals("127.0.0.1")) {
            System.out.println("=====================================DevPlatform mode: forcing user to 'a.admin' =====================");
            return true;
        }
        return false;
    }

    //Constructor
    public Portal() {
        InitUserRole();
        InitLoggedInLinks();
        InitGuestLinks();
        this.helpLinkTeacher = i.getHelpLinkTeacher(); //Initialisiert den Helplink auf der Loggin Portalseite
        this.helpLinkStudents = i.getHelpLinkStudents(); //Initialisiert den Helplink auf der Loggin Portalseite
        this.helpLinkAll = i.getHelpLinkAll(); //Initialisiert den Helplink auf der Loggin Portalseite
    }

    public void Login() {
        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.redirect("secureUser/logedInPortal.xhtml?faces-redirect=true");

        } catch (IOException ex) {
            //Logging
            Logger.getLogger(Portal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void Logout() {

        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String encodedRedirectUrl = encode(i.getRedirectUrl());
        String finaleLogoutUrl = LogoutUrl + "?post_logout_redirect_uri=" + encodedRedirectUrl + "&client_id=Portal";
        try {
            request.logout();
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.redirect(finaleLogoutUrl);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Portal.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServletException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public userData LoggedInUser() {

        Principal userid = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal();
        OidcPrincipal<OidcSecurityContext> kp = (OidcPrincipal<OidcSecurityContext>) userid;

        //# semar
        if ( ! Portal.isDevPlatform()) {
            userLogin = kp.getOidcSecurityContext().getIDToken().getPreferredUsername();
            userLogin = userLogin.toLowerCase().trim();
        } else {
            userLogin = "admin";
        }

        LDAP ldap = new LDAP();
        LDAPResult r = ldap.getUserData(userLogin, "", "");
        //LDAP CLOSE
        ldap.closeLDAPContext();
        return r.getUser();
    }

    public String GetRequestLink() {
        if (LoggedInUser() != null) {
            return i.getRequestLink() + LoggedInUser().getUserUidNumber();
        }
        return null;
    }

    public void InitUserRole() {


        if (FacesContext.getCurrentInstance().getExternalContext().isUserInRole("AdminsRole")) {
            loggedInUserRole = "AdminsRole";
        }
        if (FacesContext.getCurrentInstance().getExternalContext().isUserInRole("StudentsRole")) {
            loggedInUserRole = "StudentsRole";
        }
        if (FacesContext.getCurrentInstance().getExternalContext().isUserInRole("TeachersRole")) {
            loggedInUserRole = "TeachersRole";
        }
        if (FacesContext.getCurrentInstance().getExternalContext().isUserInRole("GuestRole")) {
            loggedInUserRole = "GuestRole";
        }
        if (FacesContext.getCurrentInstance().getExternalContext().isUserInRole("VerwRole")) {
            loggedInUserRole = "VerwRole";
        }
        if (FacesContext.getCurrentInstance().getExternalContext().isUserInRole("IpadViewRole")) {
            loggedInUserRole = "IpadViewRole";
        }

        //# semar
        if ( Portal.isDevPlatform()) {
            System.out.println("semar - InitUserRole.. set AdminsRole ########################################################");
            loggedInUserRole = "AdminsRole";
        }

    }

    public List<String> sysMessage() {
        SystemMessage mesg;
        List<String> list = new ArrayList();
        return list;
    }

    public void InitGuestLinks() {
        try {
            String role = "GuestRole";
//            if (loggedInUserRole.equals("")) {
//                role = "GuestRole";
//            } else {
//                role = loggedInUserRole;
//            }
            Statement statement;
            Connection connection = SQLConnect();

            statement = connection.createStatement();
            String sqlQuery = "SELECT * FROM WebLinks";
            ResultSet r = statement.executeQuery(sqlQuery);
            guestWebLinks.clear();
            while (r.next()) {
                String[] roles = r.getString("role").split(";");

                for (int i = 0; i < roles.length; i++) {
                    if (roles[i].equalsIgnoreCase("GuestRole")) {
                        WebLink webLink = new WebLink();
                        webLink.setUrl(r.getString("url"));
                        webLink.setName(r.getString("name"));
                        webLink.setDesc(r.getString("desc"));
                        webLink.setRole(r.getString("role"));
                        webLink.setImage(r.getString("image"));
                        webLink.setCat(r.getString("cat"));
                        webLink.setTooltip(r.getString("tooltip"));
                        guestWebLinks.add(webLink);
                        if (role.equalsIgnoreCase("GuestRole")) break;
                    }
                }
            }
            connection.close();
        } catch (Exception ex) {
            LinkError = LinkError + "GetLinks -->" + ex.toString();
        }
    }

    public void InitLoggedInLinks() {

           try {
            String role = null;
            if (loggedInUserRole.equals("")) {
                role = "GuestRole";
            } else {
                role = loggedInUserRole;
            }
            Statement statement;
            Connection connection = SQLConnect();

            statement = connection.createStatement();
            String sqlQuery = "SELECT * FROM WebLinks";
            ResultSet r = statement.executeQuery(sqlQuery);
            loggedInWebLinks.clear();
            while (r.next()) {
                String[] roles = r.getString("role").split(";");

                for (int i = 0; i < roles.length; i++) {
                    if (role.equals(roles[i])) {
                        WebLink webLink = new WebLink();
                        webLink.setUrl(r.getString("url"));
                        webLink.setName(r.getString("name"));
                        webLink.setDesc(r.getString("desc"));
                        webLink.setRole(r.getString("role"));
                        webLink.setImage(r.getString("image"));
                        webLink.setCat(r.getString("cat"));
                        webLink.setTooltip(r.getString("tooltip"));
                        loggedInWebLinks.add(webLink);
                        if (role.equalsIgnoreCase("GuestRole")) break;
                    }
                }
            }
            connection.close();
        } catch (Exception ex) {
            LinkError = LinkError + "GetLinks -->" + ex.toString();
        }
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
            LinkError = LinkError + e.toString();
            return connect = null;
        } catch (SQLException e) {
            LinkError = LinkError + e.toString();
            return connect = null;
        }
    }

    public void CompleteUserData() {
        LDAP ldap = new LDAP();
        LDAPResult r;
        ldap.UntisUpdateLDAP(userLogin, getGender(), getBirthday());
        ldap.closeLDAPContext();
    }

    public Boolean getLoginUserIsInAdminsRole() {

        //# semar
        if ( ! Portal.isDevPlatform() ) {
            return FacesContext.getCurrentInstance().getExternalContext().isUserInRole("AdminsRole");
        } else {
            return true;
        }
    }

    public Boolean getLoginUserIsInTeachersRole() {
        return FacesContext.getCurrentInstance().getExternalContext().isUserInRole("TeachersRole");
    }

    public Boolean getLoginUserIsInStudentsRole() {
        return FacesContext.getCurrentInstance().getExternalContext().isUserInRole("StudentsRole");
    }

    public Boolean getLoginUserIsInGuestsRole() {
        return FacesContext.getCurrentInstance().getExternalContext().isUserInRole("GuestsRole");
    }

    public Boolean getLoginUserIsInVerwRole() {
        return FacesContext.getCurrentInstance().getExternalContext().isUserInRole("VerwRole");
    }

    public Boolean getLoginUserIsInIpadsViewRole() {

        //# semar
        if ( ! Portal.isDevPlatform() ) {
            return FacesContext.getCurrentInstance().getExternalContext().isUserInRole("IpadsViewRole");
        } else {
            return true;
        }
    }

    // Todo REMOVE (2022) Überflüssig nach kompletter Untis Migration
    public Boolean getHasToChange() {
        LDAPResult r;
        LDAP ldap = new LDAP();
        LDAPResult u = ldap.getUserData(userLogin, "", "");
        String birthday = u.getUser().getBirthday();
        String gender = u.getUser().getGender();
        if (birthday.equals("n.a") || gender.equals("n.a")) {
            return true;
        } else {
            return false;
        }
    }
}//class
