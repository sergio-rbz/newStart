/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbz.portal;

import javax.annotation.ManagedBean;
import javax.inject.Named;
import java.io.Serializable;

/**
 * @author EDV-PC-Andreas
 */
@Named (value="userData")
public class userData implements Serializable {

    public userData() {
    }

    public String userLogin;
    public String userFullname;
    public String givenName;
    public String userName;
    public String userPW;
    public String userClass;
    public String userProfil;
    public String userUidNumber;
    public String userEmail;
    public String inetActive;
    public String pwChangeAllowed;
    public String gender;
    public String leavingDate;
    public String entryDate;
    public String birthday;
    public String attest;
    public String attestComment;
    public String attestDate;
    public String rentStatus;
    public String getRentStatus() {
        return rentStatus;
    }
    public void setRentStatus(String rentStatus) {
        this.rentStatus = rentStatus;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.userLogin != null ? this.userLogin.hashCode() : 0);
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
        final userData other = (userData) obj;
        if ((this.userLogin == null) ? (other.userLogin != null) : !this.userLogin.equals(other.userLogin)) {
            return false;
        }
        return true;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPW() {
        return userPW;
    }

    public void setUserPW(String userPW) {
        this.userPW = userPW;
    }

    public String getUserClass() {
        return userClass;
    }

    public void setUserClass(String userClass) {
        this.userClass = userClass;
    }

    public String getUserProfil() {
        return userProfil;
    }

    public void setUserProfil(String userProfil) {
        this.userProfil = userProfil;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserUidNumber() {
        return userUidNumber;
    }

    public void setUserUidNumber(String userUidNumber) {
        this.userUidNumber = userUidNumber;
    }

    public String getInetActive() {
        return inetActive;
    }

    public void setInetActive(String inetActive) {
        this.inetActive = inetActive;
    }

    public String getPwChangeAllowed() {
        return pwChangeAllowed;
    }

    public void setPwChangeAllowed(String pwChangeAllowed) {
        this.pwChangeAllowed = pwChangeAllowed;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getLeavingDate() {
        return leavingDate;
    }

    public void setLeavingDate(String leavingDate) {
        this.leavingDate = leavingDate;
    }

    public String getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(String entryDate) {
        this.entryDate = entryDate;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getAttest() {
        return attest;
    }

    public void setAttest(String attest) {
        this.attest = attest;
    }

    public String getAttestComment() {
        return attestComment;
    }

    public void setAttestComment(String attestComment) {
        this.attestComment = attestComment;
    }

    public String getAttestDate() {
        return attestDate;
    }

    public void setAttestDate(String attestDate) {
        this.attestDate = attestDate;
    }
}
