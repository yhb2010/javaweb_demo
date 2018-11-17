package com.chapter6classloader.urlclassloader;

import java.net.URL;
import java.net.URLClassLoader;

//URLClassLoader是ClassLoader的子类，它用于从指向 JAR 文件和目录的 URL 的搜索路径加载类和资源。也就是说，通过URLClassLoader就可以加载指定jar中的class到内存中。
//下面来看一个例子，在该例子中，我们要完成的工作是利用URLClassLoader加载jar并运行其中的类的某个方法。
//将TestAction打包为test.jar文件。
public class ClassLoaderTest {

	public static void main(String[] args) {
		try {
			URL url1 = new URL("file:e:/zsl/test.jar");
			URLClassLoader myClassLoader1 = new URLClassLoader(
					new URL[] { url1 }, Thread.currentThread().getContextClassLoader());
			Class myClass1 = myClassLoader1.loadClass("com.chapter6classloader.urlclassloader.TestAction");
			ActionInterface action1 = (ActionInterface) myClass1.newInstance();
			System.out.println("classloader:"+action1.getClass().getClassLoader());

			String str1 = action1.action();
			System.out.println(str1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
