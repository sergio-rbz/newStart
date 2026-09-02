/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbz.portal;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import java.io.InputStream;

/**
 * @author andreas
 */

@ManagedBean(name = "fileDownload")
public class fileDownload {

    /**
     * @author Andreas
     */
    private StreamedContent file;
    private String filename = "Benutzerordnung.pdf";

    public fileDownload() {
        InputStream stream = ((ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext()).getResourceAsStream("/resources/downloads/Benutzerordnung.pdf");
        file = new DefaultStreamedContent(stream, "application/pdf", "downloaded_Benutzerordnung.pdf");
    }

    public StreamedContent getFile() {
        return file;
    }

    /**
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }

    /**
     * @param filename the filename to set
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }
}
