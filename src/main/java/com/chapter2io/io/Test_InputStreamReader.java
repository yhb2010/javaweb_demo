package com.chapter2io.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

//InputStreamReader ： 是字节流与字符流之间的桥梁，能将字节流输出为字符流，并且能为字节流指定字符集，可输出一个个的字符；
public class Test_InputStreamReader {

	/*
     * 得到字符流前需先有字节流
     */
    private String getStream(String url){
        try {
            //得到字节流
            InputStream in = new URL(url).openStream();
            //将字节流转化成字符流，并指定字符集
            InputStreamReader isr = new InputStreamReader(in,"UTF-8");
            String results = "";
            int tmp;
            while((tmp = isr.read()) != -1){
                results += (char)tmp;
            }
            return results;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        String URL = "http://www.baidu.com";
        Test_InputStreamReader test = new Test_InputStreamReader();
        System.out.println(test.getStream(URL));
    }

}
