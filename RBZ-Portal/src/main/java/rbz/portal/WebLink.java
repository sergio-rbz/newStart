/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbz.portal;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.io.Serializable;

/**
 * @author EDV-PC-Andreas
 */

@ManagedBean(name = "WebLinks")
@SessionScoped
public class WebLink implements Serializable {

    private final ini i = new ini(); // Konfigurationsvariablen
    private String name; //Name des Weblinks
    private String url; // Weblink zur Webseite
    private String role; // Rolle für Anzeige
    private String image; // Bild zum Link
    private String cat; //Kategorie
    private String desc; //Beschreibung
    private String tooltip; //Anzeidetext Tooltip


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCat() {
        return cat;
    }

    public void setCat(String cat) {
        this.cat = cat;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

}