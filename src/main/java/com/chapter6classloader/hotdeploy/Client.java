package com.chapter6classloader.hotdeploy;

//客户端调用类
/**
 * classloader介绍
热部署，即需要jvm释放之前加载的业务class，且重新加载最新的业务class，并释放之前的class（卸载），其实类和普通对象一样都是对象，即如果从gc root除非，没有引用此类的别的对象存在，即会被jvm自动回收。
class文件在加载时，会把二进制文件放在内存中，并会在堆取new出一个表示此class的class对象，然后若new 类对象，则再把此new出来的对象的class对象指向刚才创建的class对象.

 * 测试步骤：
 * 1、BusServiceImpl.java现在项目里打包，生成test2.jar，然后把jar文件拷贝到e:/zsl/test2.jar目录下，删除项目里的BusServiceImpl.java
 * 2、启动Client。
 * 3、可以把BusServiceImpl.java文件拷回来，修改内容，然后重新打包，删除BusServiceImpl.java文件
 * 4、修改e:/zsl/目录下的version.txt文件，看效果。
 * @author DELL
 *
 */
public class Client {

	public static void main(String[] args) {
        Server server = new Server();
        server.init();
        int i=0;
        while(true){
            i++;
            String name="name"+i;
            String result=server.doWork(name);
            System.out.println(result);
            try {
                Thread.sleep(1000 * 5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }

}
