package com.chapter6classloader.hotdeploy2;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;

public class ClassReloader extends ClassLoader {

	private String classPath;
	String classname = "com.chapter6classloader.hotdeploy2.Person";

	public ClassReloader(String classPath){
		this.classPath = classPath;
	}

	protected Class<?> findClass(String name) throws ClassNotFoundException {
		byte[] classDate = getDate(name);
		if(classDate == null){
			throw new ClassNotFoundException();
		}else{
			return defineClass(classname, classDate, 0, classDate.length);
		}
	}

	private byte[] getDate(String name) {
		String path = classPath + name;
		try{
			InputStream is = new FileInputStream(path);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			byte[] buffer = new byte[2048];
			int num = 0;
			while((num = is.read(buffer)) != -1){
				stream.write(buffer, 0, num);
			}
			return stream.toByteArray();
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) {
		try{
			String path = "E:/workspace/my/javaweb/target/classes/com/chapter6classloader/hotdeploy2/";
			ClassReloader load = new ClassReloader(path);
			Class r = load.findClass("Person.class");
			System.out.println(r.newInstance());
			ClassReloader load2 = new ClassReloader(path);
			Class r2 = load2.findClass("Person.class");
			//写成这样Class r2 = load2.findClass("Person.class");
			//会报异常：attempted  duplicate class definition for name: "com/chapter6classloader/hotdeploy2/Person"，重复加载类
			System.out.println(r2.newInstance());
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
