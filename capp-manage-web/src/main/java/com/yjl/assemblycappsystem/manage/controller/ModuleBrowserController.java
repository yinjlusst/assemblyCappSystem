package com.yjl.assemblycappsystem.manage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("moduleBrowser")
public class ModuleBrowserController {

    @RequestMapping("index")
    public String index(){
        return "moduleBrowser";
    }
}