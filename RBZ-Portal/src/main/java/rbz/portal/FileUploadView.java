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
@ManagedBean(name = "FileUploadView")
public class FileUploadView implements Serializable {

    private UploadedFile file;

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    /**
     * Creates a new instance of fileUpload
     */
    public FileUploadView() {
    }

    public void handleCsvFileUpload(FileUploadEvent event) {
        FacesMessage msg = new FacesMessage("Erfolgreich", event.getFile().getFileName() + " ist importiert.");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        LDAP ldap = new LDAP();
        //ClassAdminView clav = new ClassAdminView();
        InputStream ips;
        BufferedReader reader = null;
        try {
            ips = event.getFile().getInputStream();
            reader = new BufferedReader(new InputStreamReader(ips));
            //StringBuilder out = new StringBuilder();
            String line;
            String classname;
            String userLogin;
            String givenName;
            String sn;
            String email;
            //String password;
            String cn;
            String gender;
            String profil;
            //String ou;

            String data[];
            while ((line = reader.readLine()) != null) {
                data = line.split(";");
                classname = data[0].toUpperCase();
                givenName = data[1];
                sn = data[2];
                cn = givenName + " " + sn;
                profil = "Public";
                //ou = "classname";
                if (data.length < 4) {
                    email = cn + "@xxxx.xx";
                } else {
                    email = data[3].toLowerCase();
                }
                LDAPResult r = ldap.GenerateUniqueUserName(givenName, sn);
                userLogin = r.getStringResult().toLowerCase().replaceAll(" ", "");
                ldap.addGroup(classname, "RBZ SP Klasse");
                ldap.UserAdd(sn, givenName, userLogin, "Winter!2020", email, classname, profil, "", "birthday", false);
            }
            ldap.closeLDAPContext();
        } catch (IOException ex) {
            //Logging
            ldap.closeLDAPContext();
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage("Erfolglos", "Fehler: " + "Fehler beim Import / Anlegen der Klassen"));
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
        context.addMessage(null, new FacesMessage("Import erfolgreich", "Meldung: " + "Import erfolgreich"));
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
            Logger.getLogger(FileUploadView.class.getName()).log(Level.SEVERE, null, ex);
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
