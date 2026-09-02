/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbz.portal;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;

/**
 * @author Andreas
 */
@ManagedBean(name = "RegistrationView")
@SessionScoped
public class RegistrationView implements Serializable {

    //private final LDAP ldap = new LDAP();
    private loggingBean log;
    private ini i = new ini();
    private Helper helper;
    private String uidNumber;
    private String uid;
    private boolean hasRead = false;
    private String key;
    private String givenname;
    private String email;
    private String emailCheck;
    private String keyClassname;
    private String messageForEmailcheck;
    private String messageForEmailReminder;
    private String messageIconPath;
    private String surname;
    private String result;
    private String classname;
    private String password1;
    private String password2;
    private String profil;
    private String message;
    private String defaultPassword;
    private String gender;
    private String birthday;


    private String inClassList;

    private void ResetRegistrationViewFields() {
        this.key = "";
        this.givenname = "";
        this.surname = "";
        this.email = "";
        this.emailCheck = "";
        this.gender = "";
        this.birthday = "";
        this.hasRead = false;
    }

    ;

    public RegistrationView() {
        ResetRegistrationViewFields();
    }

    public loggingBean getLog() {
        return log;
    }

    public void setLog(loggingBean log) {
        this.log = log;
    }

    public ini getIni() {
        return i;
    }

    public void setIni(ini i) {
        this.i = i;
    }

    public Helper getHelper() {
        return helper;
    }

    public void setHelper(Helper helper) {
        this.helper = helper;
    }

    /**
     * @return the classname
     */
    public String getClassname() {
        return classname;
    }

    /**
     * @param classname the classname to set
     */
    public void setClassname(String classname) {
        this.classname = classname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @param uid the key to set
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /**
     * @return the key
     */
    public String getUid() {
        return uid;
    }

    /**
     * @param givenname the givenname to set
     */
    public void setGivenname(String givenname) {
        this.givenname = givenname;
    }

    /**
     * @param surname the surname to set
     */
    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getResult() {
        return result;
    }

    /**
     * @return the givenname
     */
    public String getGivenname() {
        return givenname;
    }

    /**
     * @return the surname
     */
    public String getSurname() {
        return surname;
    }

    public String getPassword1() {
        return password1;
    }

    /**
     * @param password1 the passwort1 to set
     */
    public void setPassword1(String password1) {
        this.password1 = password1;
    }

    /**
     * @return the passwort2
     */
    public String getPassword2() {
        return password2;
    }

    /**
     * @param password2 the passwort2 to set
     */
    public void setPassword2(String password2) {
        this.password2 = password2;
    }

    /**
     * @return the fullname
     */
    public String getFullname() {
        return givenname + " " + surname;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @param key the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * @return the keyClassname
     */
    public String getKeyClassname() {
        return keyClassname;
    }

    /**
     * @param keyClassname the keyClassname to set
     */
    public void setKeyClassname(String keyClassname) {
        this.keyClassname = keyClassname;
    }

    /**
     * @return the profil
     */
    public String getProfil() {
        return profil;
    }

    /**
     * @param profil the profil to set
     */
    public void setProfil(String profil) {
        this.profil = profil;
    }

    /**
     * @return the hasRead
     */
    //@AssertTrue
    public boolean isHasRead() {
        return hasRead;
    }

    /**
     * @param hasRead the hasRead to set
     */
    public void setHasRead(boolean hasRead) {
        this.hasRead = hasRead;
    }

    private Boolean UserIsLogIn() {
        String authType = FacesContext.getCurrentInstance().getExternalContext().getAuthType();
        if (authType == null) {
            return false;
        } else {
            return true;
        }
    }

    public void SetUserActiveMail() {
        LDAP ldap = new LDAP();
        LDAPResult r = ldap.getUserData("", uidNumber, "");
        String classOfUser = r.getUser().getUserClass();
        String uidOfUser = r.getUser().getUserLogin();
        r = ldap.ActivateUserPassword(uidOfUser, classOfUser, 4);
        r.getException();
        if (r.isSuccess()) {
            messageForEmailcheck = "Vielen Dank.<br/>Sie haben ihre Emailaddresse bestätigt.<br/> Achtung: Der Zugang muss von einer Lehrkraft<br/>freigeschaltet werden.";
            messageIconPath = "../images/icons8_checkmark_96px.png";

        } else {
            messageForEmailcheck = "Etwas ist schief gelaufen.<br/>Bitte wenden sie sich an ihre Lehrkraft.";
            messageIconPath = "../images/icons8_error_96px.png";
        }
        ;
        ldap.closeLDAPContext();
    }

    /**
     * @return
     */
    public String registration() {
        MailerView mail = new MailerView("Bitte klicken sie zur Bestätigung ihrer Emailadresse");
        LDAP ldap = new LDAP();
        LDAPResult r = new LDAPResult();
        helper = new Helper();
        givenname = givenname.toLowerCase().trim();
        surname = surname.toLowerCase().trim();
        email = email.toLowerCase().trim();
        r = ldap.GenerateUniqueUserName(givenname, surname);
        uid = r.getStringResult().toLowerCase().trim();
        givenname = helper.setKapital(givenname);
        surname = helper.setKapital(surname);
        defaultPassword = i.getDefaultPassword();
        r = ldap.isUserPresent(givenname, surname, birthday);
        inClassList = "";
        if (r.getStringResult().equalsIgnoreCase("isPresent")) {

            for (userData u : r.getUserList()) {
                inClassList = inClassList + u.userClass + " ";
            }
            ResetRegistrationViewFields();
            return "userPresent.xhtml?c=" + inClassList;
        }

        setKeyClassname(ldap.GetClassFromKey(getKey()).getStringResult());
        ldap.DeleteKey(getKey(), ldap.GetClassFromKey(getKey()).getStringResult());

        //wenn Klasse Lehrer ist wird das Profil auf Lehrer gesetzt!
        if (getKeyClassname().equals("LEHRER")) {
            setProfil("Lehrer");
        } else {
            setProfil("Public");
        }
        r = ldap.UserAdd(getSurname(), getGivenname(), getUid(), getDefaultPassword(), getEmail(), getKeyClassname(), getProfil(), gender, birthday, false);
//        //LOGGING
        if (r.isSuccess()) {
            //log.message(true,"REGISTRIERUNG : User hat sich registriert " + getUid() + " ," + getGivenname() + " ," + getSurname() + " ," + getKeyClassname(),null);
            message = "erfolgreich angelegt worden!";
            messageForEmailReminder = "Hallo " + givenname + " " + surname + "<br/>" + "Das hat bis hierher super geklappt!<br/>" + "Ihr Benutzername lautet:" + "<br/>" + getUid() + "<br/>Bitte schauen sie jetzt in ihr Emailpostfach<br/>" + getEmail() + "<br/>und bestätigen sie ihre Emailadresse.";
            uidNumber = r.getUser().getUserUidNumber();
            String link = i.getEmailLink() + uidNumber;
            mail.setThreadName("Mailversand");
            mail.setEmail(email);
            mail.setLink(link);
            mail.setUidNumber(uidNumber);
            mail.start();
        } else {
            message = "nicht angelegt!";
            ldap.closeLDAPContext();
            log.message(false, "REGISTRIERUNG : User hat sich registriert " + getUid() + " ," + getGivenname() + " ," + getSurname() + " ," + getKeyClassname(), null);
            return "fault";
        }
        ldap.closeLDAPContext();
        return "emailreminder";
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUidNumber() {
        return uidNumber;
    }

    public void setUidNumber(String uidNumber) {
        this.uidNumber = uidNumber;
    }

    public String getMessageIconPath() {
        return messageIconPath;
    }

    public void setMessageIconPath(String messageIconPath) {
        this.messageIconPath = messageIconPath;
    }

    public String getMessageForEmailReminder() {
        return messageForEmailReminder;
    }

    public void setMessageForEmailReminder(String MessageForEmailReminder) {
        this.messageForEmailReminder = MessageForEmailReminder;
    }

    public ini getI() {
        return i;
    }

    public void setI(ini i) {
        this.i = i;
    }

    public String getEmailCheck() {
        return emailCheck;
    }

    public void setEmailCheck(String emailCheck) {
        this.emailCheck = emailCheck;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getMessageForEmailcheck() {
        return messageForEmailcheck;
    }

    public void setMessageForEmailcheck(String messageForEmailcheck) {
        this.messageForEmailcheck = messageForEmailcheck;
    }

    public String getDefaultPassword() {
        return defaultPassword;
    }

    public void setDefaultPassword(String defaultPassword) {
        this.defaultPassword = defaultPassword;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getInClassList() {
        return inClassList;
    }

    public void setInClassList(String inClassList) {
        this.inClassList = inClassList;
    }
}
