package com.yjl.assemblycappsystem.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yjl.assemblycappsystem.bean.UmsUserInfo;
import com.yjl.assemblycappsystem.bean.UmsUserSearchInfo;
import com.yjl.assemblycappsystem.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@RequestMapping("userList")
@Controller
public class UserListController {
    @Reference
    UserService userService;

    @RequestMapping("index")
    public String basicInfoIndex(){
        return "userList";
    }

    /**
     * 返回所有的用户的列表
     * @return
     */
    @RequestMapping("getUserList")
    @ResponseBody
    public List<UmsUserSearchInfo> getUserList(Integer currentPage,Integer rowInOnePage){
        if (rowInOnePage != 10){
            return null;
        }
        List<UmsUserInfo> allUser = userService.getAllUser();
        if (allUser.size() <= (currentPage-1) * (rowInOnePage)+1){
            return null;
        }
        //从elasticSearch查询数据库用户列表
        List<UmsUserSearchInfo> userList = userService.getAllUserFromSearchDB(new UmsUserSearchInfo(),currentPage,rowInOnePage);
        return userList;

    }

    /**
     * 按照用户搜索的信息（用户名或者工号）按照逗号分割的，查找符合条件的用户的信息
     * @param searchInfo
     * @return
     */
    @RequestMapping("getSearchUsers")
    @ResponseBody
    public List<UmsUserSearchInfo> getSearchUsers(String searchInfo,Integer currentPage,Integer rowInOnePage){
        if (StringUtils.isBlank(searchInfo)){
            return null;
        }

        //传入参数的处理
        String[] searchParams = searchInfo.split(",");
        if (searchParams.length==0){
            searchParams = new String[]{searchInfo.strip()};
        }

        List<UmsUserSearchInfo> umsUserSearchInfos = new ArrayList<>();

        for (String searchParam : searchParams) {
            //按照用户名匹配查找
            UmsUserSearchInfo umsUserSearchInfo = new UmsUserSearchInfo();
            umsUserSearchInfo.setUsername(searchParam.strip());
            List<UmsUserSearchInfo> searchUserByUsername = userService.getUserByFuzzyUsername(umsUserSearchInfo);
            if (searchUserByUsername!= null){
                umsUserSearchInfos.addAll(searchUserByUsername);
            }

            //按照工号匹配查找
            if (searchParam.matches("^-?[0-9]+")){
                umsUserSearchInfo.setUsername("");
                umsUserSearchInfo.setEmployeeId(searchParam.strip());
                List<UmsUserSearchInfo> searchUserByEmployeeId = userService.getUserByFuzzyEmployeeId(umsUserSearchInfo);
                if (searchUserByEmployeeId != null){

                    Iterator<UmsUserSearchInfo> iterator = searchUserByEmployeeId.iterator();
                    while (iterator.hasNext()){
                        UmsUserSearchInfo next = iterator.next();

                        for (UmsUserSearchInfo userSearchInfo : umsUserSearchInfos) {
                            if (userSearchInfo.getUsername().equals(next.getUsername())){
                                iterator.remove();
                            }
                        }
                    }
                    umsUserSearchInfos.addAll(searchUserByEmployeeId);
                }
            }
        }

        if (umsUserSearchInfos.size()!=0){
            return umsUserSearchInfos;
        }else {
            return null;
        }
    }
}
