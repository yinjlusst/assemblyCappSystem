package com.yjl.assemblycappsystem.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.yjl.assemblycappsystem.bean.UmsUserAddinfo;
import com.yjl.assemblycappsystem.bean.UmsUserHeadPortraitUrl;
import com.yjl.assemblycappsystem.bean.UmsUserInfo;
import com.yjl.assemblycappsystem.service.UserService;
import com.yjl.assemblycappsystem.util.FastdfsUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("userAddInfo")
public class UserAddInfoController {

    @Reference
    UserService userService;

    /**
     * 修改用户附加信息页面加载
     * @return
     */
    @RequestMapping("index")
    public String index(){
        return "userAddInfo";
    }
    /**
     * 获取用户附加信息
     * @param request
     * @return
     */
    @RequestMapping("getUserAddinfo")
    @ResponseBody
    public String getUserAddinfo(HttpServletRequest request){
        String username = (String) request.getAttribute("username");
        String idStr = (String) request.getAttribute("id");
        UmsUserAddinfo umsUserAddinfo = userService.getUserAddinfoByUserId(Integer.parseInt(idStr));

        if (umsUserAddinfo == null){
            umsUserAddinfo = new UmsUserAddinfo();
        }
        Map<String,String> retMap = new HashMap();
        retMap.put("qq",umsUserAddinfo.getQQ());
        retMap.put("weibo",umsUserAddinfo.getWeibo());
        retMap.put("introduce",umsUserAddinfo.getIntroduce());
        String retJson = JSON.toJSONString(retMap);


        return retJson;
    }


    @ResponseBody
    @RequestMapping("saveHeadPortraits")
    public String saveHeadPortraits(MultipartFile fileUpload){
        if (fileUpload == null){
            return "fail";
        }
        //将文件(图片视频...)上传到分布式的文件存储系统(保存图片的对象数据)
        String imgUrl = FastdfsUtil.uploadDocument(fileUpload);
        if (imgUrl == null){
            return "fail";
        }
        return imgUrl;
    }


    /**
     * 用户在修改用户附加信息页面上传的数据
     * 需要将这些数据存储进数据库
     * 数据由头像url和附加信息组成
     * @param umsUserAddinfo
     * @return 成功返回success,失败返回fail
     */
    @ResponseBody
    @RequestMapping("renewUserInfo")
    public String renewUserInfo(@RequestBody UmsUserAddinfo umsUserAddinfo, HttpServletRequest request){
        String username = (String) request.getAttribute("username");
        String idStr = (String) request.getAttribute("id");

        Integer uid = Integer.parseInt(idStr);
        umsUserAddinfo.setUserId(uid);

        //封装头像bean
        UmsUserHeadPortraitUrl umsUserHeadPortraitUrl = new UmsUserHeadPortraitUrl();
        umsUserHeadPortraitUrl.setUserId(uid);
        umsUserHeadPortraitUrl.setHeadPortraitUrl(umsUserAddinfo.getHeadPortraitsUrl());


        //检测数据是否有效
        if (umsUserAddinfo.getIntroduce().length()>=250 && umsUserAddinfo.getQQ().length()>=15 && umsUserAddinfo.getWeibo().length()>=25){
            return "fail";
        }

        if (StringUtils.isNotBlank(umsUserHeadPortraitUrl.getHeadPortraitUrl()) && umsUserHeadPortraitUrl.getHeadPortraitUrl().length()>3){
            //只有用户更新了头像才有必要更新
            UmsUserHeadPortraitUrl headPortraitUrl = new UmsUserHeadPortraitUrl();
            headPortraitUrl.setUserId(uid);
            headPortraitUrl.setHeadPortraitUrl(umsUserHeadPortraitUrl.getHeadPortraitUrl());
            UmsUserHeadPortraitUrl umsUserHeadPortraitUrl1 = userService.renewHeadPortraitsUrl(headPortraitUrl);
            if ( umsUserHeadPortraitUrl1  == null){
                //头像更新失败
            }
        }



        //更新附加信息
        UmsUserAddinfo umsUserAddinfo1 = userService.renewUserAddinfo(umsUserAddinfo);
        if (umsUserAddinfo1 == null){
            return "fail";
        }

        return "success";

    }
}