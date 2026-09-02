/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbz.portal;


import javax.faces.bean.ManagedBean;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;


/**
 * @author EDV-PC-Andreas
 */

@ManagedBean
public class LDAPResult implements Serializable {
    private boolean success;
    private String exception;
    private String stringResult;
    private userData user = new userData();
    private List<userData> userList = new LinkedList<>();
    private List<String> groups = new LinkedList<>();

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public String getStringResult() {
        return stringResult;
    }

    public void setStringResult(String stringResult) {
        this.stringResult = stringResult;
    }

    public userData getUser() {
        return user;
    }

    public void setUser(userData user) {
        this.user = user;
    }

    public List<userData> getUserList() {
        return userList;
    }

    public void setUserList(List<userData> userList) {
        this.userList = userList;
    }

    public List<String> getGroups() {
        return groups;
    }

    /**
     * @param groups
     */
    public void setGroups(List<String> groups) {
        this.groups = groups;
    }
}
