package com.yjl.assemblycappsystem.bean;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import java.io.Serializable;

public class MmsModuleDesignJson implements Serializable {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    private Integer documentId;
    private String designJson;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Integer documentId) {
        this.documentId = documentId;
    }

    public String getDesignJson() {
        return designJson;
    }

    public void setDesignJson(String designJson) {
        this.designJson = designJson;
    }
}
