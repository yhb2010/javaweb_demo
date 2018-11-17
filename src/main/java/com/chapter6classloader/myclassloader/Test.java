package com.chapter6classloader.myclassloader;

public class Test {

	public static void main(String[] args) throws Exception {
		MyClassLoader mcl = new MyClassLoader();
		Class c1 = mcl.findClass("com.chapter6classloader.myclassloader.Person");
		Object obj = c1.newInstance();
		System.out.println(obj);
		System.out.println(obj.getClass().getClassLoader());
	}

}
