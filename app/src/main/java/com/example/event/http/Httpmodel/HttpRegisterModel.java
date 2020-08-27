package com.example.event.http.Httpmodel;

public class HttpRegisterModel {
    /**
     * phoneNum : string
     * unitName : string
     * project : string
     * userName : string
     * password : string
     * rePassword : string
     */

    private String phoneNum;
    private String unitName;
    private String project;
    private String userName;
    private String password;
    private String rePassword;

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRePassword() {
        return rePassword;
    }

    public void setRePassword(String rePassword) {
        this.rePassword = rePassword;
    }
}
