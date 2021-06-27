package com.yjl.assemblycappsystem.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;

import com.yjl.assemblycappsystem.annotations.LoginRequired;
import com.yjl.assemblycappsystem.bean.UmsUserInfo;
import com.yjl.assemblycappsystem.commons.PassportConstants;
import com.yjl.assemblycappsystem.service.UserService;
import com.yjl.assemblycappsystem.util.JwtUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("passport")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PassportController {

    @Reference
    UserService userService;

    /**
     * login方法：在认证中心点击登录按钮
     * <p>
     * 功能：该方法传入登录页用户输入的用户名和密码
     * 调用service层验证用户名和密码
     * 如果用户名密码正确，制作token并返回
     *
     * @param umsUserInfo
     * @return
     */
    @RequestMapping("login")
    @ResponseBody
    @LoginRequired
    public String login(@RequestBody UmsUserInfo umsUserInfo, HttpServletRequest request) {
        String token = "fail";

        //将用户输入的密码加密
        //通过查找用户名找到该用户名的盐值salt
        UmsUserInfo userInfo1 = new UmsUserInfo();
        userInfo1.setUsername(umsUserInfo.getUsername());
        UmsUserInfo userByUsername = userService.getUser(userInfo1);
        if (userByUsername.getId()== -1){
            //没找到这个用户名
            return token;
        }
        String saltString = userByUsername.getSalt();

        //将用户输入的密码加密
        String password = umsUserInfo.getPassword()+saltString;
        String encodingPassword= DigestUtils.sha256Hex(password);
        umsUserInfo.setPassword(encodingPassword);



        //调用服务service验证用户名和密码
        UmsUserInfo umsUserInfoAfterCheck = userService.checkUserNamePassword(umsUserInfo);
        if (umsUserInfoAfterCheck != null) {
            String username = umsUserInfoAfterCheck.getUsername();
            String employeeId = umsUserInfoAfterCheck.getEmployeeId();
            String idStr = umsUserInfoAfterCheck.getId()+"";

            //用户名密码输入正确
            //使用jwt制作token

            //jwt由公共部分+私有部分+签名部分组成

            //jwt公共部分,这里就以项目名字作为公共部分
            String serverKey = PassportConstants.serverkey;

            //jwt私有部分,由用户名和工号组成
            HashMap<String, Object> userMap = new HashMap<>();
            userMap.put("username", username);
            userMap.put("employeeId", employeeId);
            userMap.put("id",idStr);


            //jwt签名部分，这里就以用户的ip作为签名部分
            String ip = request.getHeader("x-forwarded-for");//通过nginx转发的客户端ip
            if (StringUtils.isBlank(ip)) {
                ip = request.getRemoteAddr();//从request中获取ip
            }
            if (StringUtils.isBlank(ip)) {
                //请求出现问题或者为非法请求
                //直接return
                return "redirect:/error";
            }
            if (ip.equals("0:0:0:0:0:0:0:1")){
                ip = "127.0.0.1";
            }

            //制作token按照设计的算法对参数进行加密后，生成token(现在没有加密，都是明文)
            token = JwtUtil.encode(serverKey, userMap, ip);

            //将token存入redis一份（暂时没有意义）
            //passportService.addUserToken(token,employeeId);


            //创建消息队列，发送邮件，短信，更新数据库lastLoginTime
            userService.renewLoginStatus(idStr);

        }
        return token;
    }


    /**
     * index方法：访问passport的首页
     * <p>
     * 直接访问的passport:没有携带returnUrl,加入一个returnUrl保存到首页
     * 通过访问别的功能被拦截从而访问的passport:携带returnUrl
     * <p>
     * 功能：跳转页面，并将returnUrl给前端
     *
     * @param returnUrl
     * @param map
     * @return
     */
    @RequestMapping("index")
    @LoginRequired
    public String index(String returnUrl, ModelMap map) {
        if (StringUtils.isBlank(returnUrl)) {
            returnUrl = "http://manage.capp.com:7001/manage/index";

        }
        map.put("returnUrl", returnUrl);
        return "index";
    }


    /**
     * verify方法：用户每次访问要身份的页面被拦截
     * <p>
     * 功能：验证token是否有效
     * 1.如果有效返回信息，status为success(JSON字符串)
     * 2.如果无效返回的status为fail(JSON字符串)
     *
     * @param token
     * @param ip
     * @return
     */
    @RequestMapping("verify")
    @LoginRequired
    @ResponseBody
    public String verify(String token, String ip) {
        HashMap<String, Object> userMap = new HashMap<>();
        String returnJSON = "";

        //参数空值判断
        if (StringUtils.isBlank(ip)) {
            return "redirect:/error";
        }

        if (StringUtils.isBlank(token)) {
            userMap.put("status", "fail");
            returnJSON = JSON.toJSONString(userMap);
            return returnJSON;
        }

        //验证token是否有效
        Map<String, Object> decode = JwtUtil.decode(token, PassportConstants.serverkey, ip);

        if (decode != null) {
            userMap.put("employeeId", decode.get("employeeId"));
            userMap.put("username", decode.get("username"));
            userMap.put("id",decode.get("id"));
            userMap.put("status", "success");


            returnJSON = JSON.toJSONString(userMap);
        }
        else {
            userMap.put("status", "fail");
            returnJSON = JSON.toJSONString(userMap);
            return returnJSON;
        }
        return returnJSON;
    }

    /**
     * 非法请求等报错
     *
     * @return
     */
    @RequestMapping("error")
    @LoginRequired
    public String error() {
        return "error";
    }

}
