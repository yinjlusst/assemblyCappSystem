package com.yjl.asemblycappsystem.conf;

import com.yjl.asemblycappsystem.util.MailUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailConfig {
    @Value("${spring.mail.username:841728139@qq.com}")
    String sendAddr;

    @Bean
    public MailUtil mailUtil(){
        MailUtil mailUtil = new MailUtil();
        mailUtil.setSenderAddr(sendAddr);
        return mailUtil;
    }
}
