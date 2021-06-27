package com.yjl.assemblycappsystem.bean;


import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Transient;
import java.io.Serializable;

public class DmsProcessDocument implements Serializable {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer uploadUserId;
    private String uploadDate;
    private String updateTimes;
    private String size;
    private String documentName;
    private String url;
    @Transient
    private UmsUserInfo umsUserInfo;
    @Transient
    private String uploadUsername;
    @Transient
    private String uploadUserEmail;


    public String getUploadUsername() {
        return uploadUsername;
    }

    public void setUploadUsername(String uploadUsername) {
        this.uploadUsername = uploadUsername;
    }

    public String getUploadUserEmail() {
        return uploadUserEmail;
    }

    public void setUploadUserEmail(String uploadUserEmail) {
        this.uploadUserEmail = uploadUserEmail;
    }






    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUploadUserId() {
        return uploadUserId;
    }

    public void setUploadUserId(Integer uploadUserId) {
        this.uploadUserId = uploadUserId;
    }

    public String getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(String uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getUpdateTimes() {
        return updateTimes;
    }

    public void setUpdateTimes(String updateTimes) {
        this.updateTimes = updateTimes;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public UmsUserInfo getUmsUserInfo() {
        return umsUserInfo;
    }

    public void setUmsUserInfo(UmsUserInfo umsUserInfo) {
        this.umsUserInfo = umsUserInfo;
    }
}
