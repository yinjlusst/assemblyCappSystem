package com.yjl.assemblycappsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.yjl.assemblycappsystem.passport.mapper")
public class CappPassportServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CappPassportServiceApplication.class, args);
    }

}
