package com.yjl.assemblycappsystem.service;



import com.yjl.assemblycappsystem.bean.DmsModuleDocument;
import com.yjl.assemblycappsystem.bean.DmsProcessDocument;
import com.yjl.assemblycappsystem.bean.MmsModuleDesignJson;
import com.yjl.assemblycappsystem.bean.PmsProcessDocumentText;

import java.util.List;

public interface DocumentService {
    List<DmsProcessDocument> getAllProcessDocuments(String page, String limit);

    String saveDocument(DmsProcessDocument dmsProcessDocument);

    String dropDocumentById(String id);

    List<DmsModuleDocument> getAllModuleDocuments(String page, String limit);

    String saveModuleDocument(DmsModuleDocument dmsModuleDocument);

    String dropModuleDocumentById(String id);

    String saveProcessDocumentText(PmsProcessDocumentText pmsProcessDocumentText);

    PmsProcessDocumentText getProcessDocumentText(PmsProcessDocumentText pmsProcessDocumentText);


    void saveDesignJson(MmsModuleDesignJson mmsModuleDesignJson);

    MmsModuleDesignJson getMduleDocumentDesignInfo(MmsModuleDesignJson mmsModuleDesignJson);

}
