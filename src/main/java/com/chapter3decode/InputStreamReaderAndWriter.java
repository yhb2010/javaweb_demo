package com.chapter3decode;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class InputStreamReaderAndWriter {

	public static void main(String[] args) throws Exception {
		//写字符转换成字节流
		String file = "e:/zsl/stream.txt";
		String charset = "UTF-8";
		FileOutputStream outputStream = new FileOutputStream(file);
		//OutputStreamWriter是Writer和OutputStream之间的桥梁，需要字符到字节的编码，这个操作由StreamEncode实现，StreamEncode的编码过程需要指定Charset编码格式。
		OutputStreamWriter writer = new OutputStreamWriter(outputStream, charset);
		writer.write("这是一段中文");
		writer.close();

		//读取字节转换成字符
		FileInputStream inputStream = new FileInputStream(file);
		InputStreamReader reader = new InputStreamReader(inputStream, charset);
		StringBuffer buffer = new StringBuffer();
		char[] buf = new char[64];
		int count = 0;
		while((count = reader.read(buf)) != -1){
			buffer.append(buf, 0, count);
		}
		reader.close();
		System.out.println(buffer.toString());
	}

}
