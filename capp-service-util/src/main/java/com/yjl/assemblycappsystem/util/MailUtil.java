package com.yjl.assemblycappsystem.util;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

public class MailUtil {


    String senderAddr;

    public void setSenderAddr(String senderAddr) {
        this.senderAddr = senderAddr;
    }

    public void sendMail(String addr, String subject, String content, JavaMailSender javaMailSender) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        //true代表的是multipart的类型
        MimeMessageHelper helper = new MimeMessageHelper(message,true);
        helper.setSubject(subject);
        helper.setFrom(senderAddr);
        helper.setTo(addr);
        //true代表支持HTML
        helper.setText(content,true);
        //添加附件
        //helper.addAttachment("通知.docx",new FileSystemResource("D:"));
        javaMailSender.send(message);

    }
}
