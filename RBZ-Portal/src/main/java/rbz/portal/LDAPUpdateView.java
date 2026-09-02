/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbz.portal;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Andreas
 */
@RequestScoped
@ManagedBean(name = "LDAPUpdateView")
public class LDAPUpdateView implements Serializable {

    private UploadedFile file;

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    private Helper helper = new Helper();

    /**
     * Creates a new instance of fileUpload
     */
    public LDAPUpdateView() {
    }

    public void handleLDAPUpdateView(FileUploadEvent event) {
        FacesMessage msg = new FacesMessage("Erfolgreich", event.getFile().getFileName() + " ist importiert.");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        LDAPResult result = new LDAPResult();
        LDAP ldap = new LDAP();
        //ClassAdminView clav = new ClassAdminView();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(event.getFile().getInputStream()));
            //StringBuilder out = new StringBuilder();
            String classname;
            String uidNumber;
            String uid;
            String givenName;
            String sn;
            String email;
            String entryDate;
            String leavingDate;
            String birthday;
            String gender;
            String data[];
            String fileName = "Update-Error.log";
            String encoding = "UTF-8";
            PrintWriter writer = new PrintWriter(fileName, encoding);
            String line;
            while ((line = reader.readLine()) != null) {
                data = line.split(";");

                uidNumber = data[0];
                uid = data[1];
                givenName = data[2];
                sn = data[3];
                email = data[4];
                entryDate = data[5];
                leavingDate = data[6];
                gender = data[7];
                birthday = data[8];
                // classenname wird dem Schema angepasst: XXXzzx
                classname = helper.Classname_Check(data[9]);
                result = ldap.updateLDAP(uidNumber, uid, givenName, sn, email, entryDate, leavingDate, gender, birthday, classname);
                if (!result.isSuccess()) {
                    writer.println("FEHLER: " + uidNumber + ", " + result.getException());
                }
            }
            writer.close();
            ldap.closeLDAPContext();
        } catch (IOException ex) {
            //Logging
            ldap.closeLDAPContext();
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage("LDAP Update", "Fehler: " + "Fehler beim Update des LDAP"));
        } finally {
            try {
                reader.close();
                ldap.closeLDAPContext();
            } catch (IOException ex) {
                ldap.closeLDAPContext();
                //Logging
            }
        }
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage("LDAP Update", "Meldung: " + "Update erfolgreich"));
    }


    public void handleFileUpload(FileUploadEvent event) {
        FacesMessage msg = new FacesMessage("Successful", event.getFile().getFileName() + " is uploaded.");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        ClassAdminView clav = new ClassAdminView();
        InputStream ips;
        BufferedReader reader = null;
        try {
            ips = event.getFile().getInputStream();
            reader = new BufferedReader(new InputStreamReader(ips));
            StringBuilder out = new StringBuilder();
            String line;
            String data[];
            while ((line = reader.readLine()) != null) {
                data = line.split(";");
                String classname = data[0];
                int count = Integer.parseInt(data[1]);
                clav.setClassName(classname);
                clav.setCount(count);
                clav.genClassKeys();
            }
            System.out.println(out.toString());   //Prints the string content read from input stream
        } catch (IOException ex) {
            Logger.getLogger(LDAPUpdateView.class.getName()).log(Level.SEVERE, null, ex);
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage("Erfolglos", "Fehler: " + "Fehler beim Import / Anlegen der Klassen"));
        } finally {
            try {
                reader.close();
            } catch (IOException ex) {
                //Logging
            }
        }
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage("Import erfolgreich", "Meldung: " + "Import erfolgreich"));
    }
}
