package com.yjl.assemblycappsystem.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;


import com.yjl.assemblycappsystem.commons.WebConstans;
import com.yjl.assemblycappsystem.service.UserService;
import com.yjl.assemblycappsystem.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("manage")
public class ManageController {

    @Reference
    UserService userService;

    @RequestMapping("index")
    public String index(){
        return "index";
    }

    /**
     * 查询用户基本信息(头像和用户名)
     * @param request
     * @return
     */
    @RequestMapping("getUserInfo")
    @ResponseBody
    public String getUserInfo(HttpServletRequest request){
        String username = (String) request.getAttribute("username");
        String idStr = (String) request.getAttribute("id");
        String headPortraitsUrl = userService.getHeadPortraitsUrlById(Integer.parseInt(idStr), WebConstans.defaultHeadPortraitsUrl);
        if (StringUtils.isBlank(headPortraitsUrl)){
            headPortraitsUrl = WebConstans.defaultHeadPortraitsUrl;
        }
        Map<String,String> retMap = new HashMap();
        retMap.put("username",username);
        retMap.put("headPortraitsUrl",headPortraitsUrl);

        String retJson = JSON.toJSONString(retMap);

        return retJson;
    }

    /**
     * 注销用户，删除cookie
     * @return
     */
    @RequestMapping("logout")
    public String logout(HttpServletRequest request, HttpServletResponse response){

        CookieUtil.deleteCookie(request,response,"oldToken");
        return "redirect:http://passport.capp.com:7000/passport/index";
    }


}
