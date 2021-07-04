package com.yjl.assemblycappsystem.passport;


import com.yjl.assemblycappsystem.bean.UmsUserHeadPortraitUrl;
import com.yjl.assemblycappsystem.passport.mapper.UmsUserHeadPortraitUrlMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class CappPassportServiceApplicationTests {
    @Autowired
    UmsUserHeadPortraitUrlMapper umsUserHeadPortraitUrlMapper;

    @Test
    public void contextLoads() {
        UmsUserHeadPortraitUrl umsUserHeadPortraitUrl = new UmsUserHeadPortraitUrl();
        umsUserHeadPortraitUrl.setUserId(201);
        umsUserHeadPortraitUrl.setHeadPortraitUrl("http://1922222");
        umsUserHeadPortraitUrl.setIsDel("0");

        UmsUserHeadPortraitUrl insert = umsUserHeadPortraitUrlMapper.insert(umsUserHeadPortraitUrl);
        System.out.println(insert);


    }

}
