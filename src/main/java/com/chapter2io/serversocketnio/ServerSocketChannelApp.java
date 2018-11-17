package com.chapter2io.serversocketnio;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

//如果您选择在ServerSocketChannel上调用accept( )方法则会返回SocketChannel类型的对象，返回的对象能够在非阻塞模式下运行。
//如果以非阻塞模式被调用，当没有传入连接在等待时，ServerSocketChannel.accept( )会立即返回null (因为他是非阻塞的所以要有返回)。
public class ServerSocketChannelApp {

	private static final String MSG = "hello, I must be going \n";

    public static void main(String[] args) throws Exception {

        int port = 8989;
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ServerSocket ss = ssc.socket();
        ss.bind(new InetSocketAddress(port));
        // set no blocking
        ssc.configureBlocking(false);

        ByteBuffer buffer = ByteBuffer.wrap(MSG.getBytes());

        while (true) {
            System.out.println("wait for connection ……");
            SocketChannel sc = ssc.accept();

            if (sc == null) {
                // no connections, snooze a while ...
                Thread.sleep(1000);
            } else {
                System.out.println("Incoming connection from " + sc.socket().getRemoteSocketAddress());
                buffer.rewind();
                //write msg to client
                sc.write(buffer);
                sc.close();
            }
        }
    }

}
