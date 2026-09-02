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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author andreas
 */
@ManagedBean(name = "UntisKompleteCsvDownload")
public class UntisKompleteCsvDownload {

    private StreamedContent file;

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    private String filter;

    File temp;
    String downloadfile = "";

    public UntisKompleteCsvDownload() {
        //public void Download(String file, String filter) {
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
            ux.UntisAll(downloadfile);
        } catch (IOException e) {
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
            Logger.getLogger(UntisKompleteCsvDownload.class.getName()).log(Level.SEVERE, null, ex);
        }
        temp.delete();
    }

    public StreamedContent getFile() {
        return file;
    }
}
