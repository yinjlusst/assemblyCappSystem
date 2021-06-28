package com.yjl.assemblycappsystem.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yjl.assemblycappsystem.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("userManage")
public class UserManageController {
    @Reference
    UserService userService;


    /**
     * 获取用户数量
     */
    @RequestMapping("getUserNum")
    @ResponseBody
    public Integer getUserNum(){
        return 13;
    }
}