package com.yjl.assemblycappsystem.bean;

import javax.persistence.Id;
import java.io.Serializable;

public class UmsUserSearchInfo implements Serializable {

    @Id
    private String employeeId;
    private String username;
    private String email;
    private String phone;
    private String role;
    private String lastLoginDate;

    @Override
    public String toString() {
        return "UmsUserSearchInfo{" +
                "employeeId=" + employeeId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", role='" + role + '\'' +
                ", lastLoginDate='" + lastLoginDate + '\'' +
                '}';
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(String lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }
}
