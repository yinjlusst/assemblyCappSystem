package com.yjl.assemblycappsystem.manage;


import com.yjl.assemblycappsystem.util.HttpclientUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class CappManageWebApplicationTests {


    @Test
    public void contextLoads() {
        HttpclientUtil.doGet("");
    }

    @Test
    public void download(){
        Map<String,String> map = new HashMap<>();
        map.put("url","http://192.168.142.128/group1/M00/00/01/wKiOgGDgT3GAErG0AAACEv55tJ0320.txt");
        map.put("outPath","F:\\javawebStudy\\笔记\\a.txt");
        String s = HttpclientUtil.doPost("http://127.0.0.1:9093/index/download",map);
        System.out.println(s);
    }

    @Test
    public void upload(){
        Map<String,String> map = new HashMap<>();
        map.put("url","http://192.168.142.128/group1/M00/00/01/wKiOgGDgT3GAErG0AAACEv55tJ0320.txt");
        map.put("outPath","F:\\javawebStudy\\笔记\\a.txt");
        String s = HttpclientUtil.doPost("http://127.0.0.1:9093/index/download",map);
        System.out.println(s);
    }

    @Test
    public void getToken(){
        Map<String,String> map = new HashMap<>();
        map.put("username","yinjianliang");
        map.put("password","SBNND3210");
        String s = HttpclientUtil.doPost("http://127.0.0.1:9093/passport/getToken",map);
        System.out.println(s);

    }

}
