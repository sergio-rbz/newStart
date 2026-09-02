/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbz.portal;

import org.primefaces.PrimeFaces;
import org.primefaces.event.CellEditEvent;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author EDV-PC-Andreas
 */
//@SessionScoped
@ViewScoped
@ManagedBean(name = "ClassAdminView")

public class ClassAdminView implements Serializable {


    //private final LDAP ldap = new LDAP();
    private final UntisExport untisExport = new UntisExport();
    private boolean showKeys;
    private boolean isPortalAdmin;
    private String classToShow;
    private String userLogin;
    private String[] classmembersUIDs;
    private String givenName;
    private String userName;
    private String activ = "activ:no";
    private String password;
    private String pwdChangeEnabled;
    private String usersClass;
    private final ini set = new ini();
    final private Helper helper = new Helper();
    private List<userData> classMembers;
    private List<userData> allUserList;
    private List<userData> filteredUsers;
    private String className;
    private int count;
    private String message;
    private String key;
    private String[] selectedClasses;
    private String classnameForNewUser;
    private String password1;
    private String password2;
    private boolean isread;
    private String profil;
    private String userEmail;
    private String messageForAdminText;
    private boolean ticketAktive;
    private String userUidNumberToDelete;
    private String userFullNameToDelete;
    private String userNameToDelete;
    private String userNameToDeleteOU;
    private String gender;
    private List<userData> selectedUsers;
    private String leavingDate;
    private String entryDate;
    private String birthday;
    MissingIPadsBean missingIPadsBean;

    public String getAttest() {

        return attest;
    }

    public void setAttest(String attest) {
        this.attest = attest;
    }

    private String attest;

    public String getAttestDate() {
        return attestDate;
    }

    public void setAttestDate(String attestDate) {
        this.attestDate = attestDate;
    }

    private String attestDate;

    public String getAttestComment() {
        return attestComment;
    }

    public void setAttestComment(String attestComment) {
        this.attestComment = attestComment;
    }

    private String attestComment;
    private loggingBean log;

    //@ManagedProperty(value = "#{ticketView}")

    /**
     * Creates a new instance of ClassAdminView
     */
    public ClassAdminView() {
        ticketAktive = true;
        missingIPadsBean = new MissingIPadsBean();
        log = new loggingBean();
//        this.RefreshAllUserList();
    }

    public void clearMultiViewState() {
        FacesContext context = FacesContext.getCurrentInstance();
        String viewId = context.getViewRoot().getViewId();
        //PrimeFaces.current().multiViewState().clearAll(viewId, true, this::showMessage);
        PrimeFaces.current().multiViewState().clearAll(viewId, true, this::showMessage);

    }

    private void showMessage(String clientId) {
        FacesContext.getCurrentInstance()
                .addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, clientId + " multiview state has been cleared out", null));
    }

    //---------------Dialoge----------------
    public void dialogMassLeavingDate() {
        Map<String, Object> options = new HashMap<>();
        options.put("modal", true);
        options.put("resizable", false);
        PrimeFaces.current().dialog().openDynamic("dlg_SetMassLeavingDate", options, null);
    }

    public void BulkEportRepeat() {
        String c = className;
        LDAP ldap = new LDAP();
        LDAPResult r = ldap.RepeatClassExport(className);
        if (r.isSuccess()) {
            message = "Export wir wiederholt für Klasse " + className;
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, message, null);
            FacesContext.getCurrentInstance().addMessage("Erfolgreich!", msg);
            className = null;
        } else {
            message = "Fehler: Export wir nicht wiederholt für Klasse " + className;
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null);
            FacesContext.getCurrentInstance().addMessage("Fehler", msg);
            className = null;
        }
        ldap.closeLDAPContext();
    }

    public void BulkEntryDate() {
        String ld = entryDate;
        ld = ld.replace(".", "/");
        String c = className;

        LDAP ldap = new LDAP();
        LDAPResult r = ldap.setMassEntryDate(className, entryDate);
        if (r.isSuccess()) {
            message = "Eintrittsdatum gesetzt für Klasse " + className;
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, message, null);
            FacesContext.getCurrentInstance().addMessage("Erfolgreich!", msg);
            entryDate = null;
            className = null;
        } else {
            message = "Eintrittsdatum nicht gesetzt für Klasse " + className;
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null);
            FacesContext.getCurrentInstance().addMessage("Fehler", msg);
            entryDate = null;
            className = null;
        }
        ldap.closeLDAPContext();
    }

    public String AddUserAttest() {
        LDAP ldap = new LDAP();
        LDAPResult r = ldap.addUserAttest(getUserLogin(), getAttest(), getAttestComment());
        ldap.closeLDAPContext();
        if (r.isSuccess()) {
            message = "Attest für " + getUserName() + " " + getGivenName() + " geändert";
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Attest-/Bemerkungsänderung", message);
            FacesContext.getCurrentInstance().addMessage("Erfolgreich!", msg);
        } else {
            message = "Attest für " + getUserLogin() + " " + getGivenName() + " geändert";
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Attest-/Bemerkungsänderung", message);
            FacesContext.getCurrentInstance().addMessage("Fehler", msg);
        }
        return "classAdmin.xhtml";
    }

    public void BulkLeavingDate() {

        String ld = leavingDate;
        if (ld.equals("") || leavingDate.isEmpty()) {
            ld = "n.a";
        }
        //ld = ld.replace(".", "/");
        //String c = className;
        LDAP ldap = new LDAP();
        LDAPResult r = ldap.setMassLeavingDate(className, ld);
        if (r.isSuccess()) {
            message = "Austrittsdatum gesetzt für Klasse " + className;
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, message, null);
            FacesContext.getCurrentInstance().addMessage("Erfolgreich!", msg);
        } else {
            message = "Austrittsdatum nicht gesetzt für Klasse " + className;
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null);
            FacesContext.getCurrentInstance().addMessage("Fehler", msg);
        }
        this.leavingDate = null;
        ldap.closeLDAPContext();
    }

    public void RefreshAllUserList() {
        LDAP ldap = new LDAP();
        LDAPResult r = ldap.GetAllUsers();
        allUserList = r.getUserList();
        ldap.closeLDAPContext();
    }

    public List getClasses() {
        LDAP ldap = new LDAP();
        LDAPResult r = ldap.GetAllClasses();
        List classes = r.getGroups();
        classes.sort(String.CASE_INSENSITIVE_ORDER);
        ldap.closeLDAPContext();
        return classes;
    }

    public void OnClassSelectClassChanged() {
        LDAP ldap = new LDAP();
        int show;
        if (showKeys) {
            show = 2;
        } else {
            show = 1;
        }
        LDAPResult r = ldap.GetClassOUMembers(classToShow, show);
        classMembers = new LinkedList();
        classMembers = r.getUserList();
        ldap.closeLDAPContext();
    }

    public void OnKeySelectChanged() {
        //showKeys = !showKeys;
        LDAP ldap = new LDAP();
        int show;
        if (showKeys) {
            show = 2;
        } else {
            show = 1;
        }

        LDAPResult r = ldap.GetClassOUMembers(classToShow, show);
        classMembers = new LinkedList();
        classMembers = r.getUserList();
        ldap.closeLDAPContext();
    }

    public void UserActivate(userData editUser) {
        LDAP ldap = new LDAP();
        this.userLogin = editUser.getUserLogin();
        this.activ = editUser.getInetActive();
        this.usersClass = editUser.getUserClass();
        ldap.ActivateUser(userLogin, usersClass, 3);
        ldap.closeLDAPContext();
    }

    public void UserDeActivate(userData editUser) {
        LDAP ldap = new LDAP();
        this.userLogin = editUser.getUserLogin();
        this.activ = editUser.getInetActive();
        this.usersClass = editUser.getUserClass();
        // die 1 deaktiviert den USER
        ldap.ActivateUser(userLogin, usersClass, 1);
        ldap.closeLDAPContext();
    }

    public String UserDelete() {
        LDAP ldap = new LDAP();
        ldap.DeleteUser(this.userNameToDelete, this.userNameToDeleteOU);
        ldap.closeLDAPContext();
        return "classAdmin.xhtml";
    }

    // # semar #######################################################################################
    public String semarSendmail() {
        System.out.println("#######################################   sending mail");
        MailerView mailerView = new MailerView("Moin, bitte nicht vergessen, den iPad zurück zu bringen !");
        mailerView.setEmail("s.marcello@rbz-schuetzenpark.de");
        mailerView.setLink("link zum iPad");
        mailerView.setThreadName("Mailversand");
        mailerView.setUidNumber("123");

        mailerView.start();

        System.out.println("#######################################   mail sent");
        return "classAdmin.xhtml";
    }


    //generiert neue leere Klasse:
    public void AddNewClass() {
        if (genClassKeys()) {
            message = "Neue Klasse: " + getClassName() + " angelegt.";
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage("Klasse erfolgreich angelegt", "Meldung: " + message));
            log.message(true, "NEU: Schlüssel für Klasse " + getClassName() + " hinzugefügt", null);
            this.count = 0;
            this.className = "";
        } else {
            message = "Das ging schief - keine neu Klasse erzeugt!";
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage("Fehler beim Klasse erstellen!", "Meldung: " + message));
            log.message(false, "FEHLER: Klasse " + getClassName() + " nicht angelegt", null);
        }
    }

    //Fügt einer Klasse neue Schlüssel hinzu:
    public void AddClassKeys() {
        if (genClassKeys()) {
            message = "Zusätzliche Schlüssel für: " + getClassName() + " angelegt.";
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage("Anlegen neuer Schlüssel erfolgreich", "Meldung: " + message));
            this.count = 0;
            this.className = "";
        } else {
            message = "Das ging schief - keine Schlüssel erzeugt!";
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage("Fehler beim Anlegen neuer Schlüssel", "Meldung: " + message));
        }
    }

    public void genClassforKeys(String _class) {
        LDAP ldap = new LDAP();
        _class = helper.Classname_Check(_class);
        ldap.addGroup(_class, "Klasse des RBZ-Schuetzenpark");
        ldap.closeLDAPContext();
    }

    public boolean genClassKeys() {
        LDAP ldap = new LDAP();
        List keylist = new LinkedList();
        // erzeuge count Keys und speichere in keylist
        for (int i = 0; i < getCount(); i++) {
            key = helper.GenerateKey();
            keylist.add(key);
        }
        // Klassen OU anlegen falls nicht vorhanden.
        genClassforKeys(getClassName());
        //            LDAP obLDAP = new LDAP(s.getLDAPip(), s.getPort(), s.getCn(), s.getPassword());
        for (Object k : keylist) {
            ldap.UserAdd("RBZ-Sp. INTERNET", "KEY", k.toString(), "!WqrDeeWtH3;5F88T!tlkjjHH", "key@rbz-sp.de", getClassName(), "KEY", "ticket", "01.01.1900", false);
        }
        ldap.closeLDAPContext();
        return true;
    }

    public void deleteClasses() {
        for (String _class : selectedClasses) {
            localDeleteClass(_class);
        }
    }

    public void localDeleteClass(String Class) {

        LDAP ldap = new LDAP();
        LDAPResult r = ldap.GetClassOUMembers(Class, 3);
        if (r.isSuccess()) {
            List<userData> lst = r.getUserList();
            for (userData k : lst) {
                //if (!k.getUserProfil().equals("KEY")) { //User nur löschen wenn nicht KEY vermeidet falsche looging Meldungen.
                ldap.DeleteUser(k.getUserLogin(), Class);
                //}
            }
            ldap.deleteClass(Class);
            ldap.closeLDAPContext();
            message = "Klassen erfolgreich gelöscht";
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage("Klasse(n) erfolgreich gelöscht", "Meldung: " + message));
        } else {
            ldap.closeLDAPContext();
            message = "Das ging schief - Fehler beim Löschen!";
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage("Fehler beim Löschen der Klasse(n)", "Fehler: " + message));
        }

    }

    public void AddUserWithoutKey() {
        LDAP ldap = new LDAP();
        LDAPResult r;// = new LDAPResult();
        givenName = givenName.toLowerCase().trim();
        userName = userName.toLowerCase().trim();
        givenName = helper.setKapital(givenName);
        userName = helper.setKapital(userName);
        String cn = classnameForNewUser;
        r = ldap.GenerateUniqueUserName(givenName, userName);
        String defaultPassword = set.getDefaultPassword();
        String userLogin = r.getStringResult().toLowerCase().trim();
        if (cn.equals("LEHRER")) {
            setProfil("Lehrer");
        } else {
            setProfil("Public");
        }
        r = ldap.UserAdd(userName, givenName, userLogin, defaultPassword, userEmail, classnameForNewUser, profil, gender, birthday, isPortalAdmin);
        if (r.isSuccess()) {
            message = userLogin + ": " + givenName + " " + userName;
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage("Benutzer erfolgreich angelegt: ", message));
            ldap.closeLDAPContext();
            untisExport.UntisDiff("", userLogin, "Neu");
            this.givenName = ("");
            this.userName = ("");
            this.className = ("");
            this.profil = ("");
            this.userLogin = ("");
            this.userEmail = ("");
            this.classnameForNewUser = ("");
            this.isPortalAdmin = false;
            this.isread = false;
            this.gender = "";
            this.birthday = "";
        } else {
            message = "Fehler beim Anlegen des Benutzers!";
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage("Fehler beim Anlegen des Benutzers!", "Fehler: " + message));
            ldap.closeLDAPContext();
        }
    }

    public void AdminPasswortReset() {
        LDAP ldap = new LDAP();
        userLogin = userLogin.toLowerCase().trim();
        LDAPResult r = ldap.ResetPassword(userLogin, getPassword2().trim(), className, true);
        ldap.closeLDAPContext();
        if (r.isSuccess()) {
            message = "Passwort geändert!";
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage("Passwort erfolgreich geändert", "Meldung: " + message));
            //Felder zurücksetzen:
            userLogin = "";
            className = "";
        } else {
            message = "Passwort nicht geändert!";
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage("Passwort nicht geändert", "Fehler: " + message));
        }
    }

//    private boolean isIPadAssigned(userData user) {
//
//    }

    public void onCellEdit(CellEditEvent event) throws IOException {
        //org.primefaces.component.api.UIColumn user = event.getColumn();
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        String column = event.getColumn().getHeaderText();
        FacesContext context = FacesContext.getCurrentInstance();
        userData newUser = context.getApplication().evaluateExpressionGet(context, "#{user}", userData.class);
        //user Tabelle neu in LDAP schreiben.
        LDAP ldap = new LDAP();
        LDAPResult r = ldap.ModifyUser(newUser, column, oldValue.toString(), newValue.toString());
        FacesMessage msg;
        if (r.isSuccess()) { //wenn alles OK ->

            if (column.equalsIgnoreCase("Austrittsdatum") && newUser.rentStatus.equalsIgnoreCase("issued") && !newUser.leavingDate.equalsIgnoreCase("")) {
                //true
                missingIPadsBean.MissingIpadsToDatabase(newUser);
                Map<String, Object> options = new HashMap<String, Object>();
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Schüler hat IPad", "!! BITTE PRÜFEN !! - Muss das iPad zurück gegeben werden?");

                PrimeFaces.current().dialog().showMessageDynamic(message);
            } else {
                //false
            }
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Benutzeränderung", r.getStringResult());
        } else {
            //Message erzeugen Fehlerfall
            //FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, r.getStringResult(), newUser.getUserFullname());
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Benutzeränderung", r.getStringResult());
        }
        FacesContext.getCurrentInstance().addMessage(null, msg);
        ldap.closeLDAPContext();
    }


    // ------------------ getter and setter: ---------------------
    public String getProfil() {
        return profil;
    }

    public void setProfil(String profil) {
        this.profil = profil;
    }

    public boolean isShowKeys() {
        return showKeys;
    }

    public void setShowKeys(boolean showkeys) {
        this.showKeys = showkeys;
    }

    public String getClassToShow() {
        return classToShow;
    }

    public void setClassToShow(String classToShow) {
        this.classToShow = classToShow;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public String[] getClassmembersUIDs() {
        return classmembersUIDs;
    }

    public void setClassmembersUIDs(String[] classmembersUIDs) {
        this.classmembersUIDs = classmembersUIDs;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getActiv() {
        return activ;
    }

    public void setActiv(String activ) {
        this.activ = activ;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPwdChangeEnabled() {
        return pwdChangeEnabled;
    }

    public void setPwdChangeEnabled(String pwdChangeEnabled) {
        this.pwdChangeEnabled = pwdChangeEnabled;
    }

    public String getUsersClass() {
        return usersClass;
    }

    public void setUsersClass(String usersClass) {
        this.usersClass = usersClass;
    }

    public List<userData> getClassMembers() {
        return classMembers;
    }

    public void setClassMembers(List<userData> members) {
        this.classMembers = members;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String[] getSelectedClasses() {
        return selectedClasses;
    }

    public void setSelectedClasses(String[] selectedClasses) {
        this.selectedClasses = selectedClasses;
    }

    public ini getSet() {
        return set;
    }

    public Helper getHelper() {
        return helper;
    }

    public String getClassnameForNewUser() {
        return classnameForNewUser;
    }

    public void setClassnameForNewUser(String classnameForNewUser) {
        this.classnameForNewUser = classnameForNewUser;
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

    public boolean getIsread() {
        return isread;
    }

    public void setIsread(boolean isread) {
        this.isread = isread;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public boolean isIsPortalAdmin() {
        return isPortalAdmin;
    }

    public void setIsPortalAdmin(boolean isPortalAdmin) {
        this.isPortalAdmin = isPortalAdmin;
    }

    public List<userData> getFilteredUsers() {
        return filteredUsers;
    }

    public void setFilteredUsers(List<userData> filteredUsers) {
        this.filteredUsers = filteredUsers;
    }

    public List getAllUserList() {

        return allUserList;
    }

    public void setAllUserList(List<userData> allUserList) {
        this.allUserList = allUserList;
    }

    public boolean isIsread() {
        return isread;
    }

    public String getMessageForAdminText() {
        return messageForAdminText;
    }

    public void setMessageForAdminText(String messageForAdminText) {
        this.messageForAdminText = messageForAdminText;
    }

    public String getUserUidNumberToDelete() {
        return userUidNumberToDelete;
    }

    public void setUserUidNumberToDelete(String userUidNumberToDelete) {
        this.userUidNumberToDelete = userUidNumberToDelete;
    }

    public String getUserFullNameToDelete() {
        return userFullNameToDelete;
    }

    public void setUserFullNameToDelete(String userFullNameToDelete) {
        this.userFullNameToDelete = userFullNameToDelete;
    }

    public String getUserNameToDelete() {
        return userNameToDelete;
    }

    public void setUserNameToDelete(String userNameToDelete) {
        this.userNameToDelete = userNameToDelete;
    }

    public String getUserNameToDeleteOU() {
        return userNameToDeleteOU;
    }

    public void setUserNameToDeleteOU(String userNameToDeleteOU) {
        this.userNameToDeleteOU = userNameToDeleteOU;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public UntisExport getUntisExport() {
        return untisExport;
    }

    public boolean isTicketAktive() {
        return ticketAktive;
    }

    public void setTicketAktive(boolean ticketAktive) {
        this.ticketAktive = ticketAktive;
    }

    public List<userData> getSelectedUsers() {
        return selectedUsers;
    }

    public void setSelectedUsers(List<userData> selectedUsers) {
        this.selectedUsers = selectedUsers;
    }

    public String getLeavingDate() {
        return leavingDate;
    }

    public void setLeavingDate(String leavingDate) {
        this.leavingDate = leavingDate;
    }


    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }


    public String getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(String entryDate) {
        this.entryDate = entryDate;
    }
}
