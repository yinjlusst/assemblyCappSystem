package com.yjl.assemblycappsystem.manage.mapper;

import com.yjl.assemblycappsystem.bean.DmsModuleDocument;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface DmsModuleDocumentMapper extends Mapper<DmsModuleDocument> {
    List<DmsModuleDocument> selectAllDmsModuleDocuments();
}
