package com.chapter6classloader.readjar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 自定义的类加载器【子类优先】
 * 从jar读取则相对麻烦一点，java给我们提供了一个专门用来读取jar包文件的类，抽象成一个JarFile的对象。
 * 通过调用这个对象的getInputStream方法，也是可以获取文件的输入流，从而读取字节数组。笔者做了一点相应的缓存，
 * 如果每次查找文件都要先读取jar文件，再遍历查找class文件是非常耗时的操作。于是，笔者选择再加载之前，
 * 把所有的jar包中的所有class读取到内存中，保存在一个map对象中。建立一个全限定名和字节数组的映射。
 * 这样在加载阶段，就能省下很多的时间了。全部的代码如下
 * @author hujiancai
 * @description
 * @data 2017年3月11日
 * @version v_0.1
 */
public class MyJarLoader extends ClassLoader{
    /**
     * lib:表示加载的文件在jar包中
     * 类似tomcat就是{PROJECT}/WEB-INF/lib/
     */
    private String lib;
    /**
     * classes:表示加载的文件是单纯的class文件
     * 类似tomcat就是{PROJECT}/WEB-INF/classes/
     */
    private String classes;
    /**
     * 采取将所有的jar包中的class读取到内存中
     * 然后如果需要读取的时候，再从map中查找
     */
    private Map<String, byte[]> map;

    /**
     * 只需要指定项目路径就好
     * 默认jar加载路径是目录下{PROJECT}/WEB-INF/lib/
     * 默认class加载路径是目录下{PROJECT}/WEB-INF/classes/
     * @param webPath
     * @throws MalformedURLException
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    public MyJarLoader(String webPath) throws NoSuchMethodException, SecurityException, MalformedURLException{
        lib = webPath + "WEB-INF/lib/";
        classes = webPath + "WEB-INF/classes/";
        map = new HashMap<String,byte[]>(64);

        preReadJarFile();
    }

    /**
     * 按照父类的机制，如果在父类中没有找到的类
     * 才会调用这个findClass来加载
     * 这样只会加载放在自己目录下的文件
     * 而系统自带需要的class并不是由这个加载
     */
    @Override
    protected Class<?> findClass(String name){
        try {
            byte[] result = getClassFromFileOrMap(name);
            if(result == null){
                throw new FileNotFoundException();
            }else{
                return defineClass(name, result, 0, result.length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 从指定的classes文件夹下找到文件
     * @param name
     * @return
     */
    private byte[] getClassFromFileOrMap(String name){
        String classPath = classes + name.replace('.', '/') + ".class";
        File file = new File(classPath);
        if(file.exists()){
            InputStream input = null;
            try {
                input = new FileInputStream(file);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int bufferSize = 4096;
                byte[] buffer = new byte[bufferSize];
                int bytesNumRead = 0;
                while ((bytesNumRead = input.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesNumRead);
                }
                return baos.toByteArray();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally{
                if(input != null){
                    try {
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }else{
            if(map.containsKey(name)) {
                //去除map中的引用，避免GC无法回收无用的class文件
                return map.remove(name);
            }
        }
        return null;
    }

    /**
     * 预读lib下面的包
     */
    private void preReadJarFile(){
        List<File> list = scanDir();
        for(File f : list){
            JarFile jar;
            try {
                jar = new JarFile(f);
                readJAR(jar);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 读取一个jar包内的class文件，并存在当前加载器的map中
     * @param jar
     * @throws IOException
     */
    private void readJAR(JarFile jar) throws IOException{
        Enumeration<JarEntry> en = jar.entries();
        while (en.hasMoreElements()){
            JarEntry je = en.nextElement();
            String name = je.getName();
            if (name.endsWith(".class")){
                String clss = name.replace(".class", "").replaceAll("/", ".");
                if(this.findLoadedClass(clss) != null) continue;

                InputStream input = jar.getInputStream(je);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int bufferSize = 4096;
                byte[] buffer = new byte[bufferSize];
                int bytesNumRead = 0;
                while ((bytesNumRead = input.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesNumRead);
                }
                byte[] cc = baos.toByteArray();
                input.close();
                map.put(clss, cc);//暂时保存下来
            }
        }
    }

    /**
     * 扫描lib下面的所有jar包
     * @return
     */
    private List<File> scanDir() {
        List<File> list = new ArrayList<File>();
        File[] files = new File(lib).listFiles();
        for (File f : files) {
            if (f.isFile() && f.getName().endsWith(".jar"))
                list.add(f);
        }
        return list;
    }

    /**
     * 添加一个jar包到加载器中去。
     * @param jarPath
     * @throws IOException
     */
    public void addJar(String jarPath) throws IOException{
        File file = new File(jarPath);
        if(file.exists()){
            JarFile jar = new JarFile(file);
            readJAR(jar);
        }
    }
}