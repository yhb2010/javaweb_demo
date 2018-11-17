package com.chapter2io.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;

public class OutputStreamWriterDemo {

	private String getStream(String url, String fileName){
        try {
        	File f = new File(fileName) ;
        	BufferedWriter out = null ; // 字符输出流
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f))) ; // 字节流变为字符流

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
                out.write(newLine);
                out.newLine();
            }
            out.close();
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
        OutputStreamWriterDemo test = new OutputStreamWriterDemo();
        System.out.println(test.getStream(URL, "e:" + File.separator + "demo.txt"));
    }

}
