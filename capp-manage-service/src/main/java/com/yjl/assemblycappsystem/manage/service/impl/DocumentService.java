package com.yjl.assemblycappsystem.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.yjl.assemblycappsystem.util.ActiveMQUtil;
import com.yjl.assemblycappsystem.util.RedisUtil;
import com.yjl.assemblycappsystem.bean.*;
import com.yjl.assemblycappsystem.manage.mapper.DmsModuleDocumentMapper;
import com.yjl.assemblycappsystem.manage.mapper.DmsProcessDocumentMapper;
import com.yjl.assemblycappsystem.manage.mapper.MmsModuleDesignJsonMapper;
import com.yjl.assemblycappsystem.manage.mapper.PmsProcessDocumentTextMapper;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Session;
import java.util.List;

@Service
public class DocumentService implements com.yjl.assemblycappsystem.service.DocumentService {

    @Autowired
    DmsProcessDocumentMapper dmsProcessDocumentMapper;
    @Autowired
    DmsModuleDocumentMapper dmsModuleDocumentMapper;
    @Autowired
    ActiveMQUtil activeMQUtil;
    @Autowired
    ConnectionFactory connectionFactory;
    @Autowired
    PmsProcessDocumentTextMapper pmsProcessDocumentTextMapper;
    @Autowired
    MmsModuleDesignJsonMapper mmsModuleDesignJsonMapper;


    @Autowired
    RedisUtil redisUtil;


    /**
     * 从数据库中查询dms_process_document中的所有数据
     * @param page
     * @param limit
     * @return
     */
    @Override
    public List<DmsProcessDocument> getAllProcessDocuments(String page, String limit) {
        //从mysql中查询数据

        //采用配置文件配置mybatis一对一，使用resultMap属性
        // 这里为了训练mybatis一对一的写法特意写的，其实完全不需要
        Integer start = (Integer.parseInt(page)-1)*Integer.parseInt(limit);
        Integer end = Integer.parseInt(limit);
        List<DmsProcessDocument> dmsProcessDocuments = dmsProcessDocumentMapper.selectAllProcessDocuments(start,end);

        for (DmsProcessDocument dmsProcessDocument : dmsProcessDocuments) {
            dmsProcessDocument.setUploadUsername(dmsProcessDocument.getUmsUserInfo().getUsername());
            dmsProcessDocument.setUploadUserEmail(dmsProcessDocument.getUmsUserInfo().getEmail());
            dmsProcessDocument.setUmsUserInfo(new UmsUserInfo());
        }
        return dmsProcessDocuments;
    }

    /**
     * 从数据库中查询dms_module_document中的所有数据
     * @param page
     * @param limit
     * @return
     */
    @Override
    public List<DmsModuleDocument> getAllModuleDocuments(String page, String limit) {
        //从mysql中查询数据
        //这里使用的是resultType属性
        List<DmsModuleDocument> dmsModuleDocuments = dmsModuleDocumentMapper.selectAllDmsModuleDocuments();
        return dmsModuleDocuments;
    }

    /**
     * 向数据库中保存已经上传了的模型文件数据
     * @param dmsModuleDocument
     * @return
     */
    @Override
    public String saveModuleDocument(DmsModuleDocument dmsModuleDocument)  {
        //先向mysql数据库中存入数据

        int i = dmsModuleDocumentMapper.insertSelective(dmsModuleDocument);
        if (i>0){
            //新建消息队列解析模型文件的特征树
            Connection connection = null;
            Session session = null;
            try {
                //创建session和connection
                //1.建立连接
                connection = connectionFactory.createConnection();
                connection.start();
                session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
                activeMQUtil.sendText(session,dmsModuleDocument.getUrl() + "&" + dmsModuleDocument.getId(),"PARSE_MODULE_DOCUMENT_QUEUE");
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    //消息回旋
                    session.rollback();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } finally {
                try {
                    connection.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return "success";
        }
        return "fail";
    }

    /**
     * 向数据库中保存上传了工艺的文件的数据
     * @param dmsProcessDocument
     * @return
     */
    @Override
    public String saveDocument(DmsProcessDocument dmsProcessDocument) {
        //先向mysql数据库中存入数据
        int i = dmsProcessDocumentMapper.insertSelective(dmsProcessDocument);
        if (i>0){
            //是否是txt文件
            String extName = "";
            int index = dmsProcessDocument.getUrl().lastIndexOf(".");

            if (index > 0 && index < dmsProcessDocument.getUrl().length() - 1) {
                extName = dmsProcessDocument.getUrl().substring(index + 1);
            }

            if (extName.equals("txt")){
                Connection connection = null;
                Session session = null;
                try {
                    //创建session和connection
                    //1.建立连接
                    connection = connectionFactory.createConnection();
                    connection.start();
                    session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
                    activeMQUtil.sendText(session, dmsProcessDocument.getUrl()+ "&" +dmsProcessDocument.getId(),"PARSE_PROCESS_DOCUMENT_QUEUE");

                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        //消息回旋
                        session.rollback();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        return "fail";
                    }
                } finally {
                    try {
                        connection.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return "fail";
                    }
                }
            }
            return "success";
        }
        return "fail";
    }

    /**
     * 从数据库删除一条文档记录
     * 这里先进行这样的删除，数据库里的东西不能随便删，应该做一个记号，数据库运维来统一删
     * @param id
     * @return
     */
    @Override
    public String dropDocumentById(String id) {
        DmsProcessDocument dmsProcessDocument = new DmsProcessDocument();
        dmsProcessDocument.setId(Integer.parseInt(id));
        int delete = dmsProcessDocumentMapper.delete(dmsProcessDocument);
        if (delete>0){
            return "success";
        }
        return "false";
    }

    @Override
    public String dropModuleDocumentById(String id) {
        DmsModuleDocument dmsModuleDocument = new DmsModuleDocument();
        dmsModuleDocument.setId(Integer.parseInt(id));
        int delete = dmsModuleDocumentMapper.delete(dmsModuleDocument);
        if (delete>0){
            return "success";
        }
        return "false";
    }

    @Override
    public String saveProcessDocumentText(PmsProcessDocumentText pmsProcessDocumentText) {
        int i = pmsProcessDocumentTextMapper.insertSelective(pmsProcessDocumentText);
        if (i>0){
            return "success";
        }
        return "fail";
    }

    /**
     * 从pms_process_document_text读取对应数据
     */
    @Override
    public PmsProcessDocumentText getProcessDocumentText(PmsProcessDocumentText pmsProcessDocumentText) {
        PmsProcessDocumentText pmsProcessDocumentText1 = pmsProcessDocumentTextMapper.selectOne(pmsProcessDocumentText);
        if (pmsProcessDocumentText1 == null){
            return null;
        }
        return pmsProcessDocumentText1;
    }

    /**
     * 从mms_module_design_json中读取设计数据
     * @param mmsModuleDesignJson
     * @return
     */
    @Override
    public MmsModuleDesignJson getMduleDocumentDesignInfo(MmsModuleDesignJson mmsModuleDesignJson) {
        MmsModuleDesignJson mmsModuleDesignJson1 = mmsModuleDesignJsonMapper.selectOne(mmsModuleDesignJson);
        if (mmsModuleDesignJson1 == null){
            return null;
        }
        return mmsModuleDesignJson1;
    }


    /**
     * 将模型文件中模型特征树的json信息直接以字符串的形式保存在数据库
     */
    @Override
    public void saveDesignJson(MmsModuleDesignJson mmsModuleDesignJson) {
        int i = mmsModuleDesignJsonMapper.insertSelective(mmsModuleDesignJson);
        System.out.println(i);
    }

}
