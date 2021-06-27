package com.yjl.assemblycappsystem.bean;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;


public class UmsUserInfo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String username;
    private String password;
    private String role;
    private String phone;
    private String employeeId;
    private String email;
    private String salt;
    private String headPortraitsUrl;
    private String isBan;
    private String lastLoginDate;


    @Override
    public String toString() {
        return "UmsUserInfo{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", role='" + role + '\'' +
                ", phone='" + phone + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", email='" + email + '\'' +
                ", salt='" + salt + '\'' +
                ", headPortraitsUrl='" + headPortraitsUrl + '\'' +
                ", isBan='" + isBan + '\'' +
                ", lastLoginDate='" + lastLoginDate + '\'' +
                '}';
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getHeadPortraitsUrl() {
        return headPortraitsUrl;
    }

    public void setHeadPortraitsUrl(String headPortraitsUrl) {
        this.headPortraitsUrl = headPortraitsUrl;
    }

    public String getIsBan() {
        return isBan;
    }

    public void setIsBan(String isBan) {
        this.isBan = isBan;
    }

    public String getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(String lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

}
