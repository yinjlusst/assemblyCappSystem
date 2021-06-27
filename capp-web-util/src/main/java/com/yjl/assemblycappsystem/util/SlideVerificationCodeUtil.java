package com.yjl.assemblycappsystem.util;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SlideVerificationCodeUtil {

    public static final int IMAGECOUNT = 2; //文件夹中拥有图片的数量
    public static final int ORI_WIDTH = 280;  //源文件宽度
    public static final int ORI_HEIGHT = 171;  //源文件高度




    public static final int TARGRTWIDTH = 55;  //小图宽度
    public static final int TARGRTHEIGHT = 45;  //小图高度

    public static final int circleR=6;//半径
    public static final int r1=2;//距离点

    public static int getOriWidth() {
        return ORI_WIDTH;
    }
    public static int getOriHeight() {
        return ORI_HEIGHT;
    }

    public static Map<String, byte[]> createSlideVerificationImg(int X, int Y) throws Exception {
        //随机获取一张原图targetFile
        int templateNo = (int) Math.floor(Math.random()*IMAGECOUNT);


        File targetFile = null;
        targetFile = new File(SlideVerificationCodeUtil.class.getClassLoader().getResource("static/slideVerificationImg/" + templateNo + ".jpg").getFile());




        //将原图按照模板图进行切割处理
        Map<String, byte[]> resultMap = pictureTemplatesCut(targetFile, X, Y);

        return resultMap;
    }



    /**
     * 根据模板图裁剪图片，生成源图遮罩图和裁剪图
     * @param targetFile 源图文件
     * @param X 感兴趣区域X轴
     * @param Y 感兴趣区域Y轴
     * @return
     * @throws
     */
    public static  Map<String, byte[]> pictureTemplatesCut(File targetFile,int X,int Y) throws Exception {

        if (targetFile == null){
            return null;
        }

        //获取源文件流
        File oriFile = targetFile;

        //获取切除图轮廓数组，用二维数组表示
        int[][] cutImageOneZeroArr = getBlockData();

        //将原图按照小图轮廓 切出小图
        Map<String, BufferedImage> resultBufferedImageMap = cutByTemplateImage(oriFile, cutImageOneZeroArr, X, Y);



        //将BufferedImage 转换成字节数组，输出
        Map<String,byte[]> resultBytesMap = new HashMap<>();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(resultBufferedImageMap.get("oriBufferedImage"), "jpg", os);
        resultBytesMap.put("oriBytes",os.toByteArray());

        ByteArrayOutputStream os1 = new ByteArrayOutputStream();
        ImageIO.write(resultBufferedImageMap.get("cutBufferedImage"), "png", os1);
        resultBytesMap.put("cutBytes",os1.toByteArray());



        return resultBytesMap;


    }

    public static void byte2image(byte[] data,String path){
        if(data.length<3||path.equals(""))
            return;
        //判断输入bai的byte是否为空
        try{
            FileImageOutputStream imageOutput = new FileImageOutputStream(new File(path));
            //打开输入流
            imageOutput.write(data, 0, data.length);
            //将byte写入硬盘
            imageOutput.close();
            System.out.println("Make Picture success,Please find image in " + path);
        } catch(Exception ex) {
            System.out.println("Exception: " + ex);
            ex.printStackTrace();
        }

    }

    private static Map<String,BufferedImage> cutByTemplateImage(File oriFile,int[][] templateImage,int x, int y) throws IOException {
        Map<String,BufferedImage> resultMap = new HashMap<>();
        BufferedImage oriBufferedImage = ImageIO.read(oriFile);
        BufferedImage cutBufferedImage = new BufferedImage(TARGRTWIDTH, TARGRTHEIGHT, BufferedImage.TYPE_4BYTE_ABGR);

        for (int i = 0; i < TARGRTHEIGHT; i++) {
            for (int j = 0; j < TARGRTWIDTH; j++) {
                int rgb = templateImage[i][j];//前面得到的图片的1,0数组
                int rgbOri = oriBufferedImage.getRGB(y+j ,x+i);//原图在该点的颜色
                if (rgb == 1){
                    //如果这一个像素点的二进制数为1，将原图的颜色填入切出图中，并将原图在该点的颜色变为黑色
                    cutBufferedImage.setRGB(j,i,rgbOri);
                    oriBufferedImage.setRGB( y + j,x + i, rgbOri & 0x363636 );
                }
                else {
                    //如果该点像素点二进制数为0，把切出图的颜色设定为透明
                    cutBufferedImage.setRGB(j,i,rgbOri& 0x00ffffff);
                }
            }
        }

        resultMap.put("oriBufferedImage",oriBufferedImage);
        resultMap.put("cutBufferedImage",cutBufferedImage);

        return resultMap;



    }


    private static int[][] getBlockData() {
        int[][] data = new int[TARGRTHEIGHT][TARGRTWIDTH];
        //生成一个长度为4的随机数组
        //四个数对应四条边
        //如果为0，则是平边
        //如果为1，则是凸圆边
        //如果是2，则是凹圆边
        int[] randomShapeArr = new int[4];

        for (int i = 0; i < randomShapeArr.length; i++) {
            randomShapeArr[i] = (int) Math.floor(Math.random()*3);
        }




        double po = circleR*circleR;

        //一定为1的范围
        double begin = circleR+r1;
        double xend = TARGRTHEIGHT-begin;
        double yend = TARGRTHEIGHT-begin;


        //随机生成圆的位置
        double h1Up = 2*begin + 2* + Math.random() * (TARGRTHEIGHT-2*begin);
        double h1Down = 2*begin + Math.random() * (TARGRTHEIGHT-2*begin);
        double h1Left = 2*begin + Math.random() * (TARGRTHEIGHT-2*begin);
        double h1Right = 2*begin+ Math.random() * (TARGRTHEIGHT-2*begin);


        double up = 0;
        double down = 0;
        double left = 0;
        double right = 0;

        double poUp = po;
        double poDown = po;
        double poLeft = po;
        double poRight = po;

        for (int i = 0; i < TARGRTHEIGHT; i++) {
            for (int j = 0; j < TARGRTHEIGHT; j++) {


                if (randomShapeArr[0] == 1){
                    up = Math.pow(i - (circleR - 2),2) + Math.pow(j - h1Up,2);//如果上面为凸,圆的中心为(circleR - 2,h1)
                }else if(randomShapeArr[0] == 2){
                    up = -1 * (Math.pow(i - 2,2) + Math.pow(j - h1Up,2));//如果上面为凹,圆的中心为(2,h1)
                    poUp = -po;
                }else {
                    up = 0;
                }


                if (randomShapeArr[1] == 1){
                    down = Math.pow(i - (xend+2),2) + Math.pow(j - h1Down,2);//如果下面为凸,圆的中心为(xend+2,h1)
                }
                else if(randomShapeArr[1] == 2){
                    down = -1 * (Math.pow(i - (TARGRTHEIGHT-2),2) + Math.pow(j - h1Down,2));//如果下面为凹,圆的中心为(targetHeight-2,h1)
                    poDown = -po;
                }
                else{
                    down = 0;
                }


                if (randomShapeArr[2] == 1){
                    left =  Math.pow(i - h1Left,2) + Math.pow(j - (circleR-2),2);//如果左边为凸，圆的中心为(h1,(circleR-2))
                }
                else if(randomShapeArr[2] == 2){
                    left = -1 * (Math.pow(j-2,2) + Math.pow(i - h1Left,2));//如果左边为凹，圆的中心为(h1,2)
                    poLeft = -po;
                }
                else {
                    left = 0;
                }



                if (randomShapeArr[3] == 1){
                    right = Math.pow(i - h1Right,2) + Math.pow(j - (yend+2),2);//如果右边为凸，圆的中心为(h1,yend+2)
                }
                else if(randomShapeArr[2] == 2){
                    right = -1 * (Math.pow(i - h1Right,2) + Math.pow(j - (TARGRTHEIGHT-2),2));//如果右边为凹，圆的中心为(h1,targetWidth - 2)
                    poRight = -po;
                }
                else {
                    right = 0;
                }



                //if条件中按照上下左右顺序
                if ((i <= begin && up >= poUp)|| (i >= xend && down >= poDown)|| (j <= begin && left >= poLeft)|| (j >= yend && right >= poRight)) {
                    data[i][j] = 0;
                } else {
                    data[i][j] = 1;
                }
            }
        }
        return data;
    }
}
