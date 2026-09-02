/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbz.jamf;

import javax.faces.bean.ManagedBean;
import java.io.Serializable;


/**
 * @author EDV-PC-Andreas
 */
@ManagedBean(name = "JamfUserData")
public class JamfUserData implements Serializable {

    public JamfUserData() {
    }

    public String uid;
    public String userFullname;
    public String givenName;
    public String sn;
    public String ou;
    public String userProfil;
    public String uidNumber;
    public String email;
    public String deviceId;
    public String issued;
    public String approved;
    public String returned_ok;
    public String announced;
    public String returned_problem;
    public String description;
    public String status;

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.uid != null ? this.uid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final JamfUserData other = (JamfUserData) obj;
        if ((this.uid == null) ? (other.uid != null) : !this.uid.equals(other.uid)) {
            return false;
        }
        return true;
    }

    // ------------------ Getter und Setter -----------------------
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUserFullname() {
        return userFullname;
    }

    public void setUserFullname(String userFullname) {
        this.userFullname = userFullname;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getOu() {
        return ou;
    }

    public void setOu(String ou) {
        this.ou = ou;
    }

    public String getUserProfil() {
        return userProfil;
    }

    public void setUserProfil(String userProfil) {
        this.userProfil = userProfil;
    }

    public String getUidNumber() {
        return uidNumber;
    }

    public void setUidNumber(String uidNumber) {
        this.uidNumber = uidNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIssued() {
        return issued;
    }

    public void setIssued(String issued) {
        this.issued = issued;
    }

    public String getAnnounced() {
        return announced;
    }

    public void setAnnounced(String announced) {
        this.announced = announced;
    }

    public String getApproved() {
        return approved;
    }

    public void setApproved(String approved) {
        this.approved = approved;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getReturned_problem() {
        return returned_problem;
    }

    public void setReturned_problem(String returned_Problem) {
        this.returned_problem = returned_Problem;
    }
}
