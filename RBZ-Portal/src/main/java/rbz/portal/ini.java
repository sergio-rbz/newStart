/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbz.portal;

import lombok.Data;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tomcat 9.0.26 JSF 2.2 Primefaces 7.o
 *
 * @author Andreas Rolke rbz am Schuetzenpark
 */
@ManagedBean(name = "ini")
@SessionScoped
@Data
public class ini implements Serializable {

    private static Properties properties;
    private String version;
    private String LDAPip;
    private String FileserverIP;
    private int port;
    private String cn;
    private String AdminsRole;
    private String TeachersRole;
    private String StudentsRole;
    private String RolesOU;
    private String mailAddress;
    private String fromEMail;
    private String mailPassword;
    private String smtpHost;
    private String smtpPort;
    private String smtpAuth;
    private String smtpSSL;
    private String mailSubject;
    private String mailWelcome;
    private String EmailLink;
    private String EmailLinkPWDReset;
    private String redirectUrl;
    private String logoutUrl;
    private String domain;
    private String password;
    private String defaultPassword;
    private String rootOU;
    private boolean logEnable;
    private String MySQLHost;
    private String MySQLUser;
    private String MySQLPassword;
    private boolean loggingEnable;
    private String MySQLDatabase;
    private String MySQLTableLog;
    private String MySQLTableConfig;
    private String MySQLTableTicket;
    private String tls;
    private String HelpLinkTeacher;


    private String requestLink;


    private String MailIPadMessage;
    private String MailNoIPadMessage;
    private String MailIPadSubject;
    private String MailIPadRequestURL;


    private String HelpLinkStudents;
    private String HelpLinkAll;
    private String dateForExitDateDelete;


    private String environment() {
        Properties sysprops = System.getProperties();
        String os = sysprops.getProperty("os.name");
        if (os.equalsIgnoreCase("Windows 11")) {
            return "dev";
        } else {
            return "live";
        }
    }

    public ini() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        InputStream input;
        Properties prop = new Properties();

        try {
            input = classLoader.getResourceAsStream("ini.properties");
            prop.load(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        this.version = prop.getProperty("portal.version");
        this.defaultPassword = prop.getProperty("portal.defaultpassword");
        this.dateForExitDateDelete = prop.getProperty("portal.defaultexitdate");
        this.requestLink = prop.getProperty("portal.ipadrequesturl");

        this.LDAPip = prop.getProperty("ldap.server");
        this.port = Integer.parseUnsignedInt(prop.getProperty("ldap.port"));
        this.cn = prop.getProperty("ldap.cn");
        this.domain = prop.getProperty("ldap.domain");
        try {
            this.password = decryptPassword(prop.getProperty("ldap.password"));
        } catch (Exception ex) {
            Logger.getLogger(ini.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.rootOU = prop.getProperty("ldap.rootou");

        //Rollen Konfiguration
        this.AdminsRole = prop.getProperty("ldap.adminsrole");
        this.TeachersRole = prop.getProperty("ldap.teachersrole");
        this.StudentsRole = prop.getProperty("ldap.studentsrole");
        this.RolesOU = prop.getProperty("ldap.rolesou");
        // Mail Konfiguration:
        this.mailAddress = prop.getProperty("mail.address");
        this.fromEMail = prop.getProperty("mail.from");
        this.mailPassword = prop.getProperty("mail.password");
        this.smtpHost = prop.getProperty("mail.smtphost");
        this.smtpPort = prop.getProperty("mail.smtpport");
        this.smtpAuth = prop.getProperty("mail.smtpauth");
        this.smtpSSL = prop.getProperty("mail.smtpssl");
        this.mailSubject = prop.getProperty("mail.subject");
        this.mailWelcome = prop.getProperty("mail.wellcome");
        this.MailIPadMessage = prop.getProperty("mail.ipadmessage");
        this.MailNoIPadMessage = prop.getProperty("mail.noipadmessage");
        this.MailIPadSubject = prop.getProperty("mail.ipadsubject");
        this.MailIPadRequestURL = this.requestLink;
        this.EmailLink = prop.getProperty("mail.link");
        this.EmailLinkPWDReset = prop.getProperty("mail.resetpasswordlink");
        this.redirectUrl = prop.getProperty("oidc.redirecturl");
        this.logoutUrl = prop.getProperty("oidc.logouturl");

        String StrlogEnable = prop.getProperty("portal.logenable");
        this.logEnable = false;
        if (StrlogEnable.equals("true")) {
            this.logEnable = true;
        }
        if (StrlogEnable.equals("false")) {
            this.logEnable = false;
        }
        this.MySQLHost = prop.getProperty("mysql.host");
        this.MySQLUser = prop.getProperty("mysql.user");
        try {
            this.MySQLPassword = decryptPassword(prop.getProperty("mysql.password"));
        } catch (Exception ex) {
            Logger.getLogger(ini.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.MySQLDatabase = prop.getProperty("mysql.database");
        this.MySQLTableLog = prop.getProperty("mysql.tablelog");
        this.MySQLTableConfig = prop.getProperty("mysql.tableconfig");
        this.MySQLTableTicket = prop.getProperty("mysql.tableticket");
        this.tls = prop.getProperty("ldap.tls");
        this.HelpLinkTeacher = prop.getProperty("help.linkteacher");
        this.HelpLinkStudents = prop.getProperty("help.linkstudents");
        this.HelpLinkAll = prop.getProperty("help.linkall");


    }

    private String decryptPassword(String encryptedPassword) {

        byte[] decrypted;
        try {
            decrypted = Base64.getDecoder().decode(encryptedPassword.getBytes("utf-8"));
            String s = new String(decrypted);
            return s;
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(ini.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "Fehler!!";
    }
}
