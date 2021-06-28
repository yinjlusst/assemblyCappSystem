package com.yjl.assemblycappsystem.manage.mapper;

import com.yjl.assemblycappsystem.bean.DmsProcessDocument;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface DmsProcessDocumentMapper extends Mapper<DmsProcessDocument> {

    List<DmsProcessDocument> selectAllProcessDocuments(@Param("start")Integer start, @Param("end")Integer end);

}