package com.chapter6classloader.hotdeploy;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Date;

//热部署框架类
//只要codePath+"version.txt" 文件的修改时间发生变化，就会自动加载最新的业务类，并释放旧的类。
//特别注意的是，释放旧类时，若旧类里面启动新的线程的话，一定要关闭，否则既不会释放旧类，而且线程会一直运行。虽然加载了最新的类，但旧类并没有释放，会导致内存占用和一些别的不可预估的问题。
public class Server {

	String codePath = "e:/zsl/test2.jar";
    String busServiceClass="com.chapter6classloader.hotdeploy.BusServiceImpl";
    BusService busService;
    public String doWork(String name){
        if(null != busService){
            return busService.doIt(name);
        }

        return "default";
    }

    public void init(){
        new Thread(){
            long lastTime=0;
            public void run() {
                File f = new File("e:/zsl/version.txt");
                while(true){
                    if(lastTime != f.lastModified()){
                        lastTime = f.lastModified();

                        MyClassLoader myLoader = new MyClassLoader(new URL[0]);
                        try {
                            myLoader.addDir(codePath);
                            Class<BusService> clazz = (Class<BusService>) myLoader.loadClass(busServiceClass);
                            BusService busService2 = clazz.newInstance();
                            BusService temp = busService;
                            busService = busService2;
                            if(temp != null){
	                            temp.close();//释放资源，尤其是线程，若线程不关闭的话，则类不会卸载，且会一直运行
	                            ClassLoader c = temp.getClass().getClassLoader();
	                            if(c instanceof URLClassLoader) ((URLClassLoader) c).close();//释放资源
	                            System.out.println("busService:"+busService + "  ,classloader:"+busService.getClass().getClassLoader());
	                            System.out.println("end test "+ new Date().toLocaleString());
                            }
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            };
        }.start();

        //myLoader.close();
    }

}
