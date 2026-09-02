/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbz.portal;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.naming.directory.SearchResult;
import java.util.LinkedList;
import java.util.List;

/**
 * @author EDV-PC-Andreas
 */
@Named
@ApplicationScoped
public class WSLDAPService {
    private LDAPResult r;

    public List<userData> GetAllPortalUsers() {

        LDAP ldap = new LDAP();
        r = ldap.GetAllUsers();
        // CLOSE LDAP CONTEXT
        ldap.closeLDAPContext();
        return r.getUserList();
    }

    public LDAPResult getR() {
        return r;
    }

    public void setR(LDAPResult r) {
        this.r = r;
    }

}
