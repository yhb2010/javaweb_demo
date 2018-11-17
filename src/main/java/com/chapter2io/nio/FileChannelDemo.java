package com.chapter2io.nio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.EnumSet;

public class FileChannelDemo {

	/**打开文件通道：
	 * 在打开文件通道时可以选择的选项有很多，其中最常见的是读取和写入模式的选择，分别通过java.nio.file.StandardOpenOption枚举类型中的READ和WRITE来声明。
	 * CREATE表示当目标文件不存在时，需要创建一个新文件；
	 * 而CREATE_NEW同样会创建新文件，区别在于如果文件已经存在，则会产生错误；
	 * APPEND表示对文件的写入操作总是发生在文件的末尾处，即在文件的末尾添加新内容；
	 * 当声明了TRUNCATE_EXISTING选项时，如果文件已经存在，那么它的内容将被清空；
	 * DELETE_ON_CLOSE用在需要创建临时文件的时候。
	 *
	 * 另外一种创建文件通道的方式是从已有的FileInputStream类、FileOutputStream类和RandomAccessFile类的对象中得到。
	 * 这3个类都有一个getChannel方法来获取对应的FileChannel类的对象，所得到的FileChannel类的对象的能力取决于其来源流的特征。
	 * 对InputStream类的对象来说，它所得到的FileChannel类的对象是只读的，
	 * 而FileOutputStream类的对象所得到的通道是可写的，
	 * RandomAccessFile类的对象所得到的通道的能力则取决于文件打开时的选项。
	 *
	 */
	public static void openAndWrite() throws IOException {
	    FileChannel channel = FileChannel.open(Paths.get("E:/临时文档/my.txt"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
	    ByteBuffer buffer = ByteBuffer.allocate(64);
	    buffer.putChar('A').flip();
	    channel.write(buffer);
	}

	/**对于FileChannel类来说，得益于文件本身的特性，可以在任意绝对位置进行读写操作，只需额外传入一个参数来指定读写的位置即可。
	 * 在下面代码中，对于一个新创建的文件，同样可以指定任意的写入位置。文件的大小会根据写入的位置自动变化。
	 * @throws IOException
	 */
	public static void readWriteAbsolute() throws IOException {
	    FileChannel channel = FileChannel.open(Paths.get("E:/临时文档/my.txt"), StandardOpenOption.READ, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
	    ByteBuffer writeBuffer = ByteBuffer.allocate(4).putChar('A').putChar('B');
	    writeBuffer.flip();
	    channel.write(writeBuffer, 1024);
	    ByteBuffer readBuffer = ByteBuffer.allocate(2);
	    channel.read(readBuffer, 1026);
	    readBuffer.flip();
	    System.out.println(readBuffer.getChar()); //值为'B'
	}

	/**在使用文件进行I/O操作时的一些典型场景包括把来自其他实体的数据写入文件中，以及把文件中的内容读取到其他实体中，按照通道的概念来说，就是文件通道和其他通道之间的数据传输。
	 * 对于这种常见的需求，FileChannel类提供了transferFrom和transferTo方法用来快速地传输数据，其中transferFrom方法把来自一个实现了ReadableByteChannel接口的通道中的数据写入文件通道中，
	 * 而transferTo方法则把当前文件通道的数据传输到一个实现了WritableByteChannel接口的通道中。在进行这两种方式的数据传输时都可以指定当前文件通道中的传输的起始位置和数据长度。
	 *
	 * 使用FileChannel类中的这两个数据传输方法比传统的使用缓冲区进行循环读取的做法要简单，性能也更好。
	 * 这主要是因为这两个方法在实现中尽可能地使用了底层操作系统的支持。比如，当需要通过HTTP协议来获取一个网页的内容并保存在文件中时，可以使用代码清单3-9中的代码实现。
	 * @param url
	 * @throws IOException
	 */
	public static void loadWebPage(String url) throws IOException {
	    try (
	    	FileChannel destChannel = FileChannel.open(Paths.get("E:/临时文档/content.txt"), StandardOpenOption.WRITE, StandardOpenOption.CREATE)
	    ) {
	        InputStream input = new URL(url).openStream();
	        ReadableByteChannel srcChannel = Channels.newChannel(input);
	        destChannel.transferFrom(srcChannel, 0, Integer.MAX_VALUE);
	    }
	}

	/**如果采用传统的循环读取的方式，使用新的ByteBuffer类会比字节数组简单一些，如代码清单3-10所示。
	 * 使用ByteBuffer类的时候并不需要记录每次实际读取的字节数，但是要注意flip和compact方法的使用。
	 * @param srcFilename
	 * @param destFilename
	 * @throws IOException
	 */
	public void copyUseByteBuffer(String srcFilename, String destFilename) throws IOException {
	    ByteBuffer buffer = ByteBuffer.allocateDirect(32 * 1024);
	    try (
	    	FileChannel src = FileChannel.open(Paths.get(srcFilename), StandardOpenOption.READ);
	        FileChannel dest = FileChannel.open(Paths.get(destFilename), StandardOpenOption.WRITE, StandardOpenOption.CREATE)
	    ) {
	        while (src.read(buffer) > 0 || buffer.position() != 0) {
	            buffer.flip();
	            dest.write(buffer);
	            buffer.compact();
	        }
	    }
	}

	/**如果使用FileChannel类中的传输方法来实现，代码就更加简单了，如代码清单3-11所示，进行复制的逻辑只需要一行代码即可。
	 * @param srcFilename
	 * @param destFilename
	 * @throws IOException
	 */
	public void copyUseChannelTransfer(String srcFilename, String destFilename) throws IOException {
	    try (
	    	FileChannel src = FileChannel.open(Paths.get(srcFilename), StandardOpenOption.READ);
	        FileChannel dest = FileChannel.open(Paths.get(destFilename), StandardOpenOption.WRITE, StandardOpenOption.CREATE)
	    ) {
	        src.transferTo(0, src.size(), dest);
	    }
	}

	/**在对大文件进行操作时，性能问题一直比较难处理。通过操作系统的内存映射文件支持，可以比较快速地对大文件进行操作。内存映射文件的原理在于把系统的内存地址映射到要操作的文件上。
	 * 读取这些内存地址就相当于读取文件的内容，而改变这些内存地址的值就相当于修改文件中的内容。被映射到内存地址上的文件在使用上类似于操作系统中使用的虚拟内存文件。
	 * 通过内存映射的方式对文件进行操作时，不再需要通过I/O操作来完成，而是直接通过内存地址访问操作来完成，这就大大提高了操作文件的性能，因为I/O操作比访问内存地址要慢得多。
	 *
	 * FileChannel类的map方法可以把一个文件的全部或部分内容映射到内存中，所得到的是一个ByteBuffer类的子类MappedByteBuffer的对象，程序只需要对这个MappedByteBuffer类的
	 * 对象进行操作即可。对这个MappedByteBuffer类的对象所做的修改会自动同步到文件内容中。代码清单3-12给出了使用文件通道的内存映射功能的一个示例。在进行内存映射时需要指定映射
	 * 的模式，一共有3种可用的模式，由FileChannel.MapMode这个枚举类型来表示：READ_ONLY表示只能对映射之后的MappedByteBuffer类的对象进行读取操作；READ_WRITE表示是可读可
	 * 写的；而PRIVATE的含义是通过MappedByteBuffer类的对象所做的修改不会被同步到文件中，而是被同步到一个私有的复本中。这些修改对其他同样映射了该文件的程序是不可见的。如果希
	 * 望对MappedByteBuffer类的对象所做的修改被立即同步到文件中，可以使用force方法。
	 * @throws Exception
	 */
	public static void mapperedBuffer() throws Exception {
		Path copy_to = Paths.get("E:/zsl/JavaScript权威指南(第5版)中文版(下)2.pdf");
		FileChannel fileChannel_to = (FileChannel.open(copy_to, EnumSet.of(StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ)));
		int BUFFER_SIZE = 1024;
		String fileName = "E:/zsl/JavaScript权威指南(第5版)中文版(下).pdf";
		long fileLength = new File(fileName).length();
		long bufferCount = 1 + (int)(fileLength / BUFFER_SIZE);
		long remaining = fileLength;
		ByteBuffer rr, ww = null;
		for(int i=0; i< bufferCount; i++) {
			RandomAccessFile file = new RandomAccessFile(fileName, "r");
			rr = file.getChannel().map(MapMode.READ_ONLY, i * BUFFER_SIZE, (int)Math.min(remaining, BUFFER_SIZE));
			ww = fileChannel_to.map(MapMode.READ_WRITE, i * BUFFER_SIZE, (int)Math.min(remaining, BUFFER_SIZE));
			ww.put(rr);
			remaining -= BUFFER_SIZE;
			file.close();
		}
	}

	/**
	 * 当需要在多个程序之间进行数据交换时，文件通常是一种很好的选择。一个程序把产生的输出保存在指定的文件中，另外一个程序进行读取即可。双方只需要在文件的格式上达成
	 * 一致就可以了，内部逻辑的实现都是独立的。但是在这种情况下，对这个文件的访问操作容易产生冲突，而且对两个独立的应用程序来说，也没有什么比较好的方式来实现操作的
	 * 同步。对于这种情况，最好的办法是对文件进行加锁。在一个程序完成操作之前，阻止另外一个程序对该文件的访问。通过FileChannel类的lock和tryLock方法可以对当前文
	 * 件通道所对应的文件进行加锁。加锁时既可以选择锁定文件的全部内容，也可以锁定指定的范围区间中的部分内容。lock和tryLock两个方法的区别在于lock方法是阻塞式的，
	 * 而tryLock方法则不是。当成功加锁之后，会得到一个FileLock类对象。在完成对锁定文件的操作之后，通过FileLock类的release方法可以解除锁定状态，允许其他程序来
	 * 访问。FileLock类表示的锁分共享锁和排它锁两类。共享锁不允许其他程序获取到与当前锁定范围相重叠的排它锁，而获取共享锁是允许的；排它锁不允许其他程序获取到与锁
	 * 定范围相重叠的共享锁和排它锁。如果调用FileLock类的对象的isShared方法的返回值为true，则表明是一个共享锁，否则是排它锁。
	 *
	 * 注意　对FileLock类表示的共享锁和排它锁的限制只发生在待锁定的文件范围与当前已有锁的范围发生重叠的时候。不同程序可以同时在一个文件上加上自己的排它锁，只要这
	 * 些锁的锁定范围不互相重叠即可。
	 */
	public static void fileLockTest(){
		FileChannel channel = null;
        FileLock lock = null;
        try {
            //1. 对于一个只读文件通过任意方式加锁时会报NonWritableChannelException异常
            //2. 无参lock()默认为独占锁，不会报NonReadableChannelException异常，因为独占就是为了写
            //3. 有参lock()为共享锁，所谓的共享也只能读共享，写是独占的，共享锁控制的代码只能是读操作，当有写冲突时会报NonWritableChannelException异常
            channel = new FileOutputStream("logfile.txt",true).getChannel();
            RandomAccessFile raf = new RandomAccessFile("logfile.txt","rw");

            //在文件末尾追加内容的处理
            raf.seek(raf.length());
            channel = raf.getChannel();

            //获得锁方法一：lock()，阻塞的方法，当文件锁不可用时，当前进程会被挂起
            lock = channel.lock();//无参lock()为独占锁
            //lock = channel.lock(0L, Long.MAX_VALUE, true);//有参lock()为共享锁，有写操作会报异常

            //获得锁方法二：trylock()，非阻塞的方法，当文件锁不可用时，tryLock()会得到null值
            //do {
            //  lock = channel.tryLock();
            //} while (null == lock);

            //互斥操作
            ByteBuffer sendBuffer=ByteBuffer.wrap((new Date()+" 写入\n").getBytes());
            channel.write(sendBuffer);
            Thread.sleep(5000);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (lock != null) {
                try {
                    lock.release();
                    lock = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (channel != null) {
                try {
                    channel.close();
                    channel = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
	}

	public static void main(String[] args) throws Exception {
		//readWriteAbsolute();
		//loadWebPage("http://www.baidu.com");
		mapperedBuffer();
	}

}
