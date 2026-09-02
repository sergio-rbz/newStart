/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbz.portal;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import javax.faces.bean.ManagedBean;
import java.io.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author andreas
 */
@ManagedBean(name = "UntisDiffCsvDownload")
public class UntisDiffCsvDownload {

    //UntisExport ux = new UntisExport();
    private StreamedContent file;
    private StreamedContent kompleteLDAPFile;

    File temp;
    String downloadfile = "";

    public UntisDiffCsvDownload() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        Date currentTime = new Date();
        String date = formatter.format(currentTime);
        try {
            temp = File.createTempFile("Untis-Export-" + date + "-", ".csv");
            downloadfile = temp.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        UntisExport ux = new UntisExport();
        try {
            ux.UntisDiffCSVExport(downloadfile);
        } catch (IOException e) {
        } catch (SQLException ex) {
            Logger.getLogger(UntisDiffCsvDownload.class.getName()).log(Level.SEVERE, null, ex);
        }

        //InputStream location = FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream(downloadfile);
        try {
            InputStream location = new FileInputStream(downloadfile);
            file = DefaultStreamedContent.builder()
                    .name("Untis-Export-" + date + ".csv")
                    .contentType("application/csv")
                    .stream(() -> location)
                    .build();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(UntisDiffCsvDownload.class.getName()).log(Level.SEVERE, null, ex);
        }
        temp.delete();
    }

    public StreamedContent getFile() {
        return file;
    }

    public StreamedContent getKompleteLDAPFile() {
        return file;
    }
}
