/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
//TODO Meldungen in den Verlauf eintragen
package rbz.jamf;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import rbz.portal.LDAP;
import rbz.portal.LDAPResult;
import rbz.portal.MailerIpad;
import rbz.portal.ini;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


/**
 * @author andreas
 */
@Named(value = "jamf")
@ViewScoped
public class jamf implements Serializable {
    private String uidNumber;
    private String givenName;
    private String userName;


    private String userClass;

    private ini i;
    private JamfUserData selectedUser;

    //---------------Member------------------//
    public jamf() {
        this.i = new ini();
//        this.uidNumber = getUidNumber();
    }


    /**
     * @return
     */
    public String getStatus() {
        String query;
        String s = uidNumber;
        String charset = "UTF-8";
        HttpResponse<String> httpResponse;
        try {
            query = String.format("uidNumber=%s",
                    URLEncoder.encode(s, charset));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        try {
            httpResponse = Unirest.get("http://localhost:8081/get" + "?" + query)
                    .basicAuth("admin", "LK18global")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .asString();
        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }
        String str = httpResponse.getBody().toString();
        return str;
    }

    public String add() {
        //String uidnumer = uidNumber;
        LDAPResult result = new LDAPResult();
        LDAP ldap = new LDAP();
        result = ldap.getUserData("", uidNumber, "");
        ldap.setRentStatus(uidNumber, "requestedByTeacher");
        MailerIpad mail = new MailerIpad(true);
        mail.setThreadName("AddMailversand");
        mail.setEmail(result.getUser().getUserEmail());
        mail.setUidNumber(uidNumber);
        mail.start();
        return "classAdmin.xhtml?faces-redirect=true";
    }

    public String remove() {
        //String uidnumer = uidNumber;
        LDAPResult result = new LDAPResult();
        LDAP ldap = new LDAP();
        result = ldap.getUserData("", uidNumber, "");
        ldap.setRentStatus(uidNumber, "n.a");
        MailerIpad mail = new MailerIpad(false);
        mail.setThreadName("RemoveMailversand");
        mail.setEmail(result.getUser().getUserEmail());
        mail.setUidNumber(uidNumber);
        mail.start();
        return "classAdmin.xhtml?faces-redirect=true";
    }

    //-------------------- Getter und Setter:-----------------------------------//
    public String getUidNumber() {
        return uidNumber;
    }

    public void setUidNumber(String uidNumber) {
        this.uidNumber = uidNumber;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getUserClass() {
        return userClass;
    }

    public void setUserClass(String userClass) {
        this.userClass = userClass;
    }


    public JamfUserData getSelectedUser() {
        return selectedUser;
    }

    public ini getI() {
        return i;
    }

    public void setI(ini i) {
        this.i = i;
    }

    public void setSelectedUser(JamfUserData selectedUser) {
        this.selectedUser = selectedUser;
    }
}
