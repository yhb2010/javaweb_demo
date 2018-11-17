package com.chapter2io.serversocket;

import java.io.*;
import java.net.*;

/**
 * ServerSocket的构造方法有以下几种重载形式：

◆ServerSocket()throws IOException
◆ServerSocket(int port) throws IOException
◆ServerSocket(int port, int backlog) throws IOException
◆ServerSocket(int port, int backlog, InetAddress bindAddr) throws IOException

在以上构造方法中，参数port指定服务器要绑定的端口（服务器要监听的端口），参数backlog指定客户连接请求队列的长度，参数bindAddr指定服务器要绑定的IP地址。

如果把参数port设为0，表示由操作系统来为服务器分配一个任意可用的端口。由操作系统分配的端口也称为匿名端口。对于多数服务器，会使用明确的端口，而不会使用匿名端口，因为客户程序需要事先知道服务器的端口，才能方便地访问服务器。在某些场合，匿名端口有着特殊的用途。

当服务器进程运行时，可能会同时监听到多个客户的连接请求。例如，每当一个客户进程执行以下代码：Socket socket=new Socket(www.javathinker.org,80);
就意味着在远程www.javathinker.org主机的80端口上，监听到了一个客户的连接请求。管理客户连接请求的任务是由操作系统来完成的。操作系统把这些连接请求存储在一个先进先出的队列中。许多操作系统限定了队列的最大长度，一般为50。
当队列中的连接请求达到了队列的最大容量时，服务器进程所在的主机会拒绝新的连接请求。只有当服务器进程通过ServerSocket的accept()方法从队列中取出连接请求，使队列腾出空位时，队列才能继续加入新的连接请求。

对于客户进程，如果它发出的连接请求被加入到服务器的队列中，就意味着客户与服务器的连接建立成功，客户进程从Socket构造方法中正常返回。如果客户进程发出的连接请求被服务器拒绝，Socket构造方法就会抛出ConnectionException。

ServerSocket构造方法的backlog参数用来显式设置连接请求队列的长度，它将覆盖操作系统限定的队列的最大长度。值得注意的是，在以下几种情况中，仍然会采用操作系统限定的队列的最大长度：

◆backlog参数的值大于操作系统限定的队列的最大长度；
◆backlog参数的值小于或等于0；
◆在ServerSocket构造方法中没有设置backlog参数。

如果主机只有一个IP地址，那么默认情况下，服务器程序就与该IP地址绑定。ServerSocket的第4个构造方法ServerSocket(int port, int backlog, InetAddress bindAddr)有一个bindAddr参数，它显式指定服务器要绑定的IP地址，该构造方法适用于具有多个IP地址的主机。

ServerSocket的isClosed()方法判断ServerSocket是否关闭，只有执行了ServerSocket的close()方法，isClosed()方法才返回true；否则，即使ServerSocket还没有和特定端口绑定，isClosed()方法也会返回false。

ServerSocket的isBound()方法判断ServerSocket是否已经与一个端口绑定，只要ServerSocket已经与一个端口绑定，即使它已经被关闭，isBound()方法也会返回true。
 *
 *
 */
public class Server {

	private int port = 8000;
	private ServerSocket serverSocket;

	public Server() throws IOException {
		serverSocket = new ServerSocket(port, 3); // 连接请求队列的长度为3
		System.out.println("服务器启动");
	}

	public void service() {
		while (true) {
			Socket socket = null;
			try {
				//ServerSocket的accept()方法从连接请求队列中取出一个客户的连接请求，然后创建与客户连接的Socket对象，并将它返回。如果队列中没有连接请求，accept()方法就会一直等待，直到接收到了连接请求才返回。
				socket = serverSocket.accept(); // 从连接请求队列中取出一个连接
				//接下来，服务器从Socket对象中获得输入流和输出流，就能与客户交换数据。当服务器正在进行发送数据的操作时，如果客户端断开了连接，那么服务器端会抛出一个IOException的子类SocketException异常：
				System.out.println("New connection accepted " + socket.getInetAddress() + ":" + socket.getPort());
			} catch (IOException e) {
				//这只是与单个客户通信时遇到的异常，可能是由于客户端过早断开连接引起的，这种异常不应该中断整个while循环
				e.printStackTrace();
			} finally {
				try {
					if (socket != null)
						socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String args[]) throws Exception {
		Server server = new Server();
		Thread.sleep(60000 * 10); // 睡眠10分钟
		//server.service();
		//作了以上修改，服务器与8000端口绑定后，就会在一个while循环中不断执行serverSocket.accept()方法，该方法从队列中取出连接请求，使得队列能及时腾出空位，以容纳新的连接请求。
	}

}