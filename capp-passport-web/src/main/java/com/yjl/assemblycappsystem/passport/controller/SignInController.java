package com.yjl.assemblycappsystem.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;

import com.yjl.assemblycappsystem.annotations.LoginRequired;
import com.yjl.assemblycappsystem.bean.UmsUserAddinfo;
import com.yjl.assemblycappsystem.bean.UmsUserInfo;
import com.yjl.assemblycappsystem.commons.WebConstans;
import com.yjl.assemblycappsystem.service.UserService;
import com.yjl.assemblycappsystem.util.SlideVerificationCodeUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("signIn")
public class SignInController {

    @Reference
    UserService userService;

    @RequestMapping("index")
    @LoginRequired
    public String index(){
        return "signIn";
    }


    /**
     * 校验umsUserInfo的用户名是否重复
     * redis存储所有当前的username
     * @param umsUserInfo
     * @return
     */
    @RequestMapping("checkUsername")
    @LoginRequired
    @ResponseBody
    public String checkUsername(@RequestBody UmsUserInfo umsUserInfo){
        String returnStr = "fail";
        if (umsUserInfo == null || StringUtils.isBlank(umsUserInfo.getUsername())){
            return returnStr;
        }
        //交给userService查看是否这个username已经使用
        returnStr = userService.checkUsername(umsUserInfo);
        return returnStr;
    }


    /**
     * 校验umsUserInfo的工号是否重复
     * @param umsUserInfo
     * @return
     */
    @RequestMapping("checkEmployeeId")
    @LoginRequired
    @ResponseBody
    public String checkEmployeeId(@RequestBody UmsUserInfo umsUserInfo){
        String returnStr = "fail";
        if (umsUserInfo == null || StringUtils.isBlank(umsUserInfo.getEmployeeId())){
            return "fail";
        }
        returnStr = userService.checkEmployeeId(umsUserInfo);
        return returnStr;
    }


    /**
     * 注册，将用户信息写入ums_user_info
     * 在ums_user_addinfo表中新建一行，数据全为空，等待后期填入
     * @param umsUserInfo
     * @return
     * @throws UnsupportedEncodingException
     */
    @RequestMapping("signIn")
    @LoginRequired
    @ResponseBody
    public String signIn(@RequestBody UmsUserInfo umsUserInfo) throws UnsupportedEncodingException {

        //进行表单数据的校验
        //只校验用户名和工号,手机和邮箱后期会通过短信和邮件方式校验
        String username = umsUserInfo.getUsername();
        String employeeId = umsUserInfo.getEmployeeId();
        if (username.length()<6 || username.length()>14 || employeeId.length() != 7){
            return "fail";
        }
        String checkUsername = userService.checkUsername(umsUserInfo);
        String checkEmployeeId = userService.checkEmployeeId(umsUserInfo);

        if (checkUsername.equals("fail")|| checkEmployeeId.equals("fail")){
            return "fail";
        }

        //校验成功，将用户信息存入数据库
        Integer saveUserId = -1;

        //将密码哈希加密,采用sha256+salt
        //随机salt盐值
        byte[] saltbytes = new byte[32];
        SecureRandom random = new SecureRandom();
        random.nextBytes(saltbytes);
        String saltString = org.apache.commons.codec.binary.Base64.encodeBase64String(saltbytes);
        umsUserInfo.setSalt(saltString);

        //采用sha256加密
        String password = umsUserInfo.getPassword()+saltString;
        String encodingPassword= DigestUtils.sha256Hex(password);
        umsUserInfo.setPassword(encodingPassword);

        //保存
        saveUserId = userService.addUser(umsUserInfo);

        if (saveUserId != -1){
            umsUserInfo.setId(saveUserId);
            UmsUserAddinfo umsUserAddinfo = new UmsUserAddinfo();
            umsUserAddinfo.setUserId(saveUserId);
            UmsUserAddinfo umsUserAddinfo1 = userService.addUserAddinfo(umsUserAddinfo);
            if (umsUserAddinfo1 == null ){
                return "fail";
            }
            else {
                //头像新增
                userService.addHeadPortraitUrl(umsUserInfo,WebConstans.defaultHeadPortraitsUrl);
                //redis的username和employeeId新增
                String success = userService.addUsernameAndEmployeeId(umsUserInfo);

                return success;
            }
        }else {
            return "fail";
        }
    }


    /**
     * getVerification方法生成滑块验证码发送给前端
     * @return 返回的map中有处理过的原图的字节数组和切出图的字节数组
     * 由于返回值是
     * @throws Exception
     */
    @RequestMapping("getVerification")
    @LoginRequired
    @ResponseBody
    public Map<String,String> getVerification() throws Exception {

        //随机一个X的值
        int X = (int) Math.floor(20 + Math.random()*(SlideVerificationCodeUtil.ORI_HEIGHT-SlideVerificationCodeUtil.TARGRTWIDTH - 20));;

        //随机一个Y的值
        int Y = (int) Math.floor(20 + Math.random()*(SlideVerificationCodeUtil.ORI_WIDTH-SlideVerificationCodeUtil.TARGRTHEIGHT - 20));;

        Map<String, byte[]> slideVerificationImgMap = SlideVerificationCodeUtil.createSlideVerificationImg(X,Y);

        //获取处理后的原图的字节数组和切出图的字节数组
        byte[] oriCopyImageBytes = slideVerificationImgMap.get("oriCopyImage");
        byte[] newImageBytes = slideVerificationImgMap.get("newImage");


        //进行Base64编码转换
        String oriCopyImage = Base64Utils.encodeToString(oriCopyImageBytes);
        String newImage = Base64Utils.encodeToString(newImageBytes);

        Map<String,String> resultMap=new HashMap<>();
        resultMap.put("newImage", newImage);
        resultMap.put("oriCopyImage", oriCopyImage);
        resultMap.put("Y", Y+"");
        return resultMap;
    }
}
