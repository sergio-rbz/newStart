/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbz.portal;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.validator.ValidatorException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author andreas
 */
@ManagedBean(name = "ValidateView")
@SessionScoped
public class ValidateView implements Serializable {

    //private final LDAP ldap = new LDAP();
    private UIInput password1Input;
    private UIInput email1Input;
    private UIInput oldPasswortInput;
    private String email;
    private String emailCheck;

    public ValidateView() {
    }

    public void birthdayValidate(FacesContext ctx, UIComponent comp, Object value) throws ValidatorException {
        if (value == null) {
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Bitte ein Geburtsdatum (tt.mm.jjjj) eintragen!", null));
        }
        String stringValue = (String) value;
        Date now = new Date();
        Date inputDate = new Date();
        // ^\s*(3[01]|[12][0-9]|0?[1-9])\.(1[012]|0?[1-9])\.((?:19|20)\d{2})\s*$
        // prüft Datumsformat:
        if (!stringValue.matches("^(3[01]|[12][0-9]|0?[1-9])\\.(1[012]|0?[1-9])\\.((?:19|20)\\d{2})$")) {
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, " Bitte ein gültiges Datum eintragen.", null));
        }
        // Wandelt Datums String in Date um:
        try {
            inputDate = new SimpleDateFormat("dd.MM.yyyy").parse(stringValue);
        } catch (ParseException ex) {
            Logger.getLogger(ValidateView.class.getName()).log(Level.SEVERE, null, ex);
        }
        // Liegt Datum in der Zukunft?
        if (inputDate.after(now)) {
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Sie sind noch gar nicht geboren!?", null));
        }
        // Ist Datum heute?
        if (inputDate.equals(now)) {
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Sind sie gerade geboren worden?", null));

        }


    }

    public void tokenValidate(FacesContext ctx, UIComponent comp, Object value) throws ValidatorException {

        LDAP ldap = new LDAP();
        LDAPResult r;
        if (value == null) {
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, " Bitte einen Schlüssel eintragen!", null));
        }
        r = ldap.GetClassFromKey(value.toString());
        String key = r.getStringResult();
        ldap.closeLDAPContext();
        if (key.equals("nokey")) {
//                log.Meldung("ungültiger Schlüssel");
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, " Schlüssel ist ungültig!", null));
        }
    }

    public void GivennameValidate(FacesContext ctx, UIComponent comp,
                                  Object value) throws ValidatorException {

        if (value == null) {
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, " Bitte einen Vornamen eintragen.", null));
        }
        String StringValue = (String) value;
        if (!StringValue.matches("([a-zA-Z -]*)")) {
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, " Bitte einen gültigen Vornamen eintragen.", null));
        }
    }

    public void SurnameValidate(FacesContext ctx, UIComponent comp,
                                Object value) throws ValidatorException {
        String StringValue = (String) value;
        if (value == null) {
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, " Bitte einen Nachnamen eintragen.", null));
        }
        if (!StringValue.matches("([a-zA-ZäÄöÖüÜ -]*)")) {
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, " Bitte einen gültigen Nachnamen eintragen.", null));
        }
    }

    public void UidExistsActivatedPwnChangeAllowd(FacesContext ctx, UIComponent comp,
                                                  Object value) throws ValidatorException {
        LDAP ldap = new LDAP();
        String userlogin = (String) value;
        if (!ldap.UserLoginExists(userlogin)) {
            ldap.closeLDAPContext();
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Benutzername ist nicht vorhanden.", null));
        }
        LDAPResult r = ldap.getUserData(userlogin, "", "");
        String useraktiv = r.getUser().getInetActive();
        if (useraktiv.equals("no")) {
            ldap.closeLDAPContext();
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Benutzer ist nicht aktiviert.", null));
        }
        String userpwdChangeAllow = r.getUser().getPwChangeAllowed();
        if (userpwdChangeAllow.equals("no")) {
            ldap.closeLDAPContext();
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Passwortänderung nicht erlaubt.", null));
        }
        ldap.closeLDAPContext();
    }

    public void UserLoginExistsValidate(FacesContext ctx, UIComponent comp,
                                        Object value) throws ValidatorException {
        LDAP ldap = new LDAP();
        String userlogin = (String) value;
        if (!ldap.UserLoginExists(userlogin)) {
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Benutzername ist nicht vorhanden.", null));
        }
        LDAPResult r = ldap.getUserData(userlogin, "", "");
        String useraktiv = r.getUser().getInetActive();
        if (useraktiv.equals("no")) {
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Benutzer ist nicht aktiviert.", null));
        }
    }

    public void UserLoginValidate(FacesContext ctx, UIComponent comp,
                                  Object value) throws ValidatorException {
        LDAP ldap = new LDAP();
        String StringValue = (String) value;
        if (!StringValue.matches("([a-zA-Z0-9.-_]*)")) {
            ldap.closeLDAPContext();
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Bitte einen gültigen Benutzername eintragen.", null));
        }
        if (StringValue.length() < 5) {
            ldap.closeLDAPContext();
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Benutzername muss 5 Zeichen lang sein.", null));
        }
        if (ldap.UserLoginExists(StringValue)) {
            ldap.closeLDAPContext();
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Benutzername ist schon vorhanden.", null));
        }
    }

    public void emailExist(FacesContext ctx, UIComponent comp,
                           Object value) throws ValidatorException {
        String userEmail = (String) value;
        LDAP ldap = new LDAP();
        LDAPResult r = ldap.getUserData("", "", userEmail);
        String userLogin = r.getUser().getUserLogin();
        ldap.closeLDAPContext();
        if (userLogin.isEmpty()) {
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Emailadresse nicht vorhanden!", null));
        }
    }

    public void postValidateEmail(ComponentSystemEvent ev) {
        this.email1Input = (UIInput) ev.getComponent();
        this.email = (String) email1Input.getValue();
    }

    public void EmailValidate(FacesContext ctx, UIComponent comp,
                              Object value) throws ValidatorException {
        String StringValue = (String) value;
        if (StringValue.equals("")) {
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, " Bitte Emailadresse eintragen!", null));
        }

        if (!StringValue.matches("[\\w\\.-]*[a-zA-Z0-9_]@[\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]")) {
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Emailadresse ungültig", null));
        }
        emailCheck = StringValue;
        if (!email.equalsIgnoreCase(emailCheck)) {
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Emailadressen ungleich", null));
        }

        LDAPResult r;
        LDAP ldap = new LDAP();
        r = ldap.getUserData("", "", StringValue);
        ldap.closeLDAPContext();
        String uidNumber = r.getUser().getUserUidNumber();
        if (!uidNumber.isEmpty()) {
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Emailadresse ist bereits vorhanden.", null));
        }
        setEmail(StringValue);
    }

    // PASSWORT REGEX : ^([A-Za-z0-9ÄÖÜäöüß]{6,250})$
    public void namesValidate(FacesContext ctx, UIComponent comp,
                              Object value) throws ValidatorException {

        if (value == null) {
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, " Bitte einen Namen eintragen!", null));
        }
        String StringValue = (String) value;
        if (!StringValue.matches("[a-z A-Z äüöÄÜÖ ß -]*")) {
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, " Bitte einen gültigen Namen eintragen!", null));
        }
    }

    public void yesValidate(FacesContext ctx, UIComponent comp, Object value) throws ValidatorException {
        if (value instanceof Boolean) {
            Boolean boolValue = (Boolean) value;
            if (boolValue.equals(false)) {
                throw new ValidatorException(
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Benutzerordung gelesen?", null));
            }
        }
    }

    public void postValidatePassword(ComponentSystemEvent ev) {
        this.password1Input = (UIInput) ev.getComponent();
    }

    private boolean valitPassword(String password) {

        // "^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$"
//        int minPwdLength = 8;
//        int validSum = 0;
        boolean valit = true;
        String StringValue = (String) password;
        if (!StringValue.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$")) {
            valit = false;
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, " Passwort hat falsches Format.", null));
        }

//        String[] letters = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "ö", "ü", "ä", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "Ö", "Ü", "Ä"};
//        String[] numbers = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};
//        String[] special = {"!", "§", "$", "%", "&", "/", ".", ",", "=", "*", "?", ";", ":", "-", "+", "@", "#", "<", ">", "(", ")", "[", "]", "^", "|"};
//
//        for (String l : letters) { //enthält Buchstaben??
//            if (password.indexOf(l) != -1) {
//                //valit = true;
//                validSum++;
//                break;
//            } else {
//                valit = false;
//            }
//        }
//
//        for (String n : numbers) { // enthält Nummern?
//            if (password.indexOf(n) != -1) {
////                valit = true;
//                validSum++;
//                break;
//            } else {
//                valit = false;
//
//            }
//       }
//
//        for (String s : special) { // enthält Sonderzeichen?
//            if (password.indexOf(s) != -1) {
////                valit = true;
//                validSum++;
//                break;
//            } else {
//                valit = false;
//            }
//        }
//        if (validSum == 3) {
//            valit = true;
//        } else {
//            valit = false;
//        }
        return valit;
    }

    public void passwordValidate(FacesContext ctx, UIComponent comp,
                                 Object value) throws ValidatorException {

        if (((String) password1Input.getValue() == null) || (value == null)) {
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, " bitte beide Passwortfelder ausfüllen!", null));
        } else {
            String pwd1 = (String) password1Input.getValue();
            String pwd2 = value.toString();
            if (!(pwd1).equals(pwd2)) {
                throw new ValidatorException(
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, " eingegebene Passwörter müssen gleich sein!", null));
            } else {
                //Länge == 8?
                if ((pwd1.length() < 8) || (pwd2.length() < 8)) {
                    throw new ValidatorException(
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, " eingegebene Passwörter haben weniger als 8 Zeichen!", null));
                }
                if (!pwd1.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$")) {
                    throw new ValidatorException(
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, " Passwort hat falsches Format.", null));
                }
                if (pwd1.equals(("sommer#1010"))) {
                    throw new ValidatorException(
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, " Beispiel nicht gültig als Passwort.", null));
                }
            }
        }
    }

    public UIInput getPassword1Input() {
        return password1Input;
    }

    public void setPassword1Input(UIInput password1Input) {
        this.password1Input = password1Input;
    }

    public UIInput getOldPasswortInput() {
        return oldPasswortInput;
    }

    public void setOldPasswortInput(UIInput oldPasswortInput) {
        this.oldPasswortInput = oldPasswortInput;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmailCheck() {
        return emailCheck;
    }

    public void setEmailCheck(String emailCheck) {
        this.emailCheck = emailCheck;
    }

    public UIInput getEmail1Input() {
        return email1Input;
    }

    public void setEmail1Input(UIInput email1Input) {
        this.email1Input = email1Input;
    }
}
