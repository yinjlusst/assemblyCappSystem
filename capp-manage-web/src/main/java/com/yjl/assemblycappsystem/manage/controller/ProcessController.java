package com.yjl.assemblycappsystem.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yjl.assemblycappsystem.annotations.LoginRequired;
import com.yjl.assemblycappsystem.bean.DmsModuleDocument;
import com.yjl.assemblycappsystem.bean.DmsProcessDocument;
import com.yjl.assemblycappsystem.bean.MmsModuleDesignJson;
import com.yjl.assemblycappsystem.bean.PmsProcessDocumentText;
import com.yjl.assemblycappsystem.service.DocumentService;
import com.yjl.assemblycappsystem.util.FastdfsUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("processManage")
public class ProcessController {
    @Reference
    DocumentService documentService;

    @RequestMapping("moduleDocumentIndex")
    public String moduleDocumentIndex(){
        return "moduleDocument";
    }

    @RequestMapping("processDocumentIndex")
    public String processDocumentIndex(){
        return "processDocument";
    }

    /**
     * 返回文件列表
     * @return
     */
    @RequestMapping("getProcessDocumentList")
    @ResponseBody
    public Map<String,Object> getProcessDocumentList(String page, String limit){
        if (StringUtils.isBlank(page) || StringUtils.isBlank(limit) || Integer.parseInt(limit) != 10){
            return null;
        }
        //查询数据库中的文件信息
        List<DmsProcessDocument> dmsProcessDocuments = documentService.getAllProcessDocuments(page,limit);
        if (dmsProcessDocuments!= null){
            //将拿到的数据封装成JSON数据
            Map<String,Object> returnMap = new HashMap<>();
            returnMap.put("code",0);
            returnMap.put("msg","");
            returnMap.put("count",dmsProcessDocuments.size());
            returnMap.put("data",dmsProcessDocuments);

            return returnMap;
        }
        return null;
    }

    /**
     * 返回模型文件列表
     */
    @RequestMapping("getModuleDocumentList")
    @ResponseBody
    public Map<String,Object> getModuleDocumentList(String page,String limit){
        if (StringUtils.isBlank(page) || StringUtils.isBlank(limit) || Integer.parseInt(limit) != 10){
            return null;
        }

        //查询数据库中的文件信息
        List<DmsModuleDocument> dmsModuleDocuments = documentService.getAllModuleDocuments(page,limit);
        if (dmsModuleDocuments!= null){
            //将拿到的数据封装成JSON数据
            Map<String,Object> returnMap = new HashMap<>();
            returnMap.put("code",0);
            returnMap.put("msg","");
            returnMap.put("count",dmsModuleDocuments.size());
            returnMap.put("data",dmsModuleDocuments);
            return returnMap;
        }
        return null;

    }

    /**
     * 查找文件的文本
     */
    @RequestMapping("getProcessDocumentText")
    @ResponseBody
    public String getProcessDocumentText(String id){
        if (StringUtils.isBlank(id)){
            return "输入错误";
        }
        Integer documentId = Integer.parseInt(id);
        PmsProcessDocumentText pmsProcessDocumentText = new PmsProcessDocumentText();
        pmsProcessDocumentText.setDocumentId(documentId);
        PmsProcessDocumentText processDocumentText = documentService.getProcessDocumentText(pmsProcessDocumentText);
        if (processDocumentText != null){
            return processDocumentText.getText();
        }

        return "当前文件不是文本文件";
    }

    /**
     * 查找模型文件中的设计数据
     */
    @RequestMapping("getMduleDocumentDesignInfo")
    @ResponseBody
    public String getMduleDocumentDesignInfo(String id){
        if (StringUtils.isBlank(id)){
            return "输入错误";
        }
        Integer documentId = Integer.parseInt(id);
        MmsModuleDesignJson mmsModuleDesignJson = new MmsModuleDesignJson();
        mmsModuleDesignJson.setDocumentId(documentId);
        MmsModuleDesignJson moduleDesignJson = documentService.getMduleDocumentDesignInfo(mmsModuleDesignJson);
        if (moduleDesignJson != null){
            return moduleDesignJson.getDesignJson();
        }

        return "当前文件不是正确的模型文件";


    }



    /**
     * 上传工艺文件
     */
    @RequestMapping("uploadDocuments")
    @ResponseBody
    public Map<String,Object> uploadDocuments(MultipartFile file, HttpServletRequest request){

        String username = "yinjianliang";
        Integer userId = 8;
        //将上传的文件保存到分布式文件存储系统

        DmsProcessDocument dmsProcessDocument = new DmsProcessDocument();

        String url = FastdfsUtil.uploadDocument(file);
        if (StringUtils.isBlank(url)){
            return new HashMap<String,Object>();
        }
        dmsProcessDocument.setUrl(url);
        dmsProcessDocument.setSize(String.valueOf(file.getSize()));
        dmsProcessDocument.setDocumentName(file.getOriginalFilename());
        dmsProcessDocument.setUpdateTimes("0");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String nowDate = format.format(Calendar.getInstance().getTime());
        dmsProcessDocument.setUploadDate(nowDate);
        dmsProcessDocument.setUploadUserId(userId);
        //存储进数据库
        String success = documentService.saveDocument(dmsProcessDocument);
        if (!success.equals("fail")){
            //上传成功了返回JSON数据
            Map<String,Object> returnMap = new HashMap<>();
            returnMap.put("code",0);
            returnMap.put("msg","");
            HashMap<String, String> src = new HashMap<>();
            src.put("src",url);
            returnMap.put("data",src);
            return returnMap;
        }

        //可能需要删除分布式文件存储系统上的数据（回滚）
        return new HashMap<String,Object>();
    }


    /**
     * 上传模型文件
     */
    @RequestMapping("uploadModuleDocuments")
    @ResponseBody
    public Map<String,Object> uploadModuleDocuments(MultipartFile file){
        String username = "yinjianliang";
        Integer userId = 8;
        //将上传的文件保存到分布式文件存储系统
        DmsModuleDocument dmsModuleDocument = new DmsModuleDocument();

        String url = FastdfsUtil.uploadDocument(file);
        if (StringUtils.isBlank(url)){
            return new HashMap<String,Object>();
        }
        dmsModuleDocument.setUrl(url);
        dmsModuleDocument.setSize(String.valueOf(file.getSize()));
        dmsModuleDocument.setDocumentName(file.getOriginalFilename());
        dmsModuleDocument.setUpdateTimes("0");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String nowDate = format.format(Calendar.getInstance().getTime());
        dmsModuleDocument.setUploadDate(nowDate);
        dmsModuleDocument.setUploadUserId(userId);
        //存储进数据库
        String success = documentService.saveModuleDocument(dmsModuleDocument);
        if (!success.equals("fail")){
            //上传成功了返回JSON数据
            Map<String,Object> returnMap = new HashMap<>();
            returnMap.put("code",0);
            returnMap.put("msg","");
            HashMap<String, String> src = new HashMap<>();
            src.put("src",url);
            returnMap.put("data",src);
            return returnMap;
        }
        //可能需要删除分布式文件存储系统上的数据（回滚）
        return new HashMap<String,Object>();
    }


    /**
     * 删除工艺文件
     * @param url
     * @return
     */
    @RequestMapping("deleteDocument")
    @ResponseBody
    public String deleteDocument(String url,String id){

        if (StringUtils.isBlank(url)){
            return "fail";
        }
        String success = delDocumentFromFastdfsbyUrl(url);

        if (success.equals("success")){
            //删除成功了，将数据库中的数据也删除
            String dropSuccess = documentService.dropDocumentById(id);
            if (dropSuccess.equals("success")){
                return "success";
            }
        }
        return "fail";
    }

    /**
     *下载文件
     * 返回的是 将文件保存到服务器之后的文件地址
     */
    @RequestMapping("downloadDocument")
    @ResponseBody
    @LoginRequired
    public String downloadDocument(String url, HttpServletRequest request) throws IOException {
        String[] path = url.split("/");
        //获取groupName和remoteFilename
        String groupName = path[3];
        String remoteFilename = "";
        for (int i = 4; i < path.length; i++) {
            remoteFilename += path[i];
            if (i != path.length - 1) {
                remoteFilename += "/";
            }
        }
        //创建输出流

        //获取服务器真实路径
        String realPath = request.getSession().getServletContext().getRealPath("/tmp/");
        //判断该路径是否存在
        File file = new File(realPath);
        if(!file.exists()){
            //创建文件
            file.mkdir();
            realPath += "a.txt";
            File file1 = new File(realPath);
            file1.createNewFile();
        }else{
            file.delete();
            //创建文件
            file.mkdir();
            realPath += "a.txt";
            File file1 = new File(realPath);
            file1.createNewFile();
        }
        //下载文件
        OutputStream outputStream = null;

        outputStream = new FileOutputStream(realPath);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
        int result = FastdfsUtil.downloadDocument(groupName, remoteFilename, bufferedOutputStream);
        if (result == -1 ) {
            return "fail";
        }
        return realPath;
    }

    /**
     * 删除模型文件
     * @param url
     * @return
     */
    @RequestMapping("deleteModuleDocument")
    @ResponseBody
    public String deleteModuleDocument(String url,String id){
        if (StringUtils.isBlank(url)){
            return "fail";
        }
        String success = delDocumentFromFastdfsbyUrl(url);

        if (success.equals("success")){
            //删除成功了，将数据库中的数据也删除
            String dropSuccess = documentService.dropModuleDocumentById(id);
            if (dropSuccess.equals("success")){
                return "success";
            }
        }
        return "fail";
    }

    public String delDocumentFromFastdfsbyUrl(String url){
        String[] path = url.split("/");
        String groupName = path[3];
        String remoteFilename = "";
        for (int i = 4; i < path.length; i++) {
            remoteFilename += path[i];
            if (i != path.length - 1) {
                remoteFilename += "/";
            }
        }
        String success = FastdfsUtil.deleteDocument(groupName, remoteFilename);
        return success;
    }

}
