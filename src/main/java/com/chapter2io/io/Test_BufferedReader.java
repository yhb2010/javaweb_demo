package com.chapter2io.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

//BufferedReader ： 提供通用的缓冲方式文本读取，readLine读取一个文本行， 从字符输入流中读取文本，缓冲各个字符，从而提供字符、数组和行的高效读取。
//获取字符流后，可直接缓存，然后从缓存区取出，这时的速度比InputStreamReader又将快上很多。
public class Test_BufferedReader {

	/*
     * 字节流——字符流——缓存输出的字符流
     */
    private String getStream(String url){
        try {
            //得到字节流
            InputStream in = new URL(url).openStream();
            //将字节流转化成字符流，并指定字符集
            InputStreamReader isr = new InputStreamReader(in, "UTF-8");
            //将字符流以缓存的形式一行一行输出
            BufferedReader bf = new BufferedReader(isr);
            String results = "";
            String newLine = "";
            while((newLine = bf.readLine()) != null){
                results += newLine+"\n";
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
        Test_BufferedReader test = new Test_BufferedReader();
        System.out.println(test.getStream(URL));
    }

}
