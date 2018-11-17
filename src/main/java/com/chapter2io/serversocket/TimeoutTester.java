package com.chapter2io.serversocket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * ServerSocket选项

ServerSocket有以下3个选项。

◆SO_TIMEOUT：表示等待客户连接的超时时间。
◆SO_REUSEADDR：表示是否允许重用服务器所绑定的地址。
◆SO_RCVBUF：表示接收数据的缓冲区的大小。

SO_TIMEOUT表示ServerSocket的accept()方法等待客户连接的超时时间，以毫秒为单位。如果SO_TIMEOUT的值为0，表示永远不会超时，这是SO_TIMEOUT的默认值。

当服务器执行ServerSocket的accept()方法时，如果连接请求队列为空，服务器就会一直等待，直到接收到了客户连接才从accept()方法返回。如果设定了超时时间，那么当服务器等待的时间超过了超时时间，就会抛出SocketTimeoutException，它是InterruptedException的子类。

如例程3-4所示的TimeoutTester把超时时间设为6秒钟。

如果把程序中的“serverSocket.setSoTimeout(6000)”注释掉，那么serverSocket. accept()方法永远不会超时，它会一直等待下去，直到接收到了客户的连接，才会从accept()方法返回。

Tips：服务器执行serverSocket.accept()方法时，等待客户连接的过程也称为阻塞。
 * @author DELL
 *
 */
public class TimeoutTester {

	public static void main(String args[]) throws IOException {
		ServerSocket serverSocket = new ServerSocket(8000);
		serverSocket.setSoTimeout(6000); // 等待客户连接的时间不超过6秒
		Socket socket = serverSocket.accept();
		socket.close();
		System.out.println("服务器关闭");
	}

}
