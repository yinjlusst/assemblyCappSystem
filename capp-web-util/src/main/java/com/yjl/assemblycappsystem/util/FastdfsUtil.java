package com.yjl.assemblycappsystem.util;

import com.yjl.assemblycappsystem.commons.WebConstans;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.IOException;

public class FastdfsUtil {
    //该方法将图片文件上传到指定的fastdfs服务器，并返回保存的地址
    public static TrackerServer getTrackerServer(){

        //获取配置文件路径
        String trackerConfig = FastdfsUtil.class.getResource("/tracker.conf").getPath();
        try{
            //ClientGlobal.init(conf_filename)方法初始化
            //conf_filename:配置fastdfs连接地址,最大连接数,最长等待时间...的配置文件
            ClientGlobal.init(trackerConfig);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = null;
        try{
            //获取一个trackerServer实例
            trackerServer = trackerClient.getTrackerServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return trackerServer;
    }


    /**
     * 文件的上传
     * @param multipartFile
     * @return
     */
    public static String uploadDocument(MultipartFile multipartFile){
        //图片存储tracker所在地址
        String url = WebConstans.trackerIp;

        TrackerServer trackerServer = getTrackerServer();

        //通过tracker获取一个Storage连接客户端
        //第二个参数为是否指定一个storageServer
        StorageClient storageClient = new StorageClient(trackerServer, null);

        try {
            //获取上传的文件的二进制对象
            byte[] bytes = multipartFile.getBytes();
            //获取上传文件的拓展名
            String originalFilename = multipartFile.getOriginalFilename();//获取文件名
            int dot = originalFilename.lastIndexOf(".");
            String extName = originalFilename.substring(dot+1);

            //第一个参数是文件的路径，第二个是文件的扩展名，第三个是元数据列表,为null是不传递元数据
            //返回值是上传的信息，上传的地址，组名，文件名...
            //访问文件的路径：192.168.142.128/group1/M00/00/00/wKiOgF-50wCAJMyZAABe9Dr3ixI226.jpg
            String[] uploadInfo = storageClient.upload_file(bytes,extName,null);
            for (int i = 0; i < uploadInfo.length; i++) {
                url += "/" + uploadInfo[i];
            }
        } catch (IOException | MyException e) {
            e.printStackTrace();
        }
        return url;
    }

    /**
     * 文件的下载
     */
    public static int downloadDocument(String groupName,String remoteFilename, BufferedOutputStream output) {
        int result=-1;
        //图片存储tracker所在地址
        String url = WebConstans.trackerIp;

        TrackerServer trackerServer = getTrackerServer();

        //通过tracker获取一个Storage连接客户端
        //第二个参数为是否指定一个storageServer
        StorageClient storageClient = new StorageClient(trackerServer, null);
        try {
            byte[] b = storageClient.download_file(groupName,remoteFilename);
            try{
                if(b != null){
                    output.write(b);
                    result=0;
                }
            }catch (Exception e){
                //用户可能取消了下载
                e.printStackTrace();
            }

            finally {
                if (output != null){
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;

    }

    /**
     * 删除文件
     */
    public static String deleteDocument(String groupName,String remoteFilename){
        TrackerServer trackerServer = getTrackerServer();

        StorageClient storageClient = new StorageClient(trackerServer, null);
        try {
            int i = storageClient.delete_file(groupName, remoteFilename);
            if (i == 0) {
                return "success";
            } else {
                return "fail";
            }
        } catch (IOException | MyException e) {
            e.printStackTrace();
        }

        return "fail";
    }
}
