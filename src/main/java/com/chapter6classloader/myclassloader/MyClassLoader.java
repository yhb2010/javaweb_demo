package com.chapter6classloader.myclassloader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;

public class MyClassLoader extends ClassLoader
{
    protected Class<?> findClass(String name) throws ClassNotFoundException
    {
        try
        {
            byte[] bytes = getClassBytes(name);
            Class<?> c = this.defineClass(name, bytes, 0, bytes.length);
            return c;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return super.findClass(name);
    }

    private byte[] getClassBytes(String name) throws Exception
    {
    	//先将
        name = name.replace(".", "/");
        name += ".class";
        //确定目录
        URL url = MyClassLoader.class.getClassLoader().getResource(name);
    	// 这里要读入.class的字节，因此要使用字节流
        FileInputStream fis = new FileInputStream(url.getPath());
        FileChannel fc = fis.getChannel();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        WritableByteChannel wbc = Channels.newChannel(baos);
        ByteBuffer by = ByteBuffer.allocate(1024);

        while (true)
        {
            int i = fc.read(by);
            if (i == 0 || i == -1)
                break;
            by.flip();
            wbc.write(by);
            by.clear();
        }

        fis.close();

        return deCode(baos.toByteArray());
    }

    //假设这个二进制文件是网络上加密传过来的，这样可以在这里做解密处理
    private byte[] deCode(byte[] src){
    	return src;
    }

}