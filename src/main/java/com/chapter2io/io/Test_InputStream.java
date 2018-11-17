package com.chapter2io.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

//InputStream ： 是所有字节输入流的超类，一般使用它的子类：FileInputStream等，它能输出字节流；
//通过URL连接获取了InputStream流连接，然后通过read方法来一个字节一个字节的读取字节流并组合在一起（read方法返回-1则数据读取结束），最后以reasults返回。
//输出如下：
//60 33 68 79 67 84 89 80 69 32 104 116 109 108 62 60 33 45 45 83 84 65 84 ……
//这就是字节流，每个数字都是一个字节（Byte，8位），所以如果读取英文的话，用字节流，然后用(char)强制转化一下就行了，但如果有中文等双字节语言或者说需要指定字符编码集的情况，就必须用到InputStreamReader将字节流转化为字符流了。
public class Test_InputStream {

	/**
     * 获取字节流
     * @param url
     * @return
     */
    private String getStream(String url){
        //获取字节流
        InputStream in = null;
        String result = "";
        try {
            in = new URL(url).openStream();
            int tmp;
            while((tmp = in.read()) != -1){
                result += (char)tmp;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //输出字节流
        return result;
    }

    public static void main(String[] args){

        String URL = "http://www.baidu.com";
        Test_InputStream test = new Test_InputStream();
        System.out.println(test.getStream(URL));

    }

}
