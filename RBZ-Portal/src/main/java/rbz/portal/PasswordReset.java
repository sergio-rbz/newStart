/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbz.portal;

import org.wildfly.security.http.oidc.OidcPrincipal;
import org.wildfly.security.http.oidc.OidcSecurityContext;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.validator.ValidatorException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author andreas
 */
@ManagedBean(name = "PasswordReset")
@SessionScoped
public class PasswordReset implements Serializable {

    //private final LDAP ldap = new LDAP();
    private final ini i = new ini();
    private String userLogin;
    private String encodedUserLogin;
    private String uidNumber;
    private List classes;
    private String classname;
    private List classmembers;
    private String passwordKnown;
    private String password1;
    private String password2;
    private String result;
    private String MessageForEmailReminder;
    private String MessageForEmailReminderFault;
    private String userEmail;
    private String message;
    //    private final settings set = new settings();
//    private final loggingBean log = new loggingBean();
    private UIInput password1Input;
    private String fullName;

    /**
     * Creates a new instance of PasswordReset
     */
    public PasswordReset() {
        this.MessageForEmailReminder = "Überprüfen sie bitte ihr Email Postfach. <br><br>Zum Ändern ihres Passwortes folgen sie dem Link in der Email.";
        this.MessageForEmailReminderFault = "Ihre Emailadresse ist nicht im System hinterlegt."
                + "<p> - Schreibfehler in der Adresse?"
                + "<p> - Nicht registriert?";
    }

    public void OnClassesChanged() {
        LDAP ldap = new LDAP();
        LDAPResult r = ldap.GetClassOUMembers(classname, 1);
        classmembers = new LinkedList();
        if (r.isSuccess()) {
            for (userData member : r.getUserList()) {
                String m = member.getUserLogin();
                classmembers.add(m);
            }
        }
        ldap.closeLDAPContext();
    }

    public String adminResetPassword() {
        LDAP ldap = new LDAP();
        userLogin = userLogin.toLowerCase().trim();
        LDAPResult udata = ldap.getUserData(userLogin, "", "");
        LDAPResult r = ldap.ResetPassword(userLogin, password2.trim(), udata.getUser().getUserClass(), true);
        if (r.isSuccess()) {
            ldap.PasswordChangeAllow(userLogin, classname, false);
            result = userLogin + ": Passwortänderung erfolgreich!";
            result = userLogin + ": Passwortänderung erfolgt!";
            ldap.closeLDAPContext();
            return "/Password/AdminPasswordResetOK";
        } else {
            result = userLogin + ": Passwortänderung nicht erfolgt!";
            ldap.closeLDAPContext();
            return "/Password/AdminPasswordResetFault";
        }
    }

    public String adminStandardPassword() {
        LDAP ldap = new LDAP();
        userLogin = userLogin.toLowerCase().trim();
        LDAPResult udata = ldap.getUserData(userLogin, "", "");
        LDAPResult r = ldap.ResetPassword(userLogin, i.getDefaultPassword(), udata.getUser().getUserClass(), true);
        if (r.isSuccess()) {
            ldap.PasswordChangeAllow(userLogin, classname, false);
            result = userLogin + ": Passwortänderung Standard erfolgreich!";
            //LOGGING
//                log.Meldung("Passwort geändert: " + user + " ," + classname);
            result = userLogin + ": Passwortänderung auf Standard erfolgt!";
            ldap.closeLDAPContext();
            return "/Password/AdminPasswordResetOK";
        } else {
            result = userLogin + ": Passwortänderung auf Standard nicht erfolgt!";
            ldap.closeLDAPContext();
            return "/Password/AdminPasswordResetFault";
        }
    }


    public String ResetPassword() {
        LDAP ldap = new LDAP();
        userLogin = userLogin.toLowerCase().trim();
        LDAPResult udata = ldap.getUserData(userLogin, "", "");
        LDAPResult r = ldap.ResetPassword(userLogin, password2.trim(), udata.getUser().getUserClass(), false);
        if (r.isSuccess()) {
            ldap.PasswordChangeAllow(userLogin, classname, false);
            result = userLogin + ": Passwortänderung erfolgreich!";
            //LOGGING
//                log.Meldung("Passwort geändert: " + user + " ," + classname);
            result = userLogin + ": Passwortänderung erfolgt!";
            ldap.closeLDAPContext();
            return "pwresetOK";
        } else {
            result = userLogin + ": Passwortänderung nicht erfolgt!";
            ldap.closeLDAPContext();
            return "pwresetFault";
        }
    }

    public String SetNewPassword() {
        LDAP ldap = new LDAP();
        userLogin = userLogin.toLowerCase().trim();
        LDAPResult udata = ldap.getUserData(userLogin, "", "");
        LDAPResult r = ldap.ResetPassword(userLogin, password2.trim(), udata.getUser().getUserClass(), true);
        if (r.isSuccess()) {
            result = userLogin + ": Passwortänderung erfolgreich!";
            //LOGGING
            //log.Meldung("Passwort geändert: " + user + " ," + classname);
            result = userLogin + ": Passwortänderung erfolgt!";
            ldap.closeLDAPContext();
            return "pwresetOK";
        } else {
            result = userLogin + ": Passwortänderung nicht erfolgt!";
            ldap.closeLDAPContext();
            return "reset_via_mail_Fault";
        }
    }

    public String InvokeResetPasswordViaMail() {
        LDAP ldap = new LDAP();
        MailerView mail = new MailerView("Bitte klicken sie zum ändern ihrer Emailadresse:");
        LinkCoder lc = new LinkCoder();
        LDAPResult r = ldap.getUserData("", "", userEmail);
        userLogin = r.getUser().getUserLogin();
        try {
            userLogin = lc.encode(userLogin);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(PasswordReset.class.getName()).log(Level.SEVERE, null, ex);
        }

        String link = i.getEmailLinkPWDReset() + userLogin;
        mail.setThreadName("Mailversand");
        mail.setEmail(userEmail);
        mail.setLink(link);
        mail.setUidNumber(uidNumber);
        mail.start();
        ldap.closeLDAPContext();
        return "emailreminder";
    }

    public void KnownPasswordReset() {
        LDAP ldap = new LDAP();
        LDAPResult r;

        // # semar
        // is development platform ?
        if ( Portal.isDevPlatform()) {
            userLogin = "a.admin";
        } else {
            // production platform
            Principal userid = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal();
            OidcPrincipal<OidcSecurityContext> kp = (OidcPrincipal<OidcSecurityContext>) userid;
            userLogin = kp.getOidcSecurityContext().getIDToken().getPreferredUsername();
            userLogin = userLogin.toLowerCase().trim();
        }

        r = ldap.getUserData(userLogin, "", "");

        r = ldap.ResetPassword(userLogin, password2.trim(), r.getUser().getUserClass(), true);
        if (r.isSuccess()) {
            message = "Passwort erfolgreich geändert.";
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(message));
            passwordKnown = null;
            password1 = null;
            password2 = null;
//            result = userLogin + ": Passwortänderung erfolgreich!";
//            //LOGGING
//            //log.Meldung("Passwort geändert: " + user + " ," + classname);
//            return "pwresetOK";
        } else {
            message = "Passwort nicht geändert";
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(message));
            passwordKnown = null;
            password1 = null;
            password2 = null;
//            result = userLogin + ": Passwortänderung nicht erfolgt!";
//            return "pwresetFault";
        }
        ldap.closeLDAPContext();
    }

    public void PostValidatePasswort1(ComponentSystemEvent ev) {

        this.password1Input = (UIInput) ev.getComponent();
    }

    //    public void validatePassword(FacesContext ctx, UIComponent comp, Object value) throws ValidatorException {
//        if (((String) password1Input.getValue() == null) || (value == null)) {
//            throw new ValidatorException(
//                    new FacesMessage(FacesMessage.SEVERITY_ERROR, " bitte beide Passwortfelder ausfüllen!", null));
//        } else {
//            String pwd1 = (String) password1Input.getValue();
//            String pwd2 = value.toString();
//            if (!(pwd1).equals(pwd2)) {
//                throw new ValidatorException(
//                        new FacesMessage(FacesMessage.SEVERITY_ERROR, " eingegebene Passwörter müssen gleich sein!", null));
//            } else if ((pwd1.length() < 5) || (pwd2.length() < 5)) {
//                throw new ValidatorException(
//                        new FacesMessage(FacesMessage.SEVERITY_ERROR, " eingegebene Passwörter haben weniger als 5 Zeichen!", null));
//            }
//        }
//    }
    public void validateKnownPassword(FacesContext ctx, UIComponent comp, Object value) throws ValidatorException {

        LDAP ldap = new LDAP();
        LDAPResult r;



        // # semar
        // development platform ?
        if ( Portal.isDevPlatform()) {
            userLogin = "a.admin";
        } else {
            // production platform
            Principal userid = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal();
            OidcPrincipal<OidcSecurityContext> kp = (OidcPrincipal<OidcSecurityContext>) userid;
            userLogin = kp.getOidcSecurityContext().getIDToken().getPreferredUsername();
            userLogin = userLogin.toLowerCase().trim();
        }
        
        r = ldap.getUserData(userLogin, "", "");
        ldap.closeLDAPContext();
        if (!r.getUser().getUserPW().equals(value)) {
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Passwort passt nicht zum angemeldeten Benutzer.", null));
        }
    }

    //    public void ValidateKnownPasswort(FacesContext ctx, UIComponent comp, Object value) throws ValidatorException {
//        if (value == null) {
//            throw new ValidatorException(
//                    new FacesMessage(FacesMessage.SEVERITY_ERROR, " Bitte ihr aktuelles Passwort eintragen!", null));
//        }
//
//        String password = (String) value;
//        if (password.length() < 5) {
//            throw new ValidatorException(
//                    new FacesMessage(FacesMessage.SEVERITY_ERROR, " eingegebenes Passwort hat weniger als 5 Zeichen!", null));
//        }
//        LDAPResult r = authenticateUser(userLogin, passwordKnown, classname);
//        if (!r.isSuccess()) {
//            throw new ValidatorException(
//                    new FacesMessage(FacesMessage.SEVERITY_ERROR, " aktuelle Passwort / Benutzer Kombination falsch!", null));
//        }
//    }
//    public void ValidateOldPasswort(FacesContext ctx, UIComponent comp,
//            Object value) throws ValidatorException {
//        if (value == null) {
//            throw new ValidatorException(
//                    new FacesMessage(FacesMessage.SEVERITY_ERROR, " Bitte ihr aktuelles Passwort eintragen!", null));
//        }
//
//        String password = (String) value;
//        if (password.length() < 5) {
//            throw new ValidatorException(
//                    new FacesMessage(FacesMessage.SEVERITY_ERROR, " eingegebenes Passwort hat weniger als 5 Zeichen!", null));
//        }
//
//        LDAPResult r = authenticateUser(userLogin, passwordKnown, classname);
//        if (!r.isSuccess()) {
//            throw new ValidatorException(
//                    new FacesMessage(FacesMessage.SEVERITY_ERROR, " aktuelle Passwort / Benutzer Kombination falsch!", null));
//        }
//    }
//getter und setter
    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public List getClasses() {
        LDAP ldap = new LDAP();
        LDAPResult r = ldap.GetAllClasses();
        ldap.closeLDAPContext();
        classes = r.getGroups();
        classes.sort(String.CASE_INSENSITIVE_ORDER);
        return classes;
    }

    public void setClasses(List classes) {
        this.classes = classes;
    }

    public String getClassname() {
        return classname;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

    public List getClassmembers() {
        return classmembers;
    }

    public void setClassmembers(List classmembers) {
        this.classmembers = classmembers;
    }

    public String getPassword1() {
        return password1;
    }

    public void setPassword1(String password1) {
        this.password1 = password1;
    }

    public String getPassword2() {
        return password2;
    }

    public void setPassword2(String password2) {
        this.password2 = password2;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getPasswordKnown() {
        return passwordKnown;
    }

    public void setPasswordKnown(String passwordKnown) {
        this.passwordKnown = passwordKnown;
    }

    public String getUidNumber() {
        return uidNumber;
    }

    public void setUidNumber(String uidNumber) {

        this.uidNumber = uidNumber;
    }

    public String getMessageForEmailReminder() {
        return MessageForEmailReminder;
    }

    public void setMessageForEmailReminder(String MessageForEmailReminder) {
        this.MessageForEmailReminder = MessageForEmailReminder;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getEncodedUserLogin() {
        return encodedUserLogin;
    }

    public void setEncodedUserLogin(String encodedUserLogin) {
        LinkCoder lc = new LinkCoder();
        try {
            this.userLogin = lc.decode(encodedUserLogin);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(PasswordReset.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        this.encodedUserLogin = encodedUserLogin;
    }

    public String getMessageForEmailReminderFault() {
        return MessageForEmailReminderFault;
    }

    public void setMessageForEmailReminderFault(String MessageForEmailReminderFault) {
        this.MessageForEmailReminderFault = MessageForEmailReminderFault;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UIInput getPassword1Input() {
        return password1Input;
    }

    public void setPassword1Input(UIInput password1Input) {
        this.password1Input = password1Input;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }


}
