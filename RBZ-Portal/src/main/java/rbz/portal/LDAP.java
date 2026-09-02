/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open  the template in the editor.
 */
package rbz.portal;

//import javax.jws.WebMethod;
//import javax.jws.WebParam;

import javax.inject.Named;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.StartTlsResponse;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author EDV-PC-Andreas
 */
@Named (value="LDAP")
public class LDAP implements Serializable {

    private final ini i = new ini();
    private final Helper helper = new Helper();
    private final UntisExport untisExport = new UntisExport();
    private final loggingBean log = new loggingBean();
    private final String ROOT_OU = i.getRootOU() + "," + i.getDomain();
    private DirContext context;

    private LdapContext getInitialContextTLS(String hostname, int port, String cn, String password)
            throws NamingException {

        // sudo keytool -import -alias myssl -file /root/capub.crt -keystore $JAVA_HOME/jre/lib/security/cacerts 
        // keytool -import -alias ldapDevssl -file D:/Zertifikate/server.crt -keystore "C:\Program Files\Java\jdk1.8.0_92\jre\lib\security\cacerts"
        // keytool -import -alias fs2ssl -file /home/rbz1/server.crt -keysstore "/usr/lib/jvm/java-8-oracle/jre/lib/security/cacerts"
        // -Djavax.net.ssl.trustStore=/usr/local/tomcat/conf/cacerts 
        // openssl x509 -in <(openssl s_client -connect ads.hs-karlsruhe.de:636 -prexit 2>/dev/null) > my-ca.crt 
        // Standart Passwort keystore: changeit        
        String providerURL = new StringBuffer("ldap://").append(hostname).append(":").append(port).toString();
        Hashtable env = new Hashtable(11);
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        // Must use the name of the server that is found in its certificate
        env.put(Context.PROVIDER_URL, providerURL);
        // Create initial context
        LdapContext ctx = new InitialLdapContext(env, null);
        // Start TLS
        StartTlsResponse tls
                = (StartTlsResponse) ctx.extendedOperation(new StartTlsRequest());
        try {
            tls.negotiate();
        } catch (IOException ex) {
            Logger.getLogger(LDAP.class.getName()).log(Level.SEVERE, null, ex);
        }
        // Perform simple client authentication
        // Authenticate as S. User and password "mysecret"
        if ((cn != null) && (!cn.equals(""))) {
            ctx.addToEnvironment(Context.SECURITY_AUTHENTICATION, "simple");
            ctx.addToEnvironment(Context.SECURITY_PRINCIPAL, cn);
            ctx.addToEnvironment(Context.SECURITY_CREDENTIALS, password);
        }
        return ctx;
    }

    private DirContext getInitialContext(String hostname, int port, String cn, String password)
            throws NamingException {

        String providerURL = new StringBuffer("ldap://").append(hostname).append(":").append(port).toString();
        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
//        props.put("com.sun.jndi.ldap.connect.pool", "true");
//        props.put("com.sun.jndi.ldap.connect.pool.maxsize", 400);
//        props.put("com.sun.jndi.ldap.connect.pool.authenticatio", "simple");
//        props.put("com.sun.jndi.ldap.connect.pool.initsize", 200);
//        props.put("com.sun.jndi.ldap.connect.pool.prefsize", 10);
//        props.put("com.sun.jndi.ldap.connect.pool.timeout", 100000); // 5 Minuten
        props.put(Context.PROVIDER_URL, providerURL);

        if ((cn != null) && (!cn.equals(""))) {
            props.put(Context.SECURITY_AUTHENTICATION, "simple");
            props.put(Context.SECURITY_PRINCIPAL, cn);
            props.put(Context.SECURITY_CREDENTIALS, ((password == null) ? "" : password));
        }
        return new InitialDirContext(props);
    }

    public LDAP() {


        String hostname = i.getLDAPip();
        int port = i.getPort();
        String cn = i.getCn();
        String password = i.getPassword();
        if (i.getTls().equals("yes")) {
            try {
                context = getInitialContextTLS(hostname, port, cn, password);
            } catch (NamingException ex) {
                Logger.getLogger(LDAP.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                context = getInitialContext(hostname, port, cn, password);
            } catch (NamingException ex) {
                Logger.getLogger(LDAP.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void closeLDAPContext() {

        try {
            context.close();
        } catch (NamingException ex) {
            Logger.getLogger(LDAP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @param userName
     * @param userGivenname
     * @param userLogin
     * @param userPassword
     * @param userEmail
     * @param userClass
     * @param userProfil
     * @param isPortalAdmin
     * @param gender
     * @return
     */
    public LDAPResult UserAdd(String userName, String userGivenname, String userLogin, String userPassword, String userEmail, String userClass, String userProfil, String gender, String userBirthday, boolean isPortalAdmin) {
        // * Fügt neuen Benutzer hinzu.*/
        LDAPResult result = new LDAPResult();
        userData user = new userData();
        try {
            Attributes container = new BasicAttributes();
//          Create the objectclass to add
            Attribute objClasses = new BasicAttribute("objectClass");
            objClasses.add("top");
            objClasses.add("person");
            objClasses.add("posixAccount");
            objClasses.add("organizationalPerson");
            objClasses.add("inetOrgPerson");
            objClasses.add("portalExtensions");
//          Assign the username, first name, and last name
            String cnValue = new StringBuffer(userGivenname).append(" ").append(userName).toString();
            Attribute cn = new BasicAttribute("cn", cnValue);
            Attribute givenName = new BasicAttribute("givenName", userGivenname);
            Attribute sn = new BasicAttribute("sn", userName);
            Attribute uid = new BasicAttribute("uid", userLogin);
            Attribute uidNumber;
            Attribute gidNumber;
            if (userProfil.equals("KEY")) {
                uidNumber = new BasicAttribute("uidNumber", "555000555");
                gidNumber = new BasicAttribute("gidNumber", "555000555");
            } else {
                user.setUserUidNumber(getNextFreeUidNumber());
                uidNumber = new BasicAttribute("uidNumber", user.getUserUidNumber());
                gidNumber = new BasicAttribute("gidNumber", GetGidNumber(userClass));
            }

            Attribute inetActive = new BasicAttribute("inetActive", "no");
            Attribute acticeShare = new BasicAttribute("activeShare", "no");
            Attribute profil = new BasicAttribute("profil", userProfil);
            Attribute pwdChangeAllow = new BasicAttribute("pwdChangeAllow", "no");
            Attribute homeDirectory = new BasicAttribute("homeDirectory", "/");
            Attribute mail = new BasicAttribute("mail", userEmail);
            Attribute ou = new BasicAttribute("ou", userClass);
            Attribute gendr = new BasicAttribute("employeeType", gender); //gender
            // Add password
            Attribute userPasswd = new BasicAttribute("userpassword", DeActivatePassword(userPassword));
            Attribute entryDate = new BasicAttribute("title", "n.a"); // Austrittsdatum
            Attribute leavingDate = new BasicAttribute("telexNumber", "n.a"); // Austrittsdatum
            Attribute birthday = new BasicAttribute("employeeNumber", userBirthday); // Geburtstag

            Attribute attest = new BasicAttribute("destinationIndicator", "no"); // Attest Ja/nein
            Attribute attestDate = new BasicAttribute("departmentNumber", "n.a"); // Attest Datum
            Attribute attestComment = new BasicAttribute("description", "n.a"); // Attest Kommentar
            Attribute ipadStatus = new BasicAttribute("gecos", "n.a"); // Status der Ipad Ausleihe

// Add these to the container
            container.put(objClasses);
            container.put(cn);
            container.put(sn);
            container.put(givenName);
            container.put(uid);
            container.put(uidNumber);
            container.put(gidNumber);
            container.put(homeDirectory);
            container.put(ou);
            container.put(mail);
            container.put(inetActive);
            container.put(acticeShare);
            container.put(profil);
            container.put(pwdChangeAllow);//TODO nicht mehr benötigt?
            container.put(gendr);//Geschlecht
            container.put(entryDate);//Eintrittsdatum
            container.put(leavingDate);//Entlassdatum
            container.put(birthday); //Geburtstag

            container.put(attest); // Attest ja / nein
            container.put(attestDate); // Datum des Attesttes
            container.put(attestComment); // Kommentar zu Attest
            container.put(userPasswd);
            container.put(ipadStatus); // Status der Ipad Ausleihe
            context.createSubcontext(getUserDN(userLogin, userClass), container);
            result.setUser(user);

            String roleDn = "cn=" + userClass + ",ou=" + userClass + "," + i.getRootOU() + "," + i.getDomain();
            ModificationItem[] roleMods = new ModificationItem[]{
                    //                new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute("memberUid", getUserDN(userLogin, userClass)))
                    new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute("memberUid", userLogin))
            };
            context.modifyAttributes(roleDn, roleMods);

            if (userProfil.equals("Public")) { //fügt User zusätzlich in StudentsRole LDAP ein.
                //String roleDn = "cn=StudentsRole,ou=Groups,dc=rbz,dc=edu";
                roleDn = i.getStudentsRole() + "," + i.getRolesOU() + "," + i.getDomain();
                roleMods = new ModificationItem[]{
                        //                new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute("memberUid", getUserDN(userLogin, userClass)))
                        new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute("memberUid", userLogin))
                };
                context.modifyAttributes(roleDn, roleMods);
            }

            if (userProfil.equals("Lehrer")) { //fügt User zusätzlich in LehrerRole LDAP ein.
                //String roleDn = "cn=TeachersRole,ou=Groups,dc=rbz,dc=edu";
                roleDn = i.getTeachersRole() + "," + i.getRolesOU() + "," + i.getDomain();
                roleMods = new ModificationItem[]{
                        //                new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute("memberUid", getUserDN(userLogin, userClass)))
                        new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute("memberUid", userLogin))
                };
                context.modifyAttributes(roleDn, roleMods);
            }

            if (isPortalAdmin) { //fügt User zusätzlich in AdminsRole LDAP ein.
                //String roleDn = "cn=AdminsRole,ou=Groups,dc=rbz,dc=edu";
                roleDn = i.getAdminsRole() + "," + i.getRolesOU() + "," + i.getDomain();
                roleMods = new ModificationItem[]{
                        //                new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute("memberUid", getUserDN(userLogin, userClass)))
                        new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute("memberUid", userLogin))
                };
                context.modifyAttributes(roleDn, roleMods);
            }

            result.setSuccess(true);
            //wenn Log enable LOGGING
            if (!userProfil.equals("KEY")) { // nicht loggen wenn User ein Key
                log.message(true, "Benutzer hinzugefügt: " + userLogin + " / " + userGivenname + " " + userName + " / " + userClass, null);
            }

        } catch (NamingException ex) {
            Logger.getLogger(LDAP.class.getName()).log(Level.SEVERE, null, ex);
            result.setException(ex.toString());
            result.setSuccess(false);
            log.message(false, "Benutzer hinzugfügen fehlgeschlagen.: " + userLogin + " / " + userGivenname + " " + userName + " / " + userClass, ex.toString());
        }
        return result;
    }

    /**
     * @param userLogin
     * @param uidNumber
     * @param userEmail
     * @return
     */
    public LDAPResult getUserData(String userLogin, String uidNumber, String userEmail) {
        LDAPResult result = new LDAPResult();
        userData u = new userData();
        String fullname = "";
        String givenName = "";
        String name = "";
        String pwd = "";
        String profil = "";
        String group = "";
        String filter = "";
        String email = "";
        String uidnumber = "";
        String inetActive = "";
        String pwdChangeAllow = "";
        String gender = "";
        String leavingDate = "";
        String entryDate = "";
        String birthday = "";
        String attest = "";
        String attestDate = "";
        String attestComment = "";
        String rentStatus = "";

        if (!userLogin.equals("")) {
            filter = "(uid=" + userLogin + ")";
        }
        if (!uidNumber.equals("")) {
            filter = "(uidNumber=" + uidNumber + ")";
        }
        if (!userEmail.equals("")) {
            filter = "(mail=" + userEmail + ")";
        }
        //filter = "(uid=" + userLogin + ")";
        SearchControls cons = new SearchControls();
        cons.setSearchScope(SearchControls.SUBTREE_SCOPE);
        NamingEnumeration results;
        try {
            boolean userexists = false;
            results = context.search(ROOT_OU, filter, cons);
            while (results.hasMore()) {
                userexists = true;
                SearchResult sresult = (SearchResult) results.next();
                Attribute pw = sresult.getAttributes().get("userPassword");//.toString().substring(13);
                pwd = new String((byte[]) pw.get());
                userLogin = sresult.getAttributes().get("uid").toString().split(":")[1];
                fullname = sresult.getAttributes().get("cn").toString().split(":")[1];
                givenName = sresult.getAttributes().get("givenname").toString().split(":")[1];
                name = sresult.getAttributes().get("sn").toString().split(":")[1];
                group = sresult.getName().split("ou=")[1];
                profil = sresult.getAttributes().get("profil").toString().split(":")[1];
                email = sresult.getAttributes().get("mail").toString().split(":")[1];
                uidnumber = sresult.getAttributes().get("uidNumber").toString().split(":")[1];
                inetActive = sresult.getAttributes().get("inetActive").toString().split(":")[1];
                pwdChangeAllow = sresult.getAttributes().get("pwdChangeAllow").toString().split(":")[1];
                gender = sresult.getAttributes().get("employeeType").toString().split(":")[1];
                birthday = sresult.getAttributes().get("employeeNumber").toString().split(":")[1];
                leavingDate = sresult.getAttributes().get("telexNumber").toString().split(":")[1];
                entryDate = sresult.getAttributes().get("title").toString().split(":")[1];
                attest = sresult.getAttributes().get("destinationIndicator").toString().split(":")[1];
                attestDate = sresult.getAttributes().get("departmentNumber").toString().split(":")[1];
                attestComment = sresult.getAttributes().get("description").toString().split(":")[1];
                rentStatus = sresult.getAttributes().get("gecos").toString().split(":")[1];
            }
            if (userexists) {
                result.setSuccess(true);
                result.setException(null);
            } else {
                result.setSuccess(false);
                result.setException("There is no User with that Login Name");
            }
        } catch (NamingException ex) {
            Logger.getLogger(LDAP.class.getName()).log(Level.SEVERE, null, ex);
            result.setSuccess(false);
            result.setException(ex.toString());
        }
        u.setUserLogin(userLogin.trim());
        u.setUserFullname(fullname.trim());
        u.setGivenName(givenName.trim());
        u.setUserName(name.trim());
        u.setUserProfil(profil.trim());
        u.setUserPW(pwd);
        u.setUserClass(group.trim());
        u.setUserEmail(email.trim());
        u.setUserUidNumber(uidnumber.trim());
        u.setPwChangeAllowed(pwdChangeAllow.trim());
        u.setInetActive(inetActive.trim());
        u.setGender(gender.trim());
        u.setBirthday(birthday.trim());
        u.setLeavingDate(leavingDate.trim());
        u.setEntryDate(entryDate.trim());
        u.setAttest(attest.trim());
        u.setAttestDate(attestDate.trim());
        u.setAttestComment(attestComment.trim());
        u.setRentStatus(rentStatus.trim());
        result.setUser(u);
        return result;
    }

    public LDAPResult deleteClass(String name) {
        LDAPResult result = new LDAPResult();
        try {
            String groupDN = getGroupDN(name);
            context.destroySubcontext(groupDN);
            String ouDN = getOuDN(name);
            context.destroySubcontext(ouDN);
            result.setSuccess(true);
            log.message(true, "GELÖSCHT: Klasse " + name + " gelöscht", null);
        } catch (NamingException ex) {
            result.setSuccess(false);
            result.setException(ex.toString());
            log.message(false, "FEHLER: Klasse " + name + " nicht gelöscht", ex.toString());
        }
        return result;
    }

    public LDAPResult DeleteKey(String key, String ou) {
        LDAPResult result = new LDAPResult();
        try {
            context.destroySubcontext(getUserDN(key, ou));
            result.setSuccess(true);
        } catch (NamingException ex) {
            Logger.getLogger(LDAP.class.getName()).log(Level.SEVERE, null, ex);
            result.setSuccess(false);
            result.setException(ex.toString());
        }
        return result;
    }

    public LDAPResult DeleteUser(String userUid, String ou) {
        LDAPResult result = new LDAPResult();
        LDAPResult r = getUserData(userUid, "", "");
        //userData oldUser = r.getUser();
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        Date currentTime = new Date();
        String date = formatter.format(currentTime);
        ModificationItem[] mods = new ModificationItem[1];
        mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("telexNumber", date));
        String userDN;
        try {
            userDN = getUserDNFromUidNumber(r.getUser().getUserUidNumber());
            context.modifyAttributes(userDN, mods);
            result.setSuccess(true);
            result.setStringResult("Austrittsdatum beim Löschen eingetragen.");
            // Änderung speichern in Datenbank Portal
            untisExport.UntisDiff(r.getUser().getUserUidNumber(), "", "Gelöscht mit Austrittsdatum");
            log.message(true, "GEÄNDERT: Austrittsdatum für " + r.getUser().userLogin + " beim Löschen geändert.", null);
        } catch (NamingException ex) {
            result.setSuccess(false);
            result.setStringResult("Austrittsdatum nicht geändert.");
            result.setException(ex.toString());
            Logger.getLogger(LDAP.class.getName()).log(Level.SEVERE, null, ex);
            log.message(false, "Fehler: Austrittsdatum für " + r.getUser().userLogin + " beim Löschen geändert", ex.toString());
        }

        try {
            context.destroySubcontext(getUserDN(userUid, ou));
            if (ou.equals("Lehrer")) { //löscht user aus Role Gruppe(n).
                String roleDn = i.getTeachersRole() + "," + i.getRolesOU() + "," + i.getDomain();
                ModificationItem[] roleMods = new ModificationItem[]{
                        new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute("memberUid", userUid))
                };
                context.modifyAttributes(roleDn, roleMods);
            } else {
                if (!r.getUser().getUserProfil().equals("KEY")) { // nur wenn nicht Key. KEY hat keine Role !!
                    String roleDn = i.getStudentsRole() + "," + i.getRolesOU() + "," + i.getDomain();
                    ModificationItem[] roleMods = new ModificationItem[]{
                            new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute("memberUid", userUid))
                    };
                    context.modifyAttributes(roleDn, roleMods);
                }
            }
            result.setSuccess(true);
            if (!r.getUser().getUserProfil().equals("KEY")) { // nur loggen wenn nicht Key.
                log.message(true, "GELÖSCHT: User " + userUid + "/" + ou, null);
            }
        } catch (NamingException ex) {
            result.setSuccess(false);
            result.setException(ex.toString());
            log.message(false, "FEHLER: User " + userUid + " konnte nicht gelöscht werden", ex.toString());
        }
        return result;
    }

    public LDAPResult AuthenticateUser(String username, String password, String strClass) {
        LDAPResult result = new LDAPResult();
        String strHostname = i.getLDAPip();
        int strPort = i.getPort();
        String server;
        username = "uid=" + username + ",ou=" + strClass + "," + i.getRootOU() + "," + i.getDomain();
        server = "ldap://" + strHostname + ":" + strPort;

        // Set up environment for creating initial context
        Hashtable<String, Object> env = new Hashtable<>(11);
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, server);

        // Authenticate as S. User and give incorrect password
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, username);
        env.put(Context.SECURITY_CREDENTIALS, ((password == null) ? "" : password));
        try {
            // Create initial context
            DirContext ctx = new InitialDirContext(env);
            ctx.close();
            result.setSuccess(true);
        } catch (NamingException e) {
            result.setSuccess(false);
            result.setException(e.toString());
        }
        return result;
    }

    /**
     * @param userObject
     * @param column
     * @param oldValue
     * @param newValue
     * @return LDAPResult Modifiziert einen Benutzer;
     */
    public LDAPResult ModifyUser(userData userObject, String column, String oldValue, String newValue) {
        LDAPResult result = new LDAPResult();
        if (column.equals("Aktiv")) {
            try {
                ModificationItem[] mods = new ModificationItem[2];
                userData user = userObject;
                mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("inetActive", newValue));
                String name = getUserDN(user.getUserLogin(), user.getUserClass());
                Attributes attr = context.getAttributes(name);
                Attribute pw = attr.get("userPassword");
                //User Deaktivieren.
                if (newValue.equals("no")) {
                    String pwd = new String((byte[]) pw.get());
                    if (!pwd.startsWith("<!xx!>")) {
                        pwd = DeActivatePassword(pwd);
                        mods[1] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("userPassword", pwd));
                    } else {
                        mods[1] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("userPassword", pwd));
                    }
                    result.setStringResult("User ist jetzt deaktiviert.");
                    log.message(true, "DEAKTIVIERT: User " + user.userLogin + " deaktiviert", null);
                }

                // User hat Emailadresse bestätigt.
                if (newValue.equals("mail")) {
                    String oldPwd = new String((byte[]) pw.get());
                    mods[1] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("userPassword", oldPwd));
                    result.setStringResult("Status auf Mail bestätigt gesetzt.");
                    log.message(true, "EMAILADRESSE BESTÄTIGT: user " + user.userLogin + " Emailadresse bestätigt", null);
                }

                //User aktivieren.
                if (newValue.equals("yes")) {
                    String deactivatedPwd = new String((byte[]) pw.get());
                    if (deactivatedPwd.startsWith("<!xx!>")) {
                        String pwd = ActivatePassword(deactivatedPwd);
                        mods[1] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("userPassword", pwd));
                    } else {
                        String oldPwd = new String((byte[]) pw.get());
                        mods[1] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("userPassword", oldPwd));
                    }
                    result.setStringResult("User ist jetzt aktiviert.");
                    log.message(true, "AKTIVIERT: User " + user.userLogin + " aktiviert", null);
                }
                String userDN = getUserDNFromUidNumber(user.getUserUidNumber());
                context.modifyAttributes(userDN, mods);
                if (newValue.equals("yes")) {
                    untisExport.UntisDiff(user.getUserUidNumber(), "", "de-/aktiviert");
                }
                result.setSuccess(true);
                return result;
            } catch (NamingException ex) {
                result.setSuccess(false);
                result.setStringResult("Sorry, da ist etwas schief gegangen!");
                result.setException(ex.toString());
                Logger.getLogger(LDAP.class.getName()).log(Level.SEVERE, null, ex);
                log.message(false, "FEHLER: user " + userObject.getUserLogin() + " Fehler im Bereich De-/Aktivierung", ex.toString());
                return result;

            }
        }
//------------------------------------------------##---------------------------------------------------------        
//                                     Klasse des Users ändern.
//------------------------------------------------##---------------------------------------------------------        
        if (column.equals("Klasse")) {
            userData user = userObject;
            LDAPResult r = getUserData("", user.getUserUidNumber(), "");
            userData oldUser = r.getUser();

            try {

                //versetzen:
                if (!oldValue.equals("LEHRER") && !newValue.equals("LEHRER")) {
                    //für Klassenwechsel
                    if (!oldValue.equals(newValue)) {

                        //OU Attribut des Users ändern:
                        ModificationItem[] mods = new ModificationItem[1];
                        mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("ou", newValue));
                        String userDN = getUserDNFromUidNumber(user.getUserUidNumber());
                        context.modifyAttributes(userDN, mods);

                        String GroupDn = "cn=" + oldValue + ",ou=" + oldValue + "," + i.getRootOU() + "," + i.getDomain();
                        //alte Gruppenmitgliedschaft löschen:                    
                        ModificationItem[] GroupMods = new ModificationItem[1];
                        GroupMods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute("memberUid", oldUser.getUserLogin()));
                        context.modifyAttributes(GroupDn, GroupMods);
                        //Neue Gruppenmitgliedschaft anlegen:
                        String newGroupDn = "cn=" + newValue + ",ou=" + newValue + "," + i.getRootOU() + "," + i.getDomain();
                        ModificationItem[] newGroupMods = new ModificationItem[1];
                        newGroupMods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute("memberUid", user.getUserLogin()));
                        context.modifyAttributes(newGroupDn, newGroupMods);
                    }
                    String RoleGroupDn = i.getStudentsRole() + ",ou=Groups," + i.getDomain();
                    //oldUserLogin = oldUser.getUserLogin();
                    ModificationItem[] StudentsRoleMods = new ModificationItem[2];
                    StudentsRoleMods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute("memberUid", oldUser.getUserLogin()));
                    StudentsRoleMods[1] = new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute("memberUid", user.getUserLogin()));
                    context.modifyAttributes(RoleGroupDn, StudentsRoleMods);

                    String oldUserDN = getUserDNFromUidNumber(user.getUserUidNumber());
                    String newUserDN = "uid=" + user.getUserLogin() + ",ou=" + user.getUserClass() + "," + i.getRootOU() + "," + i.getDomain();
                    context.rename(oldUserDN, newUserDN);
                    result.setSuccess(true);
                    result.setStringResult("Schüler versetzt:");
                    // Änderung speichern in Datenbank Portal
                    untisExport.UntisDiff(user.getUserUidNumber(), "", "Klasse");
                    log.message(true, "VERSETZT: Schüler " + user.userLogin + " versetzt: " + oldValue + " --> " + newValue, null);
                    return result;
                } else {
                    result.setSuccess(false);
                    result.setStringResult("Lehrer können nicht versetzt werden.");
                    return result;

                }
            } catch (NamingException ex) {
                result.setSuccess(false);
                result.setStringResult("Fehler beim Ändern des Benutzers");
                result.setException(ex.toString());
                Logger.getLogger(LDAP.class.getName()).log(Level.SEVERE, null, ex);
                log.message(true, "FEHLER: bei Schüler " + user.userLogin + oldValue + " --> " + newValue, null);
                return result;
            }
        }
//------------------------------------------------##---------------------------------------------------------        
//                                     mail Attribut des Users ändern:
//------------------------------------------------##---------------------------------------------------------        

        if (column.equals("Email")) {
            userData user = userObject;
            LDAPResult r = getUserData("", "", newValue);
            newValue = newValue.toLowerCase().trim();

            if (newValue.equals("")) {
                result.setSuccess(false);
                result.setStringResult("Bitte Emailadresse eingeben");
                return result;
            }

            if (!newValue.matches("[\\w\\.-]*[a-zA-Z0-9_]@[\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]")) {
                result.setSuccess(false);
                result.setStringResult("Emailadresse ungültig");
                return result;
            }

            //if (!newValue.equalsIgnoreCase()) {
            //}
            r = getUserData("", "", newValue);
            String uidNumber = r.getUser().getUserUidNumber();
            if (!uidNumber.isEmpty()) {
                result.setSuccess(false);
                result.setStringResult("Emailadresse bereits vorhanden");
                return result;
            }

            if (!oldValue.equals(newValue)) {
                ModificationItem[] mods = new ModificationItem[1];
                mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("mail", newValue));
                String userDN;
                try {
                    userDN = getUserDNFromUidNumber(user.getUserUidNumber());
                    context.modifyAttributes(userDN, mods);
                    result.setSuccess(true);
                    result.setStringResult("Emailadresse geändert");
                    // Änderung speichern in Datenbank Portal
                    untisExport.UntisDiff(user.getUserUidNumber(), "", "Emailadresse");
                    log.message(true, "GEÄNDERT: Emailadresse für " + user.userLogin + " geändert", null);
                    return result;
                } catch (NamingException ex) {
                    result.setSuccess(false);
                    result.setStringResult("Fehler beim Ändern des Benutzers");
                    result.setException(ex.toString());
                    Logger.getLogger(LDAP.class.getName()).log(Level.SEVERE, null, ex);
                    log.message(false, "FEHLER: Emailadresse für " + user.userLogin + "nicht geändert", null);
                    return result;
                }
            }
        }
//------------------------------------------------##---------------------------------------------------------        
//                                     Vorname Attribut des Users ändern:
//------------------------------------------------##---------------------------------------------------------     

        if (column.equals("Vorname")) {
            userData user = userObject;
            LDAPResult r = getUserData("", "", newValue);

            if (newValue.equals("")) {
                result.setSuccess(false);
                result.setStringResult("Bitte Vornamen eingeben.");
                return result;
            }

            if (!newValue.matches("([a-zA-ZäöüÄÖÜß -]*)")) {
                result.setSuccess(false);
                result.setStringResult("Vorname ungültig.");
                return result;
            }

            if (!oldValue.equals(newValue)) {
                ModificationItem[] mods = new ModificationItem[2];
                mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("givenName", newValue));
                String newFullName = newValue + " " + user.getUserName();
                mods[1] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("cn", newFullName));
                String userDN;
                try {
                    userDN = getUserDNFromUidNumber(user.getUserUidNumber());
                    context.modifyAttributes(userDN, mods);
                    result.setSuccess(true);
                    result.setStringResult("Vorname geändert.");
                    // Änderung speichern in Datenbank Portal
                    untisExport.UntisDiff(user.getUserUidNumber(), "", "Vorname");
                    log.message(true, "GEÄNDERT: Vorname für " + user.userLogin + " geändert", null);
                    return result;
                } catch (NamingException ex) {
                    result.setSuccess(false);
                    result.setStringResult("Fehler beim Ändern des Benutzers");
                    result.setException(ex.toString());
                    Logger.getLogger(LDAP.class.getName()).log(Level.SEVERE, null, ex);
                    log.message(false, "FEHLER: Vorname für " + user.userLogin + "nicht geändert", ex.toString());
                    return result;
                }
            }
        }

//------------------------------------------------##---------------------------------------------------------        
//                                     Nachname Attribut des Users ändern:
//------------------------------------------------##---------------------------------------------------------     
        if (column.equals("Nachname")) {
            userData user = userObject;

            if (newValue.equals("")) {
                result.setSuccess(false);
                result.setStringResult("Bitte Nachnamen eingeben.");
                return result;
            }

            if (!newValue.matches("([a-zA-ZäöüÄÖÜß -]*)")) {
                result.setSuccess(false);
                result.setStringResult("Nachname ungültig.");
                return result;
            }

            if (!oldValue.equals(newValue)) {
                ModificationItem[] mods = new ModificationItem[2];
                mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("sn", newValue));
                String newFullName = user.getGivenName() + " " + newValue;
                mods[1] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("cn", newFullName));
                String userDN;
                try {
                    userDN = getUserDNFromUidNumber(user.getUserUidNumber());
                    context.modifyAttributes(userDN, mods);
                    result.setSuccess(true);
                    result.setStringResult("Nachname geändert.");
                    // Änderung speichern in Datenbank Portal
                    untisExport.UntisDiff(user.getUserUidNumber(), "", "Nachname");
                    log.message(true, "GEÄNDERT: Nachname für " + user.userLogin + " geändert", null);
                    return result;
                } catch (NamingException ex) {
                    result.setSuccess(false);
                    result.setStringResult("Fehler beim Ändern des Benutzers");
                    result.setException(ex.toString());
                    Logger.getLogger(LDAP.class.getName()).log(Level.SEVERE, null, ex);
                    log.message(false, "FEHLER: Nachname für " + user.userLogin + "nicht geändert", ex.toString());
                    return result;
                }
            }
        }

//------------------------------------------------##---------------------------------------------------------        
//  TODO noch benötigt??                                   Benutzername des Users ändern:
//------------------------------------------------##---------------------------------------------------------     
        if (column.equals("Benutzername")) {
            userData user = userObject;
            if (!oldValue.equals(newValue)) {
                if (newValue.equals("")) {
                    result.setSuccess(false);
                    result.setStringResult("Bitte Benutzername eingeben.");
                    return result;
                }
                if (!newValue.matches("([a-zA-Z0-9.-_]*)")) {
                    result.setSuccess(false);
                    result.setStringResult("Benutzername ungültig.");
                    return result;
                }
                if (UserLoginExists(newValue)) {
                    result.setSuccess(false);
                    result.setStringResult("Benutzername bereits vorhanden.");
                    return result;
                }
                String newUserDN = "uid=" + newValue + ",ou=" + user.getUserClass() + "," + i.getRootOU() + "," + i.getDomain();
                String oldUserDN = "uid=" + oldValue + ",ou=" + user.getUserClass() + "," + i.getRootOU() + "," + i.getDomain();
                try {
                    context.rename(oldUserDN, newUserDN);
                    if (user.getUserClass().equals("LEHRER")) {
                        String GroupDn = "cn=" + user.getUserClass() + ",ou=" + user.getUserClass() + "," + i.getRootOU() + "," + i.getDomain();
                        //String oldUserLogin = oldUser.getUserLogin();
                        ModificationItem[] GroupMods = new ModificationItem[2];
                        GroupMods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute("memberUid", oldValue));
                        GroupMods[1] = new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute("memberUid", newValue));
                        context.modifyAttributes(GroupDn, GroupMods);

                        GroupDn = i.getTeachersRole() + ",ou=Groups," + i.getDomain();
                        //oldUserLogin = oldUser.getUserLogin();
                        ModificationItem[] StudentsRoleMods = new ModificationItem[2];
                        StudentsRoleMods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute("memberUid", oldValue));
                        StudentsRoleMods[1] = new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute("memberUid", newValue));
                        context.modifyAttributes(GroupDn, StudentsRoleMods);
                        //wenn schueler -> dann entferne alten UID aus Klassen und Studentsrole -> dann füge neuen UID zu Studentsrole hinzu.
                    } else { //also Schüler
                        if (user.getUserClass().equals(user.getUserClass())) {
                            //Klassenzugehörigkeit aktualisieren 
                            String GroupDn = "cn=" + user.getUserClass() + ",ou=" + user.getUserClass() + "," + i.getRootOU() + "," + i.getDomain();
                            ModificationItem[] GroupMods = new ModificationItem[2];
                            GroupMods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute("memberUid", oldValue));
                            GroupMods[1] = new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute("memberUid", newValue));
                            context.modifyAttributes(GroupDn, GroupMods);
                            //Studentsrole aktualisieren
                            GroupDn = i.getStudentsRole() + ",ou=Groups," + i.getDomain();
                            ModificationItem[] StudentsRoleMods = new ModificationItem[2];
                            StudentsRoleMods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute("memberUid", oldValue));
                            StudentsRoleMods[1] = new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute("memberUid", newValue));
                            context.modifyAttributes(GroupDn, StudentsRoleMods);

                        } else {
                            String GroupDn = "cn=" + user.getUserClass() + ",ou=" + user.getUserClass() + "," + i.getRootOU() + "," + i.getDomain();
                            //alte Gruppenmitgliedschaft löschen:                    
                            ModificationItem[] GroupMods = new ModificationItem[1];
                            GroupMods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute("memberUid", oldValue));
                            context.modifyAttributes(GroupDn, GroupMods);
                            //Neue Gruppenmitgliedschaft anlegen:
                            String newGroupDn = "cn=" + user.getUserClass() + ",ou=" + user.getUserClass() + "," + i.getRootOU() + "," + i.getDomain();
                            ModificationItem[] newGroupMods = new ModificationItem[1];
                            newGroupMods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute("memberUid", newValue));
                            context.modifyAttributes(newGroupDn, newGroupMods);
                        }
                    }
                    result.setSuccess(true);
                    result.setStringResult("Benutzername geändert.");
                    // Änderung speichern in Datenbank Portal
                    untisExport.UntisDiff(user.getUserUidNumber(), "", "Login Name");
                    log.message(true, "Benutzername (UID) für " + user.userLogin + " geändert", null);
                    return result;
                } catch (NamingException ex) {
                    result.setSuccess(false);
                    result.setStringResult("Fehler beim Ändern des Benutzers");
                    result.setException(ex.toString());
                    Logger.getLogger(LDAP.class.getName()).log(Level.SEVERE, null, ex);
                    log.message(false, "Benutzername (UID) für " + user.userLogin + "nicht geändert", null);
                    return result;
                }
            }
        }
//------------------------------------------------##---------------------------------------------------------
//                                    Austrittsdatum des Users ändern:
//------------------------------------------------##---------------------------------------------------------

        if (column.equals("Austrittsdatum")) {
            userData user = userObject;
            if (newValue.equals("")) {
                newValue = "n.a";
            }
            LDAPResult r = getUserData("", "", newValue);


            if (!oldValue.equals(newValue)) {
                ModificationItem[] mods = new ModificationItem[1];
                mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("telexNumber", newValue));
                String userDN;
                try {
                    userDN = getUserDNFromUidNumber(user.getUserUidNumber());
                    context.modifyAttributes(userDN, mods);
                    result.setSuccess(true);
                    if (newValue.equals("n.a")) {
                        result.setStringResult("Austrittsdatum gelöscht.");
                    } else {
                        result.setStringResult("Austrittsdatum geändert.");
                    }
                    // Änderung speichern in Datenbank Portal
                    untisExport.UntisDiff(user.getUserUidNumber(), "", "Austrittsdatum");
                    if (newValue.equals("n.a")) {
                        log.message(true, "GELÖSCHT: Austrittsdatum für " + user.userLogin + " gelöscht", null);
                    } else {
                        log.message(true, "GEÄNDERT: Austrittsdatum für " + user.userLogin + " geändert", null);
                    }
                    result.setUser(user);
                    return result;
                } catch (NamingException ex) {
                    result.setSuccess(false);
                    result.setStringResult("Austrittsdatum nicht geändert.");
                    result.setException(ex.toString());
                    Logger.getLogger(LDAP.class.getName()).log(Level.SEVERE, null, ex);
                    log.message(false, "Fehler: Austrittsdatum für " + user.userLogin + " nicht geändert", ex.toString());
                    return result;
                }
            }
        }
//------------------------------------------------##---------------------------------------------------------
//                                    Eintrittsdatum des Users ändern:
//------------------------------------------------##---------------------------------------------------------
        if (column.equals("Eintrittsdatum")) {
            userData user = userObject;
            LDAPResult r = getUserData("", "", newValue);

            if (newValue.equals("")) {
                result.setSuccess(false);
                result.setStringResult("Bitte Eintrittsdatum eingeben.");
                return result;
            }
            if (!oldValue.equals(newValue)) {
                ModificationItem[] mods = new ModificationItem[1];
                mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("title", newValue));
                String userDN;
                try {
                    userDN = getUserDNFromUidNumber(user.getUserUidNumber());
                    context.modifyAttributes(userDN, mods);
                    result.setSuccess(true);
                    result.setStringResult("Eintrittsdatum geändert.");
                    // Änderung speichern in Datenbank Portal
                    untisExport.UntisDiff(user.getUserUidNumber(), "", "Eintrittsdatum");
                    log.message(true, "GEÄNDERT: Eintrittsdatum für " + user.userLogin + " geändert", null);
                    return result;
                } catch (NamingException ex) {
                    result.setSuccess(false);
                    result.setStringResult("Eintrittsdatum nicht geändert.");
                    result.setException(ex.toString());
                    Logger.getLogger(LDAP.class.getName()).log(Level.SEVERE, null, ex);
                    log.message(false, "FEHLER: Eintrittsdatum für " + user.userLogin + "nicht geändert", null);
                    return result;
                }
            }
        }
//------------------------------------------------##---------------------------------------------------------
//                                    Geschlecht des Users ändern:
//------------------------------------------------##---------------------------------------------------------
        if (column.equals("Geschlecht")) {
            userData user = userObject;
            LDAPResult r = getUserData("", "", newValue);

            if (newValue.equals("")) {
                result.setSuccess(false);
                result.setStringResult("Bitte Geschlecht auswählen.");
                return result;
            }
            if (!oldValue.equals(newValue)) {
                ModificationItem[] mods = new ModificationItem[1];
                mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("employeeType", newValue));
                String userDN;
                try {
                    userDN = getUserDNFromUidNumber(user.getUserUidNumber());
                    context.modifyAttributes(userDN, mods);
                    result.setSuccess(true);
                    result.setStringResult("Geschlecht geändert.");
                    // Änderung speichern in Datenbank Portal
                    untisExport.UntisDiff(user.getUserUidNumber(), "", "Geschlecht");
                    log.message(true, "GEÄNDERT: Geschlecht für " + user.userLogin + " geändert", null);
                    return result;
                } catch (NamingException ex) {
                    result.setSuccess(false);
                    result.setStringResult("Geschlecht nicht geändert.");
                    result.setException(ex.toString());
                    Logger.getLogger(LDAP.class.getName()).log(Level.SEVERE, null, ex);
                    log.message(false, "FEHLER: Geburtstag für " + user.userLogin + " nicht geändert", ex.toString());
                    return result;
                }
            }
        }
//------------------------------------------------##---------------------------------------------------------
//                                    Geburtstag des Users ändern:
//------------------------------------------------##---------------------------------------------------------
        if (column.equals("Geburtstag")) {
            userData user = userObject;
            LDAPResult r = getUserData("", "", newValue);

            if (newValue.equals("")) {
                result.setSuccess(false);
                result.setStringResult("Bitte Geburtstag ändern.");
                return result;
            }
            if (!oldValue.equals(newValue)) {
                ModificationItem[] mods = new ModificationItem[1];
                mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("employeeNumber", newValue));
                String userDN;
                try {
                    userDN = getUserDNFromUidNumber(user.getUserUidNumber());
                    context.modifyAttributes(userDN, mods);
                    result.setSuccess(true);
                    result.setStringResult("Geburtstag geändert");
                    // Änderung speichern in Datenbank Portal
                    untisExport.UntisDiff(user.getUserUidNumber(), "", "Geburtstag");
                    log.message(true, "GEÄNDERT: Geburtstag für " + user.userLogin + " geändert", null);
                    return result;
                } catch (NamingException ex) {
                    result.setSuccess(false);
                    result.setStringResult("Geburtstag nicht geändert.");
                    result.setException(ex.toString());
                    Logger.getLogger(LDAP.class.getName()).log(Level.SEVERE, null, ex);
                    log.message(false, "FEHLER: Geburtstag für " + user.userLogin + " nicht geändert", ex.toString());
                    return result;
                }
            }
        }
        return result;
    }

    public boolean setRentStatus(String uidNumber, String Status) {
        LDAPResult r = new LDAPResult();
        ModificationItem[] mods = new ModificationItem[1];
        mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("gecos", Status));
        String userDN = null;
        try {
            userDN = getUserDNFromUidNumber(uidNumber);
            context.modifyAttributes(userDN, mods);
//            untisExport.UntisDiff(uidNumber, "", "Austrittsdatum");
            log.message(true, "IPad Ausleistatus für " + uidNumber + " geändert", null);
            return true;
        } catch (NamingException ex) {
            log.message(false, "IPad Ausleistatus für " + uidNumber + "nicht geändert", ex.toString());
            return false;
        }
    }


    public LDAPResult setLeavingDate(String uidNumber, String leavingDate) {
        LDAPResult r = new LDAPResult();
        ModificationItem[] mods = new ModificationItem[1];
        mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("telexNumber", leavingDate));
        String userDN = null;
        try {
            userDN = getUserDNFromUidNumber(uidNumber);
            context.modifyAttributes(userDN, mods);
            untisExport.UntisDiff(uidNumber, "", "Austrittsdatum");
            log.message(true, "Austrittsdatum für " + uidNumber + " geändert (Bulk)", null);
        } catch (NamingException ex) {
            log.message(false, "Austrittsdatum für " + uidNumber + "nicht geändert (Bulk)", ex.toString());
        }
        //Ändere das Austrittsdatum:
        return r;
    }

    public LDAPResult isUserPresent(String givenName, String sn, String birthday) {
        LDAPResult ldapResult;
        LDAPResult lr = new LDAPResult();
        List<userData> lud = new ArrayList<>();
        ldapResult = GetAllUsers();
        lr.setStringResult("notPresent");
        for (userData ud : ldapResult.getUserList()) {
            if ((ud.givenName.equalsIgnoreCase(givenName)) && (ud.userName.equalsIgnoreCase(sn)) && (ud.birthday.equalsIgnoreCase(birthday))) {
                lud.add(ud);
                lr.setStringResult("isPresent");
            }
        }
        lr.setUserList(lud);
        return lr;
    }

    public LDAPResult addUserAttest(String uidNumber, String attest, String attestComment) {
        //String attest_str = "no";
        LDAPResult r = new LDAPResult();
        String attestDate;
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        Date currentTime = new Date();
        String date = formatter.format(currentTime);
        attestDate = date;
        ModificationItem[] mods = new ModificationItem[1];

        if (attest.equals("yes")) {
            if (attestComment.equals(attestComment.isEmpty()) || (attestComment.length() < 2)) {
                mods = new ModificationItem[3];
                mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("destinationIndicator", "yes"));
                mods[1] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("departmentNumber", attestDate));
                mods[2] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("description", "n.a"));
            } else {
                mods = new ModificationItem[3];
                mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("destinationIndicator", "yes"));
                mods[1] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("departmentNumber", attestDate));
                mods[2] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("description", attestComment));
            }
        }

        if (attest.equals("no")) {
            if (attestComment.equals(attestComment.isEmpty()) || (attestComment.length() < 2)) {
                mods = new ModificationItem[3];
                mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("destinationIndicator", "no"));
                mods[1] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("departmentNumber", "n.a"));
                mods[2] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("description", "n.a"));
            } else {
                mods = new ModificationItem[3];
                mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("destinationIndicator", "no"));
                mods[1] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("departmentNumber", "n.a"));
                mods[2] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("description", attestComment));
            }
        }

        String userDN = null;
        try {
            userDN = getUserDNFromUidNumber(uidNumber);
            context.modifyAttributes(userDN, mods);
            untisExport.UntisDiff(uidNumber, "", "Attest");
            log.message(true, "GEÄNDERT: Attest für " + uidNumber + " geändert", null);
            r.setSuccess(true);
        } catch (NamingException ex) {
            r.setSuccess(false);
            log.message(true, "FEHLER: Attest für " + uidNumber + " nicht geändert", ex.toString());
        }
        return r;
    }


    public void setEntryDate(String uidNumber, String entryDate) {
        LDAPResult r = new LDAPResult();
        ModificationItem[] mods = new ModificationItem[1];
        mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("title", entryDate));
        String userDN = null;
        try {
            userDN = getUserDNFromUidNumber(uidNumber);
            context.modifyAttributes(userDN, mods);
            untisExport.UntisDiff(uidNumber, "", "Eintrittsdatum");
            log.message(true, "GEÄNDERT (BULK): Eintrittsdatum für " + uidNumber + " geändert", null);
        } catch (NamingException ex) {
            log.message(true, "FEHLER (BULK): Eintrittsdatum für " + uidNumber + " nicht geändert (Bulk)", ex.toString());
        }
    }

    public LDAPResult setMassLeavingDate(String className, String leavingDate) {
        LDAPResult r = new LDAPResult();
        List<userData> members = new LinkedList<>();
        //Ermittel die Klassenmitglieder:
        r = GetClassOUMembers(className, 1);
        if (r.isSuccess()) {
            log.message(true, "Anstehende Änderungen (BULK): Austrittsdaten für " + className + " werden geändert...", null);
            members = r.getUserList();
            //Itariert über die ermittelten User und setzt das Leavingdate Attribute:
            for (userData ud : members) {
                setLeavingDate(ud.userUidNumber, leavingDate);
            }
        }
        return r;
    }


    public void RepeatUserExport(String uidNumber) {
        LDAPResult r = new LDAPResult();
        String userDN = null;
        try {
            userDN = getUserDNFromUidNumber(uidNumber);
            untisExport.UntisDiff(uidNumber, "", "Eintrittsdatum");
            log.message(true, "GEÄNDERT: Export für " + uidNumber + " erneut vorgesehen", null);
        } catch (NamingException ex) {
            log.message(true, "FEHLER: Export für " + uidNumber + " nicht erneuert", ex.toString());
        }
    }

    public LDAPResult RepeatClassExport(String className) {
        LDAPResult r = new LDAPResult();
        List<userData> members = new LinkedList<>();
        //Ermittel die Klassenmitglieder:
        r = GetClassOUMembers(className, 1);
        if (r.isSuccess()) {
            log.message(true, "Klasse erneut für Export vorgesehen " + className, null);
            members = r.getUserList();
            //Itariert über die ermittelten User und setzt das entryDate Attribute:
            for (userData ud : members) {
                RepeatUserExport(ud.userUidNumber);
            }
        }
        return r;
    }



    public LDAPResult setMassEntryDate(String className, String entryDate) {
        LDAPResult r = new LDAPResult();
        List<userData> members = new LinkedList<>();
        //Ermittel die Klassenmitglieder:
        r = GetClassOUMembers(className, 1);
        if (r.isSuccess()) {
            log.message(true, "Anstehende Änderungen (BULK): Eintrittsdaten für " + className + " werden geändert... (Bulk)", null);
            members = r.getUserList();
            //Itariert über die ermittelten User und setzt das entryDate Attribute:
            for (userData ud : members) {
                setEntryDate(ud.userUidNumber, entryDate);
            }
        }
        return r;
    }

    private String DeActivatePassword(String activatedPassword) {
        return "<!xx!>" + activatedPassword;
    }

    private String ActivatePassword(String deactivatedPassword) {
        String[] pwd = deactivatedPassword.split(">");
        return pwd[1];
    }

// * Aktiviert oder deaktiviert einen Benutzer Attribute inetActive und userPassword
    //   wird verändert. Wird benutzt von der Emailaktivierung */

    /**
     * @param loginName
     * @param ou
     * @param status    1 = activ:no 2 = activ:mail 3 = activ:yes
     * @return
     */
    public LDAPResult ActivateUser(String loginName, String ou, int status) {
        LDAPResult result = new LDAPResult();
        ModificationItem[] mods = new ModificationItem[1];
        String s_aktiv = "";

        if (status == 1) {
            s_aktiv = "no";
        }

        if (status == 2) {
            s_aktiv = "mail";
        }

        if (status == 3) {
            s_aktiv = "yes";
        }

        mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("inetActive", s_aktiv));
        String name = getUserDN(loginName, ou);
        try {
            context.modifyAttributes(name, mods);
            result.setSuccess(true);
            log.message(true, "User " + loginName + " aktiviert", null);
        } catch (NamingException ex) {
            result.setSuccess(false);
            result.setException(ex.toString());
            Logger.getLogger(LDAP.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        //Passwort de / aktivieren durch löschen / hinzufügen der Zeichenkette: <!xx!> im Passwort
        String deactivatedPwd = "";
        String activatedPwd = "";
        //Passwort holen aus LDAP
        try {
            Attributes attr = context.getAttributes(name);
            Attribute pw = attr.get("userPassword");
            //Passwort deaktivieren bei no
            if (status == 1) {
                activatedPwd = new String((byte[]) pw.get());
                deactivatedPwd = DeActivatePassword(activatedPwd);
                mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("userPassword", deactivatedPwd));
                log.message(true, "DEAKTIVIERT: User " + loginName + " deaktiviert", null);
            }
            //Passwort aktivieren bei mail
            if (status == 2) {
                String Pwd = new String((byte[]) pw.get());
                if (!Pwd.startsWith("<!xx!>")) {
                    activatedPwd = DeActivatePassword(Pwd);
                } else {
                    activatedPwd = Pwd;
                }
                mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("userPassword", activatedPwd));
                log.message(true, "AKTIVIERT: User " + loginName + " aktiviert", null);
            }

            //Passwort aktivieren bei yes
            if (status == 3) {
                deactivatedPwd = new String((byte[]) pw.get());
                activatedPwd = ActivatePassword(deactivatedPwd);
                mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("userPassword", activatedPwd));
                log.message(true, "AKTIVIERT: User " + loginName + " aktiviert", null);
            }

        } catch (NamingException ex) {
            Logger.getLogger(LDAP.class
                    .getName()).log(Level.SEVERE, null, ex);
            result.setSuccess(false);
            result.setException(ex.toString());
            log.message(false, "FEHLER User " + loginName + "  durch Fehler nicht de- / aktiviert", null);
            return result;
        }
        name = getUserDN(loginName, ou);
        try {
            context.modifyAttributes(name, mods);
            result.setSuccess(true);
        } catch (NamingException ex) {
            result.setSuccess(false);
            result.setException(ex.toString());
            Logger.getLogger(LDAP.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    /* Aktiviert oder deaktiviert einen Benutzer Attribute userPassword
       wird verändert. Wird benutzt von der DataGrid (userList) aktivierung
     * @param loginName
     * @param ou
     * @param status 1 = activ:no 2 = activ:mail 3 = activ:yes
     * @return
     */
    public LDAPResult ActivateUserPassword(String loginName, String ou, int status) {
        LDAPResult result = new LDAPResult();
        ModificationItem[] mods = new ModificationItem[1];
        //Passwort de / aktivieren durch löschen / hinzufügen der Zeichenkette: <!xx!> im Passwort
        String name = getUserDN(loginName, ou);
        String deactivatedPwd = "";
        String activatedPwd = "";
        //Passwort holen aus LDAP
        boolean edit = false;
        try {

            Attributes attr = context.getAttributes(name);
            Attribute pw = attr.get("userPassword");

            //Passwort deaktivieren
            if (status == 1) {
                String Pwd = new String((byte[]) pw.get());
                if (!Pwd.startsWith("<!xx!>")) {
                    Pwd = DeActivatePassword(Pwd);
                    edit = true;
                    mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("userPassword", Pwd));
                }
            }

//            User hat Emailadresse bestätigt:
//            if (status == 2) {
//                String Pwd = new String((byte[]) pw.get());
//                if (!Pwd.startsWith("<!xx!>")) {
//                    Pwd = DeActivatePassword(Pwd);
//                    mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("userPassword", Pwd));
//                    edit = true;
//                }
//            }
            //Passwort aktivieren
            if (status == 3) {
                deactivatedPwd = new String((byte[]) pw.get());
                if (deactivatedPwd.startsWith("<!xx!>")) {
                    String Pwd = ActivatePassword(deactivatedPwd);
                    mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("userPassword", Pwd));
                    edit = true;
                }
            }

            if (status == 4) {
                String Pwd = new String((byte[]) pw.get());
                mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("inetActive", "mail"));
                edit = true;
            }

        } catch (NamingException ex) {
            Logger.getLogger(LDAP.class
                    .getName()).log(Level.SEVERE, null, ex);
            result.setSuccess(false);
            result.setException(ex.toString());
            return result;
        }
        name = getUserDN(loginName, ou);

        try {
            if (edit) {
                context.modifyAttributes(name, mods);
            }
            result.setSuccess(true);
        } catch (NamingException ex) {
            result.setSuccess(false);
            result.setException(ex.toString());
            Logger.getLogger(LDAP.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    // TODO Noch benötigt??
    public LDAPResult PasswordChangeAllow(String loginName, String ou, boolean activ) {
        LDAPResult result = new LDAPResult();
        ModificationItem[] mods = new ModificationItem[1];
        String pwd_change = "no";
        if (activ == true) {
            pwd_change = "yes";
        }
        mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("pwdChangeAllow", pwd_change));
        String name = getUserDN(loginName, ou);
        try {
            context.modifyAttributes(name, mods);
            result.setSuccess(true);
            result.setStringResult("Benutzer geändert.");
        } catch (NamingException ex) {
            result.setSuccess(false);
            result.setException(ex.toString());
            Logger.getLogger(LDAP.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    /**
     * @param name
     * @param description
     * @return
     */
    public LDAPResult addGroup(String name, String description) {
        LDAPResult result = new LDAPResult();
        //LDAP: OU
        // Create a container set of attributes
        Attributes container = new BasicAttributes();

        // Create the objectclass to add
        Attribute objClasses = new BasicAttribute("objectClass");
        objClasses.add("top");
        objClasses.add("organizationalUnit");
        // objClasses.add("groupOfForethoughtNames");

        // Assign the name and description to the group
        Attribute ou = new BasicAttribute("ou", name);
        Attribute desc = new BasicAttribute("description", description);

        // Add these to the container
        container.put(objClasses);
        container.put(ou);
        container.put(desc);
        try {
            // Create the entry
            context.createSubcontext(getOuDN(name), container);
            result.setSuccess(false);
            //LDAP: Group
        } catch (NamingException ex) {
            Logger.getLogger(LDAP.class
                    .getName()).log(Level.SEVERE, null, ex);
            result.setSuccess(false);
            result.setException(ex.toString());
        }

        // Create a container set of attributes
        container = new BasicAttributes();

        // Create the objectclass to add
        objClasses = new BasicAttribute("objectClass");
        objClasses.add("top");
        objClasses.add("posixGroup");
        // Assign the name and description to the group
        Attribute group = new BasicAttribute("cn", name);
        String gidnumber = getNextFreeGidNumber();
        Attribute groupgidnumber = new BasicAttribute("gidNumber", gidnumber);
        Attribute groupdesc = new BasicAttribute("description", description);

        // Add these to the container
        container.put(objClasses);
        container.put(group);
        container.put(groupgidnumber);
        container.put(groupdesc);

        // Create the entry
        String groupDN = getGroupDN(name);
        try {
            context.createSubcontext(groupDN, container);
            result.setSuccess(true);
            log.message(true, "NEU: Klasse " + name + " erstellt", null);

        } catch (NamingException ex) {
            Logger.getLogger(LDAP.class
                    .getName()).log(Level.SEVERE, null, ex);
            result.setSuccess(false);
            result.setException(ex.toString());
            log.message(false, "FEHLER: Klasse " + name + " nicht erstellt", ex.toString());
        }
        return result;
    }

    public LDAPResult ResetPassword(String loginName, String newPassword, String userClass, boolean requestFromAdmin) {
        LDAPResult result = new LDAPResult();
        try {
            ModificationItem[] mods = new ModificationItem[2];
            mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("userPassword", newPassword));
            mods[1] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("pwdChangeAllow", "no"));
            String name = getUserDN(loginName, userClass);
            if (requestFromAdmin) {
                //ändert Passwort:
                context.modifyAttributes(name, mods);
                log.message(true, "GEÄNDERT: Passwort von " + loginName + " zurückgesetzt", null);
                result.setSuccess(true);
            } else {
                Attributes attr = context.getAttributes(name);
                String allow = attr.get("pwdChangeAllow").toString();
                // prüft, ob pwd:yes vom Lehrer gesetzt worden ist.
                if (allow.equals("pwdChangeAllow: yes")) {
                    //ändert Passwort:
                    context.modifyAttributes(name, mods);
                    //ändert Passwort auf dem Fileserver:
                    //fsChangeUserPassword(username, password);
                    result.setSuccess(true);
                    log.message(true, "GEÄNDERT: Passwort von " + loginName + " zurückgesetzt", null);
                } else {
                    result.setSuccess(false);
                    result.setException("Nutzer nicht für PW-Aenderung freigeschaltet");
                }
            }
        } catch (NamingException e) {
            result.setSuccess(false);
            result.setException(e.toString());
            log.message(false, "FEHLER: Passwort von " + loginName + " nicht zurückgesetzt", e.toString());
        }
        return result;
    }

    /**
     * @param key
     * @return
     */
    public LDAPResult GetClassFromKey(String key) {
        LDAPResult result = new LDAPResult();
        List classlist = new LinkedList();
        // Set up criteria to search on
        String filter = "(uid=" + key + ")";
        // Set up search constraints
        SearchControls cons = new SearchControls();
        cons.setSearchScope(SearchControls.SUBTREE_SCOPE);
        NamingEnumeration results;
        try {
            results = context.search(ROOT_OU, filter, cons);
            while (results.hasMore()) {
                SearchResult r = (SearchResult) results.next();
                classlist.add(r.getName().split("ou=")[1]);
            }
            if (classlist.isEmpty()) {
                result.setStringResult("nokey");
            } else {
                result.setStringResult(classlist.get(0).toString());
            }
            result.setSuccess(true);
        } catch (NamingException ex) {
            result.setSuccess(true);
            Logger.getLogger(LDAP.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        result.setException(null);
        return result;
    }

    /**
     * @param loginName
     * @return
     */
    public LDAPResult GetClassOfUser(String loginName) {
        LDAPResult result = new LDAPResult();
        List groups = new LinkedList();
        // Set up criteria to search on
        String filter = new StringBuffer().append("(&").append("(objectClass=*)")
                // .append("(uniqueMember=")
                .append("(uid=").append(loginName)
                // .append(",")
                // .append(ROOT_OU)
                .append(")").append(")").toString();
        // Set up search constraints
        SearchControls cons = new SearchControls();
        cons.setSearchScope(SearchControls.SUBTREE_SCOPE);
        NamingEnumeration results;
        try {
            results = context.search(ROOT_OU, filter, cons);
//            while (results.hasMore()) {
            while (results.hasMore()) {
                SearchResult r = (SearchResult) results.next();
                groups.add(r.getName().split("ou=")[1]);
                result.setSuccess(true);
            }
            if (groups.isEmpty()) {
                result.setStringResult("no class");
                result.setSuccess(false);
            } else {
                result.setStringResult(groups.get(0).toString());
            }
//            }
        } catch (NamingException ex) {
            Logger.getLogger(LDAP.class
                    .getName()).log(Level.SEVERE, null, ex);
            result.setSuccess(false);
            result.setException(ex.toString());
        }

        result.setException(null);
        return result;
    }

    /**
     * @return
     */
    public LDAPResult GetAllClasses() {
        LDAPResult result = new LDAPResult();
        try {
            List classlist = new LinkedList();
            // Set up criteria to search on
            String filter = "(objectClass=organizationalUnit)";
            // Set up search constraints
            SearchControls cons = new SearchControls();
            cons.setSearchScope(SearchControls.ONELEVEL_SCOPE);
            NamingEnumeration results = context.search(ROOT_OU, filter, cons);
            while (results.hasMore()) {
                SearchResult r = (SearchResult) results.next();
                String GroupCN = getGroupCN(r.getName());
                classlist.add(GroupCN);
            }
            result.setGroups(classlist);
            result.setSuccess(true);
        } catch (NamingException ex) {
            result.setSuccess(false);
            result.setException(ex.toString());
            Logger.getLogger(LDAP.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    private String getUserUID(String userDN) {
        int start = userDN.indexOf("=");
        int end = userDN.indexOf(",");
        if (end == -1) {
            end = userDN.length();
        }
        return userDN.substring(start + 1, end);
    }

    /**
     * Web service operation
     *
     * @param className OU
     * @param whattoget 3 = all, 2 = onlyKeys, 1 = onlyUsers
     * @return
     */
    public LDAPResult GetClassOUMembers(String className, int whattoget) {
        // user u;
        // whattoget: 3 = all, 2 = onlyKeys, 1 = onlyUsers
        LDAPResult result = new LDAPResult();
        List<userData> members = new LinkedList<>();

        // List members = new LinkedList();
        String ou = "ou=" + className + ",";
        // Set up criteria to search on
        String filter = "(objectClass=organizationalPerson)";

        // Set up search constraints
        SearchControls cons = new SearchControls();
        cons.setSearchScope(SearchControls.ONELEVEL_SCOPE);

        NamingEnumeration enumResults;
        try {
            enumResults = context.search(ou + ROOT_OU, filter, cons);
            while (enumResults.hasMore()) {
                SearchResult r = (SearchResult) enumResults.next();
                userData u = new userData();
                String givenname = r.getAttributes().get("givenName").toString().split(":")[1].trim();
                u.setGivenName(givenname);

// nur User
                if (whattoget == 1 && !givenname.equals("KEY")) {
                    u.setUserLogin(r.getAttributes().get("uid").toString().split(":")[1].trim());
                    u.setUserUidNumber(r.getAttributes().get("uidNumber").toString().split(":")[1].trim());
                    String[] arrActiv = r.getAttributes().get("inetActive").toString().split(":");
                    u.setInetActive(arrActiv[1].trim());
                    u.setUserFullname(r.getAttributes().get("cn").toString().split(":")[1].trim());
                    String[] arrpwdEnabled = r.getAttributes().get("pwdChangeAllow").toString().split(":");
                    u.setUserPW(r.getAttributes().get("userPassword").toString().split(":")[1].trim());
                    u.setPwChangeAllowed(arrpwdEnabled[1].trim());
                    u.setUserName(r.getAttributes().get("sn").toString().split(":")[1].trim());
                    u.setUserClass(className);
                    u.setUserProfil(r.getAttributes().get("profil").toString().split(":")[1].trim());
                    u.setUserEmail(r.getAttributes().get("mail").toString().split(":")[1].trim());
                    u.setGender(r.getAttributes().get("employeeType").toString().split(":")[1].trim());
                    u.setLeavingDate(r.getAttributes().get("telexNumber").toString().split(":")[1].trim());
                    u.setEntryDate(r.getAttributes().get("title").toString().split(":")[1].trim());
                    u.setRentStatus(r.getAttributes().get("gecos").toString().split(":")[1].trim());
                    members.add(u);
                }

// fügt benutzer nur ein, wenn Key
                if (whattoget == 2 && givenname.equals("KEY")) {
                    u.setUserLogin(r.getAttributes().get("uid").toString().split(":")[1].trim());
                    u.setUserUidNumber(r.getAttributes().get("uidNumber").toString().split(":")[1].trim());
                    String[] arrActiv = r.getAttributes().get("inetActive").toString().split(":");
                    u.setInetActive(arrActiv[1].trim());
                    u.setUserFullname(r.getAttributes().get("cn").toString().split(":")[1].trim());
                    u.setUserPW(r.getAttributes().get("cn").toString().split(":")[1].trim());
                    String[] arrpwdEnabled = r.getAttributes().get("pwdChangeAllow").toString().split(":");
                    u.setPwChangeAllowed(arrpwdEnabled[1].trim());
                    u.setUserName(r.getAttributes().get("sn").toString().split(":")[1].trim());
                    u.setUserClass(className);
                    u.setUserProfil(r.getAttributes().get("cn").toString().split(":")[1].trim());
                    u.setUserEmail(r.getAttributes().get("mail").toString().split(":")[1].trim());
                    u.setGender(r.getAttributes().get("employeeType").toString().split(":")[1].trim());
                    u.setLeavingDate(r.getAttributes().get("telexNumber").toString().split(":")[1].trim());
                    u.setEntryDate(r.getAttributes().get("title").toString().split(":")[1].trim());
                    u.setRentStatus(r.getAttributes().get("gecos").toString().split(":")[1].trim());
                    members.add(u);
                }

                // ALL
                if (whattoget == 3) {
                    u.setUserLogin(r.getAttributes().get("uid").toString().split(":")[1].trim());
                    u.setUserUidNumber(r.getAttributes().get("uidNumber").toString().split(":")[1].trim());
                    String[] arrActiv = r.getAttributes().get("inetActive").toString().split(":");
                    u.setInetActive(arrActiv[1].trim());
                    u.setUserFullname(r.getAttributes().get("cn").toString().split(":")[1].trim());
                    String[] arrpwdEnabled = r.getAttributes().get("pwdChangeAllow").toString().split(":");
                    u.setUserPW(r.getAttributes().get("userPassword").toString().split(":")[1].trim());
                    u.setPwChangeAllowed(arrpwdEnabled[1].trim());
                    u.setUserName(r.getAttributes().get("sn").toString().split(":")[1].trim());
                    u.setUserClass(className);
                    u.setUserProfil(r.getAttributes().get("profil").toString().split(":")[1].trim());
                    u.setUserEmail(r.getAttributes().get("mail").toString().split(":")[1].trim());
                    u.setGender(r.getAttributes().get("employeeType").toString().split(":")[1].trim());
                    u.setLeavingDate(r.getAttributes().get("telexNumber").toString().split(":")[1].trim());
                    u.setEntryDate(r.getAttributes().get("title").toString().split(":")[1].trim());
                    u.setRentStatus(r.getAttributes().get("gecos").toString().split(":")[1].trim());
                    members.add(u);
                }
                result.setSuccess(true);
                result.setUserList(members);
            }
        } catch (NamingException ex) {
            result.setSuccess(false);
            Logger.getLogger(LDAP.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    /**
     * @return
     */
    public LDAPResult GetAllUsers() {

        // user u;
        LDAPResult result = new LDAPResult();
        List<userData> members = new LinkedList<>();

        // List members = new LinkedList();
        // Set up criteria to search on
        String filter = "(objectClass=organizationalPerson)";

        // Set up search constraints
        SearchControls cons = new SearchControls();
        cons.setSearchScope(SearchControls.SUBTREE_SCOPE);

        NamingEnumeration enumResults = null;
        try {
            enumResults = context.search(ROOT_OU, filter, cons);

            while (enumResults.hasMore()) {

                SearchResult r = (SearchResult) enumResults.next();
                userData u = new userData();
                String givenname = r.getAttributes().get("givenName").toString().split(":")[1].trim();
                u.setGivenName(givenname);
                // nur User
                //if (!givenname.equals("KEY")) {
                u.setUserLogin(r.getAttributes().get("uid").toString().split(":")[1].trim());
                u.setInetActive(r.getAttributes().get("inetActive").toString().split(":")[1].trim());
                u.setUserFullname(r.getAttributes().get("cn").toString().split(":")[1].trim());
                u.setPwChangeAllowed(r.getAttributes().get("pwdChangeAllow").toString().split(":")[1].trim());
                u.setUserPW(r.getAttributes().get("userPassword").toString().split(":")[1].trim());
                u.setUserName(r.getAttributes().get("sn").toString().split(":")[1].trim());
                u.setUserClass(r.getAttributes().get("ou").toString().split(":")[1].trim());
                u.setUserProfil(r.getAttributes().get("profil").toString().split(":")[1].trim());
                u.setUserEmail(r.getAttributes().get("mail").toString().split(":")[1].trim());
                u.setUserUidNumber(r.getAttributes().get("uidNumber").toString().split(":")[1].trim());
                u.setGender(r.getAttributes().get("employeeType").toString().split(":")[1].trim());
                u.setBirthday(r.getAttributes().get("employeeNumber").toString().split(":")[1].trim());
                u.setLeavingDate(r.getAttributes().get("telexNumber").toString().split(":")[1].trim());
                u.setEntryDate(r.getAttributes().get("title").toString().split(":")[1].trim());
                u.setAttest(r.getAttributes().get("destinationIndicator").toString().split(":")[1].trim());
                u.setAttestDate(r.getAttributes().get("departmentNumber").toString().split(":")[1].trim());
                u.setAttestComment(r.getAttributes().get("description").toString().split(":")[1].trim());
                u.setRentStatus(r.getAttributes().get("gecos").toString().split(":")[1].trim());
                members.add(u);
            }
            result.setSuccess(true);
            result.setUserList(members);
            enumResults.close();
        } catch (Exception ex) {
            result.setSuccess(false);
            result.setException(ex.toString());
            try {
                enumResults.close();
            } catch (NamingException ex1) {
                Logger.getLogger(LDAP.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
        return result;
    }

    /**
     * @param userLogin
     * @return
     */
    public boolean UserLoginExists(String userLogin) {
        try {
            return !getOUs(userLogin, ROOT_OU).isEmpty();
        } catch (NamingException ex) {
            Logger.getLogger(LDAP.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    /**
     * @param givenname
     * @param surname
     * @return
     */
    public LDAPResult GenerateUniqueUserName(String givenname, String surname) {
        LDAPResult result = new LDAPResult();
        String uid = givenname.charAt(0) + "." + surname;
        uid = helper.Replace_Umlauts(uid);
        try {
            while (getOUs(uid, ROOT_OU).isEmpty() == false) {
                Random rand = new Random();
                int ext;
                ext = rand.nextInt(10) + 1;
                uid = uid + ext;
            }
            result.setSuccess(true);
            result.setException("OK");
        } catch (NamingException ex) {
            result.setException(ex.toString());
            result.setSuccess(false);
            Logger.getLogger(LDAP.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        result.setStringResult(uid);
        return result;
    }

    /**
     * Web service operation
     *
     * @param group
     * @return
     */
    public String GetGidNumber(String group) {
        String name = getGroupDN(group);
        try {
            Attributes attr = context.getAttributes(name);
            String gn = attr.get("gidNumber").toString();
            String[] gnarr = gn.split(":");
            return gnarr[1].trim();
        } catch (NamingException ex) {
            Logger.getLogger(LDAP.class
                    .getName()).log(Level.SEVERE, null, ex);
            return "0";
        }
    }

    private String getGroupDN(String name) {
        return new StringBuffer().append("cn=").append(name).append(",").append("ou=").append(name).append(",").append(ROOT_OU).toString();
    }

    /**
     * Web service operation
     *
     * @return
     */
    public String GetNextFreeUidNumber() {
        //ermittelt über cn= NextFreeuidNumber die nächste freie uidNumber für einen Benutzer.
        // inkrementiert NextFreeuidNumber:
        String nextgid;
        String uidNumber = "0";
        String name = getNextGidNumberDN("NextFreeuidNumber");
        try {
            Attributes attr = context.getAttributes(name);
            String gn = attr.get("gidNumber").toString();
            String[] gnarr = gn.split(":");
            int in = Integer.parseInt(gnarr[1].trim());
            uidNumber = String.valueOf(i);
            nextgid = String.valueOf(in + 1);
            ModificationItem[] mods = new ModificationItem[1];
            mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("gidNumber", nextgid));
            context.modifyAttributes(name, mods);
        } catch (NamingException ex) {
            Logger.getLogger(LDAP.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return uidNumber;
    }

    /**
     * Web service operation
     *
     * @return
     */
    public String GetNextFreeGidNumber() {
        //ermittelt über cn= NextFreegidNumber die nächste freie gidNumber für eine Gruppe
        // inkrementiert NextFreegidNumber:
        String nextgid;
        String gidNumber = "0";
        String name = getNextGidNumberDN("NextFreegidNumber");
        try {
            Attributes attr = context.getAttributes(name);
            String gn = attr.get("gidNumber").toString();
            String[] gnarr = gn.split(":");
            int i = Integer.parseInt(gnarr[1].trim());
            gidNumber = String.valueOf(i);
            nextgid = String.valueOf(i + 1);
            ModificationItem[] mods = new ModificationItem[1];
            mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("gidNumber", nextgid));
            context.modifyAttributes(name, mods);
        } catch (NamingException ex) {
            Logger.getLogger(LDAP.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return gidNumber;
    }

    private String getGroupCN(String groupDN) {
        int start = groupDN.indexOf("=");
        int end = groupDN.indexOf(",");
        if (end == -1) {
            end = groupDN.length();
        }
        return groupDN.substring(start + 1, end);
    }

    private String getNextGidNumberDN(String name) {
        return new StringBuffer().append("cn=").append(name).append(",").append(ROOT_OU).toString();
    }

    private String getNextFreeGidNumber() {
        //ermittelt über cn= NextFreegidNumber die nächste freie gidNumber für eine Gruppe
        // inkrementiert NextFreegidNumber:
        String nextgid = "0";
        String gidNumber = "0";
        String name = getNextGidNumberDN("NextFreegidNumber");
        try {
            Attributes attr = context.getAttributes(name);
            String gn = attr.get("gidNumber").toString();
            String[] gnarr = gn.split(":");
            int i = Integer.parseInt(gnarr[1].trim());
            gidNumber = String.valueOf(i);
            nextgid = String.valueOf(i + 1);
            ModificationItem[] mods = new ModificationItem[1];
            mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("gidNumber", nextgid));
            context.modifyAttributes(name, mods);
        } catch (NamingException ex) {
            Logger.getLogger(LDAP.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return gidNumber;
    }

    private String getNextFreeUidNumber() {
        //ermittelt über cn= NextFreeuidNumber die nächste freie uidNumber für einen Benutzer.
        // inkrementiert NextFreeuidNumber:
        //String nextgid = "0";
        String uidNumber = "0";
        String name = getNextGidNumberDN("NextFreeuidNumber");
        try {
            Attributes attr = context.getAttributes(name);
            String gn = attr.get("gidNumber").toString();
            String[] gnarr = gn.split(":");
            int i = Integer.parseInt(gnarr[1].trim());
            uidNumber = String.valueOf(i);
            //nextgid = String.valueOf(i + 1);
            ModificationItem[] mods = new ModificationItem[1];
            mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("gidNumber", String.valueOf(i + 1)));
            context.modifyAttributes(name, mods);
        } catch (NamingException ex) {
            Logger.getLogger(LDAP.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return uidNumber;
    }

    private String getOuDN(String name) {
        return new StringBuffer().append("ou=").append(name).append(",").append(ROOT_OU).toString();
    }

    private List getOUs(String username, String rootou) throws NamingException {
        List groups = new LinkedList();

        // Set up criteria to search on
        String filter = new StringBuffer().append("(&").append("(objectClass=*)")
                // .append("(uniqueMember=")
                .append("(uid=").append(username)
                // .append(",")
                // .append(ROOT_OU)
                .append(")").append(")").toString();

        // Set up search constraints
        SearchControls cons = new SearchControls();
        cons.setSearchScope(SearchControls.SUBTREE_SCOPE);

        NamingEnumeration results = context.search(ROOT_OU, filter, cons);

        while (results.hasMore()) {
            SearchResult result = (SearchResult) results.next();
            groups.add(getGroupCN(result.getName()));
        }

        return groups;
    }

    private String getUserDN(String username, String ou) {
        return new StringBuffer().append("uid=").append(username).append(",ou=").append(ou).append(",").append(ROOT_OU)
                .toString();
    }

    private String getUserDNFromUidNumber(String uidNumber) throws NamingException {
        List ids = new LinkedList();
        String filter = new StringBuffer().append("(&").append("(objectClass=*)")
                // .append("(uniqueMember=")
                .append("(uidNumber=").append(uidNumber)
                // .append(",")
                // .append(ROOT_OU)
                .append(")").append(")").toString();

        // Set up search constraints
        SearchControls cons = new SearchControls();
        cons.setSearchScope(SearchControls.SUBTREE_SCOPE);

        NamingEnumeration results = context.search(ROOT_OU, filter, cons);

        while (results.hasMore()) {
            SearchResult result = (SearchResult) results.next();
            ids.add(result.getName());
        }

        String uid = ids.get(0).toString().split(",")[0];
        String ou = ids.get(0).toString().split(",")[1];

        String userDN = uid + "," + ou + "," + ROOT_OU;
        // username und OU !!!!!!
        //return new StringBuffer().append(uid).append(ou).append(",").append(ROOT_OU).toString();
        return userDN;
    }

    // TODO REMOVE 2022 Irgendwann überflüssig:
    public LDAPResult UntisUpdateLDAP(String uid, String gender, String birthday) {
        LDAPResult result = new LDAPResult();
        LDAPResult u = getUserData(uid, "", "");
        String uidNumber = u.getUser().getUserUidNumber();
        ModificationItem[] mods = new ModificationItem[2];
        mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("employeeType", gender));
        mods[1] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("employeeNumber", birthday));
        String userDN;
        try {
            userDN = getUserDNFromUidNumber(uidNumber);
            context.modifyAttributes(userDN, mods);
            result.setSuccess(true);
            result.setStringResult("Daten ergänzt");
            // Änderung speichern in Datenbank Portal
            untisExport.UntisDiff(uidNumber, "", "LDAP-Update");
            log.message(true, "LDAPUPDATE: LDAP UPDATE für: " + uid + " erfolgreich", null);
            return result;
        } catch (NamingException ex) {
            result.setSuccess(false);
            result.setStringResult("Daten nicht ergänzt");
            result.setException(ex.toString());
            Logger.getLogger(LDAP.class.getName()).log(Level.SEVERE, null, ex);
            log.message(false, "FEHLER: LDAP UPDATE fehlgeschlagen", ex.toString());
            return result;
        }
    }

    public LDAPResult updateLDAP(String uidNumber, String uid, String givenName, String sn, String email, String
            entryDate, String leavingDate, String gender, String birthday, String classname) {
        LDAPResult result = new LDAPResult();
        ModificationItem[] mods = new ModificationItem[5];
        mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("title", entryDate));
        mods[1] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("telexNumber", leavingDate));
        mods[2] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("employeeType", gender));
        mods[3] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("employeeNumber", birthday));
        mods[4] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("ou", classname));
        String userDN;
        try {
            userDN = getUserDNFromUidNumber(uidNumber);
            context.modifyAttributes(userDN, mods);
            result.setSuccess(true);
            result.setStringResult("Eintrittsdatum geändert.");
            // Änderung speichern in Datenbank Portal
            untisExport.UntisDiff(uidNumber, "", "LDAP-Update");
            return result;
        } catch (NamingException ex) {
            result.setSuccess(false);
            result.setStringResult("Update nicht erfolgreich");
            result.setException(ex.toString());
            Logger.getLogger(LDAP.class.getName()).log(Level.SEVERE, null, ex);
            return result;
        }
    }
}
