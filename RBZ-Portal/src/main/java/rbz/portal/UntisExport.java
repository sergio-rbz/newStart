/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbz.portal;

import com.opencsv.CSVWriter;

import javax.faces.bean.ManagedBean;
import javax.inject.Named;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author andreas
 */
@Named(value = "UntisExport")
public class UntisExport implements Serializable {

    private String givenName;
    private String userName;
    private String userLogin;
    private String password;
    private String userEmail;
    private String profil;
    private String gender;
    private String userClass;
    private String UntisCsvFile;
    private Helper helper;

    public UntisExport() {

        this.helper = new Helper();

    }

    //    public boolean UntisNew(userData userdata) {
//        //ist user schon in Differenz enthalten dann mit update versehen.
//        //UserID ist nicht vorhanden: Datensatz zu user suchen und in Datenbank eintragen.
//        return true;
//    }
    // Export aller Userdaten für Untis Abgleich
    public void updateLDAP() {

    }

    public boolean UntisAll(String file) throws IOException {
        LDAPResult r = null;
        LDAP ldap = new LDAP();
        r = ldap.GetAllUsers();
        CSVWriter writer = new CSVWriter(new FileWriter(file),
                ';',
                CSVWriter.NO_ESCAPE_CHARACTER,
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.RFC4180_LINE_END);
        Boolean includeHeaders = true;
        List<userData> u = new ArrayList<>();
        u = r.getUserList();
        String[] kopfzeile = {"externeID", "Kurzname", "Vorname", "Nachname", "email", "Eintrittsdatum", "Austrittsdatum", "Geschlecht", "Geburtstag", "Klasse"};
        writer.writeNext(kopfzeile, true);
        for (userData ud : u) {
            if (!ud.givenName.equals("KEY") & !ud.userClass.equals("LEHRER")) {
                String[] user = {
                        "2" + ud.userUidNumber,
                        ud.userLogin,
                        ud.givenName,
                        ud.userName,
                        ud.userEmail,
                        ud.entryDate,
                        ud.leavingDate,
                        ud.gender,
                        ud.birthday,
                        helper.Classname_Check(ud.userClass)};
                writer.writeNext(user, true);
            }
        }
        writer.flush();
        writer.close();
        ldap.closeLDAPContext();
        return true;
    }

    public boolean UntisDiff(String uidNumber, String userLogin, String whatChanged) {
        ini i = new ini();
        LDAPResult r = null;
        if (!uidNumber.equals("")) {
            LDAP ldap = new LDAP();
            r = ldap.getUserData("", uidNumber, "");
        }
        if (!userLogin.equals("")) {
            LDAP ldap = new LDAP();
            r = ldap.getUserData(userLogin, "", "");
        }

        Sql sql = new Sql();
        Connection con = sql.GetSQLConnect();
        PreparedStatement st = null;

        try {
            Timestamp currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
            //Wenn ID vorhanden Eintrag löschen.
            st = con.prepareStatement("DELETE FROM portal.changes where uidnumber = " + r.getUser().userUidNumber + ";");
            st.executeUpdate();
            //Änderung eintragen
            st = con.prepareStatement("insert into portal.changes values (default, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
            st.setString(1, r.getUser().getUserUidNumber());
            st.setString(2, r.getUser().getUserLogin());
            st.setString(3, r.getUser().getGivenName());
            st.setString(4, r.getUser().getUserName());
            st.setString(5, r.getUser().getUserEmail());
            st.setString(6, r.getUser().getEntryDate());
            if (r.getUser().getLeavingDate().equalsIgnoreCase("n.a") && (whatChanged.equalsIgnoreCase("Austrittsdatum"))){
                st.setString(7, i.getDateForExitDateDelete()); //wenn Austrittsdatum gelöscht wird, setze Wert auf 01.01.2030 !! Muss angepasst werden !!
            } else {
                st.setString(7, r.getUser().getLeavingDate());
            }
            st.setString(8, r.getUser().getGender());
            st.setString(9, r.getUser().getBirthday());
            st.setString(10, helper.Classname_Check(r.getUser().getUserClass()));
            st.setString(11, r.getUser().getAttest());
            st.setString(12, r.getUser().getAttestDate());
            st.setString(13, r.getUser().getAttestComment());
            st.setString(14, r.getUser().getUserFullname());
            st.setString(15, r.getUser().getInetActive());
            st.setString(16, whatChanged);
            st.setTimestamp(17, currentTimestamp);
            st.setString(18, "no");
            st.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(UntisExport.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(UntisExport.class.getName()).log(Level.SEVERE, null, ex);
        }

        //ist user schon in Differenz enthalten dann mit update versehen.
        //UserID ist nicht vorhanden: Datensatz zu user suchen und in Datenbank eintragen.

        return true;
    }

    public void UntisDiffCSVExport(String file) throws IOException, SQLException {
        CSVWriter writer = new CSVWriter(new FileWriter(file),
                ';',
                CSVWriter.NO_ESCAPE_CHARACTER,
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.RFC4180_LINE_END);
        Boolean includeHeaders = true;
        Statement statement = null;
        ResultSet myResultSet = null;
        Connection connection = null;
//        String[] kopfzeile = {"externe-ID", "Kurzname", "Vorname", "Nachname", "Email", "Eintrittsdatum", "Austrittsdatum", "Geschlecht", "Geburtstag", "Klasse", "Attest", "Attest Datum", "Attest Kommentar", "Kompletter Name", "aktiv", "Änderung", "Zeit der Änderung", "exportiert"};
//        writer.writeNext(kopfzeile, true);

        Sql sql = new Sql();
        connection = sql.GetSQLConnect();
        if (connection != null) {
            statement = connection.createStatement();
            ResultSet r = statement.executeQuery("SELECT * FROM changes WHERE export = 'no' AND entryDate != 'n.a' AND gender != 'n.a' AND birthday != 'n.a';");
            while (r.next()) {
                String[] user = {
                        "2" + r.getString(2),
                        r.getString(3),
                        r.getString(4),
                        r.getString(5),
                        r.getString(6),
                        r.getString(7),
                        r.getString(8),
                        r.getString(9),
                        r.getString(10),
                        r.getString(11),
                        r.getString(12),
                        r.getString(13),
                        r.getString(14),
                        r.getString(15),
                        r.getString(16),
                        r.getString(17),
                        r.getString(18),
                        r.getString(19)
                };
                //helper.Classname_Check(ud.userClass)};
                writer.writeNext(user, false);
            }
        }
        writer.flush();
        writer.close();
        statement.executeQuery("UPDATE changes SET export = 'yes' WHERE export = 'no';");
        connection.close();
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

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getProfil() {
        return profil;
    }

    public void setProfil(String profil) {
        this.profil = profil;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getUserClass() {
        return userClass;
    }

    public void setUserClass(String userClass) {
        this.userClass = userClass;
    }

    public String getUntisCsvFile() {
        return UntisCsvFile;
    }

    public void setUntisCsvFile(String UntisCsvFile) {
        this.UntisCsvFile = UntisCsvFile;
    }
}
