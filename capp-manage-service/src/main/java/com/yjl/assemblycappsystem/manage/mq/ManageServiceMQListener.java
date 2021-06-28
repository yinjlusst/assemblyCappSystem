package com.yjl.assemblycappsystem.manage.mq;

import com.alibaba.fastjson.JSON;
import com.yjl.assemblycappsystem.bean.Assembly;
import com.yjl.assemblycappsystem.bean.MmsModuleDesignJson;
import com.yjl.assemblycappsystem.bean.PmsProcessDocumentText;
import com.yjl.assemblycappsystem.service.DocumentService;
import com.yjl.assemblycappsystem.util.HttpclientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.mail.MessagingException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

@Component
public class ManageServiceMQListener {
    @Autowired
    DocumentService documentService;


    //注解第一个参数表示监听队列的名字，第二个参数对应消息监听的连接工厂的Bean,在ActiveMQConfig中
    @JmsListener(destination = "PARSE_PROCESS_DOCUMENT_QUEUE",containerFactory = "jmsQueueListener")
    public void consumerUpdateLastLoginTimeQueueResult(TextMessage textMessage) throws JMSException {
        String id = textMessage.getText().substring(textMessage.getText().lastIndexOf('&') + 1) ;

        String url = textMessage.getText().substring(0,textMessage.getText().lastIndexOf('&'));
        String documentPath = HttpclientUtil.doGet("http://manage.capp.com:7001/processManage/downloadDocument?url="+url);

        //使用字符流读取数据
        String text = "";
        BufferedReader bufferedReader = null;
        try{
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(documentPath), "UTF-8"));
            //按行读取
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = bufferedReader.readLine())!= null){
                sb.append(line);
            }
            //保存到数据库
            PmsProcessDocumentText pmsProcessDocumentText = new PmsProcessDocumentText();
            pmsProcessDocumentText.setDocumentId(Integer.parseInt(id));
            pmsProcessDocumentText.setText(sb.toString());
            String success = documentService.saveProcessDocumentText(pmsProcessDocumentText);

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try{
                bufferedReader.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    //注解第一个参数表示监听队列的名字，第二个参数对应消息监听的连接工厂的Bean,在ActiveMQConfig中
    @JmsListener(destination = "MODULE_DESIGN_INFO_QUEUE",containerFactory = "jmsQueueListener")
    public void consumerSendLoginNoteQueue(TextMessage textMessage) throws JMSException, MessagingException {
        String message = textMessage.getText();

        int indexAnd = message.lastIndexOf('&');
        String designJson = message.substring(0,indexAnd);
        String id = message.substring(indexAnd+1);


        Assembly assembly = JSON.parseObject(designJson, Assembly.class);
        //将assembly对象转换成能够数据库的映射的类型MmsModuleFeatureInfo形式
        MmsModuleDesignJson mmsModuleDesignJson = new MmsModuleDesignJson();
        mmsModuleDesignJson.setDesignJson(designJson);
        mmsModuleDesignJson.setDocumentId(Integer.parseInt(id));



        documentService.saveDesignJson(mmsModuleDesignJson);
    }

}