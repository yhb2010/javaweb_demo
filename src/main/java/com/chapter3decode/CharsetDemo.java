package com.chapter3decode;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

public class CharsetDemo {

	public static void main(String[] args) throws Exception {
		//字符到字节的相互转换
		Charset charset = Charset.forName("UTF-8");
		ByteBuffer byteBuffer = charset.encode("我是中文");
		CharBuffer charBuffer = charset.decode(byteBuffer);
		System.out.println(charBuffer);
	}

}
