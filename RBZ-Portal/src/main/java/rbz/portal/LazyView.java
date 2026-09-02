/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbz.portal;

import org.primefaces.model.LazyDataModel;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

@Named("dtLazyView")
@ViewScoped
public class LazyView implements Serializable {
    private LazyDataModel<userData> lazyModel;
    @Inject
    private WSLDAPService service;

    @PostConstruct
    public void init() {
        lazyModel = new LazyClassAdminDataModel(service.GetAllPortalUsers());

    }

    public LazyDataModel<userData> getLazyModel() {
        return lazyModel;
    }

    public void setLazyModel(LazyDataModel<userData> lazyModel) {
        this.lazyModel = lazyModel;
    }


}
