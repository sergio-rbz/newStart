package rbz.portal;


import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


@RequestScoped
@ManagedBean(name = "WinschoolFileUploadView")
public class WinschoolFileUploadView {
    private UploadedFile file;
    private LDAP ldap = new LDAP();
    private List<userData> allUserList;
    private LDAPResult r;
    BufferedReader reader = null;

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    public void upload() {
        if (file != null) {
            FacesMessage message = new FacesMessage("Successful", file.getFileName() + " is uploaded.");
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }

    public void handleFileUpload(FileUploadEvent event) throws IOException {
        r = ldap.GetAllUsers();
        List records = new ArrayList<>();


        InputStream ips;

        try {
            ips = event.getFile().getInputStream();
            reader = new BufferedReader(new InputStreamReader(ips));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String line;
        while ((line = reader.readLine()) != null) {
            UpdateLDAPUser(line);
        }
        FacesMessage message = new FacesMessage("Successful", event.getFile().getFileName() + " is uploaded.");
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    void UpdateLDAPUser(String line) throws IOException {
        String ua[];
        ua = line.split(";");


        allUserList = r.getUserList();


        for (userData d : allUserList) {
            if (d.givenName.equals(ua[0]) && d.userName.equals(ua[1]) && d.gender.equals(ua[4]) && d.birthday.equals(ua[5]) && d.userClass.equals(ua[6])) {
                ModifyLeavingData(d.userUidNumber, ua[3]);
            }
        }

//    ldap.closeLDAPContext();
//    reader.close();
    }

    void ModifyLeavingData(String userUidNumber, String entryDate) {
        //ldap.setEntryDate(userUidNumber,entryDate);

    }

}
