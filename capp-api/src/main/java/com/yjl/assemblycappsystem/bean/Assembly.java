package com.yjl.assemblycappsystem.bean;

import java.io.Serializable;
import java.util.List;

public class Assembly implements Serializable {
    /**
     *     Public assemblyChildren As List(Of Assembly)
     *     Public partChildren As List(Of Part)
     *     Public name As String
     */
    public List<Assembly> assemblyChildren;
    public List<Part> partChildren;
    public String name;

    public List<Assembly> getAssemblyChildren() {
        return assemblyChildren;
    }

    public void setAssemblyChildren(List<Assembly> assemblyChildren) {
        this.assemblyChildren = assemblyChildren;
    }

    public List<Part> getPartChildren() {
        return partChildren;
    }

    public void setPartChildren(List<Part> partChildren) {
        this.partChildren = partChildren;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
