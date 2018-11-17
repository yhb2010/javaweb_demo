package com.chapter2io.serversocket;

import java.net.*;

public class Client {

	public static void main(String args[]) throws Exception {
		final int length = 100;
		String host = "localhost";
		int port = 8000;
		Socket[] sockets = new Socket[length];
		//从打印结果可以看出，Client与Server在成功地建立了3个连接后，就无法再创建其余的连接了，因为服务器的队列已经满了。
		for (int i = 0; i < length; i++) { // 试图建立100次连接
			sockets[i] = new Socket(host, port);
			System.out.println("第" + (i + 1) + "次连接成功");
		}
		Thread.sleep(3000);
		for (int i = 0; i < length; i++) {
			sockets[i].close(); // 断开连接
		}
	}

}