package com.yjl.assemblycappsystem.bean;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import java.io.Serializable;

public class MmsModuleFeatureTreeInfo implements Serializable {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer moduleId;
    private Integer parentAssemblyId;
    private String partName;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getModuleId() {
        return moduleId;
    }

    public void setModuleId(Integer moduleId) {
        this.moduleId = moduleId;
    }

    public Integer getParentAssemblyId() {
        return parentAssemblyId;
    }

    public void setParentAssemblyId(Integer parentAssemblyId) {
        this.parentAssemblyId = parentAssemblyId;
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }
}
