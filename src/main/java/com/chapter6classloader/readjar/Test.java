package com.chapter6classloader.readjar;

import com.chapter6classloader.urlclassloader.ActionInterface;

public class Test {

	public static void main(String[] args) throws Exception {
		MyJarLoader mcl = new MyJarLoader("e:/zsl/");
		Class c1 = mcl.findClass("com.chapter6classloader.readjar.Person");
		Object obj = c1.newInstance();
		System.out.println(obj);
		System.out.println(obj.getClass().getClassLoader());

		System.out.println("-------------------");
		MyJarLoader mcl2 = new MyJarLoader("e:/zsl/");
		Class c2 = mcl2.findClass("com.chapter6classloader.urlclassloader.TestAction");
		ActionInterface obj2 = (ActionInterface)c2.newInstance();
		System.out.println(obj2);
		System.out.println(obj2.getClass().getClassLoader());
		String str1 = obj2.action();
		System.out.println(str1);
	}

}
