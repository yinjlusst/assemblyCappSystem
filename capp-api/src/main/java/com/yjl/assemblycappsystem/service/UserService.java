package com.yjl.assemblycappsystem.service;



import com.yjl.assemblycappsystem.bean.UmsUserAddinfo;
import com.yjl.assemblycappsystem.bean.UmsUserInfo;
import com.yjl.assemblycappsystem.bean.UmsUserSearchInfo;

import java.util.List;

public interface UserService {
    UmsUserInfo checkUserNamePassword(UmsUserInfo umsUserInfo);

    void addUserToken(String token, String employeeNumber);

    UmsUserInfo getUser(UmsUserInfo umsUserInfo);


    Integer addUser(UmsUserInfo umsUserInfo);

    String getHeadPortraitsUrlById(Integer Id, String defaultHeadPortraitsUrl);

    Integer addUserAddinfo(UmsUserAddinfo umsUserAddinfo);

    UmsUserAddinfo getUserAddinfoByUserId(int userId);

    Integer renewHeadPortraitsUrl(UmsUserInfo umsUserInfo);

    Integer renewUserAddinfo(UmsUserAddinfo umsUserAddinfo);

    void renewUserLastLoginTime(UmsUserInfo umsUserInfo);

    void renewLoginStatus(String idStr);

    List<UmsUserInfo> getAllUser();

    List<UmsUserSearchInfo> getAllUserFromSearchDB(UmsUserSearchInfo umsUserSearchInfo, Integer page, Integer rowInOnePage);

    List<UmsUserSearchInfo> getUserByFuzzyUsername(UmsUserSearchInfo umsUserSearchInfo);

    List<UmsUserSearchInfo> getUserByFuzzyEmployeeId(UmsUserSearchInfo umsUserSearchInfo);

}
