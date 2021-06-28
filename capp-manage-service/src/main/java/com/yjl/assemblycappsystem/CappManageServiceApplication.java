package com.yjl.assemblycappsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.yjl.assemblycappsystem.manage.mapper")
public class CappManageServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CappManageServiceApplication.class, args);
    }

}
