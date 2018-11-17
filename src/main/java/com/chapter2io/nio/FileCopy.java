package com.chapter2io.nio;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.concurrent.Future;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

/**Java 拷贝文件的方式很多，除了 FileChannel 提供的方法外，还包括使用 Files.copy() 或使用字节数组的缓冲/非缓冲流。那个才是最好的选择呢？这个问题很难回答，因为答案基于很多因素。本文将目光集中到一个因素，那就是速度，因为拷贝任务 越快将会提高效率，在有些情况下，这是成功的关键。因此，本文将使用一个应用程序来比较下面这些拷贝方式的具体时间：

FileChannel 和非直接模式的 ByteBuffer
FileChannel 和直接模式的 ByteBuffer
FileChannel.transferTo()
FileChannel.transferFrom()
FileChannel.map()
使用字节数组和缓冲流
使用字节数组和非缓冲流
File.copy()（Path 到 Path，InputStream 到 Path 和 Path 到 OutputStream）
应用程序基于下面的条件：

拷贝文件类型 MP4 视频（文件名为 Rafa Best Shots.mp4，所在目录为 C:\rafaelnadal\tournaments\2009\videos）
文件大小：58.3MB
测试的缓冲区大小：4KB, 16KB, 32KB, 64KB, 128KB, 256KB, and 1024KB
机器配置：Mobile AMD Sempron Processor 3400 + 1.80 GHz, 1.00GB RAM, 32-bit
OS, Windows 7 Ultimate
测量类型：使用 System.nanoTime() 方法
连续运行三次后再获取时间；前三次运行将会被忽略。开始运行的时间总会比后面运行的时间要长一些。

FileChannel 和非直接模式 Buffer vs. FileChannel.transferTo() vs. Path 到 Path
最后，我们将前面最快的三种方式综合起来比较。从比较的结果来看，似乎 Path 到 Path 是最快的解决方案。

transferXXX与传统的访问文件方式相比可以减少数据从内核到用户空间的复制，数据直接在内核空间中移动。
 * @author DELL
 *
 */
public class FileCopy {

	private final static Path copy_from = Paths.get("E:/zsl/advcconsole.txt");
	private final static Path copy_to = Paths.get("E:/zsl/advcconsole2.txt");
	private static long startTime, elapsedTime;
	private static int bufferSizeKB = 1024;// also tested for 16, 32, 64, 128, 256 and 1024
	private static int bufferSize = bufferSizeKB * 1024;

	public static void main(String[] args) throws Exception {

		//transferfrom();

		//transferTo();

		//nonDirectBuffer();

		nonDirectBufferFuture();

		//directBuffer();

		//mapperedBuffer();

		//ioBufferedStream();

		//ioUnBufferedStream();

		//copyPath2Path();

		//copyInputStream2Path();

		//copyPath2OutputStream();

		// randomReadFile();

	}

	public static void transferfrom() {
		try (
			//创建AsynchronousFileChannel：AsynchronousFileChannel使得数据可以进行异步读写。下面将介绍一下AsynchronousFileChannel的使用。
			//open()的第一个参数是一个Path实体，指向我们需要操作的文件。 第二个参数是操作类型。上述示例中我们用的是StandardOpenOption.READ，表示以读的形式操作文件。
			FileChannel fileChannel_from = (FileChannel.open(copy_from, EnumSet.of(StandardOpenOption.READ)));
			FileChannel fileChannel_to = (FileChannel.open(copy_to, EnumSet.of(StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)))
		) {
			startTime = System.nanoTime();
			//FileChannel的transferFrom()方法可以将数据从源通道传输到FileChannel中（译者注：这个方法在JDK文档中的解释为将字节从给定的可读取字节通道传输到此通道的文件中）。
			//方法的输入参数position表示从position处开始向目标文件写入数据，count表示最多传输的字节数。如果源通道的剩余空间小于 count 个字节，则所传输的字节数要小于请求的字节数。
			//你可以看一下JDK中关于FileChannel的实现，是rt.jar里的FileChannelImpl类。对于FileChannel之间的transferFrom，第三个参数count会有限制，最大值是int的MAX_VALUE，就是说一次性传输的最多2147483647个字节，所以你的文件超过了2.1G（左右），FileChannel就会自动给你截断了。
			fileChannel_to.transferFrom(fileChannel_from, 0L, (int) fileChannel_from.size());
			elapsedTime = System.nanoTime() - startTime;
			System.out.println("Elapsed Time is " + (elapsedTime / 1000000000.0) + " seconds");
		} catch (IOException ex) {
			System.err.println(ex);
		}
		deleteCopied(copy_to);
	}

	public static void transferTo() throws Exception {
		try (
			FileChannel fileChannel_from = (FileChannel.open(copy_from, EnumSet.of(StandardOpenOption.READ)));
			FileChannel fileChannel_to = (FileChannel.open(copy_to, EnumSet.of(StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)))
		) {
			startTime = System.nanoTime();
			fileChannel_from.transferTo(0L, fileChannel_from.size(), fileChannel_to);
			elapsedTime = System.nanoTime() - startTime;
			System.out.println("Elapsed Time is " + (elapsedTime / 1000000000.0) + " seconds");
		} catch (IOException ex) {
			System.err.println(ex);
		}
		deleteCopied(copy_to);

	}

	//数据首先要经过操作系统的socket缓冲区，再将数据复制到Buffer中，从操作系统缓冲区到用户缓冲区复制数据比较耗性能，Buffer提供了另外一个直接操作操作系统缓冲区的方式，即directBuffer。
	//这个方法返回的DirectByteBuffer就是与底层存储空间关联的缓冲区，它通过native代码操作非jvm堆的内存空间。每个创建或者释放的时候都调用一次System.gc()。注意，在使用DirectByteBuffer时可能会引起内存泄露问题。
	//DirectByteBuffer适用于数据量比较大，生命周期比较长的情况。HeapByteBuffer适用于并发数少于1000，io操作较少时。
	public static void nonDirectBuffer() {
		try (
			FileChannel fileChannel_from = FileChannel.open(copy_from, EnumSet.of(StandardOpenOption.READ));
			FileChannel fileChannel_to = FileChannel.open(copy_to, EnumSet.of(StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE));
		) {
			startTime = System.nanoTime();
			ByteBuffer bytebuffer = ByteBuffer.allocate(bufferSize);
			int bytesCount;
			while ((bytesCount = fileChannel_from.read(bytebuffer)) > 0) {
				bytebuffer.flip();
				fileChannel_to.write(bytebuffer);
				bytebuffer.clear();
			}
			elapsedTime = System.nanoTime() - startTime;
			System.out.println("Elapsed Time is " + (elapsedTime / 1000000000.0) + " seconds");
		} catch (IOException ex) {
			System.err.println(ex);
		}
		deleteCopied(copy_to);
	}

	public static void nonDirectBufferFuture() {
		try (
			AsynchronousFileChannel fileChannel_from = AsynchronousFileChannel.open(copy_from, StandardOpenOption.READ);
			AsynchronousFileChannel fileChannel_to = AsynchronousFileChannel.open(copy_to, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
		) {
			startTime = System.nanoTime();
			ByteBuffer bytebuffer = ByteBuffer.allocate(bufferSize);
			int i = 0;
			//read()方法会立即返回，即使读操作还没有完成。通过调用read()方法返回的Future实例的isDone()方法，您可以检查读取操作是否完成。
			while (i < fileChannel_from.size()) {
				Future<Integer> operation = fileChannel_from.read(bytebuffer, i);
				System.out.println("position:"+bytebuffer.position()+"\t limit:"+bytebuffer.limit());
				while(!operation.isDone());
				System.out.println("position:"+bytebuffer.position()+"\t limit:"+bytebuffer.limit());
				bytebuffer.flip();
				System.out.println("position:"+bytebuffer.position()+"\t limit:"+bytebuffer.limit());
				operation = fileChannel_to.write(bytebuffer, i);
				while(!operation.isDone());
				bytebuffer.clear();
				System.out.println("Write done");
				i += bufferSize;
			}

			//byte[] data = new byte[bytebuffer.limit()];
			//bytebuffer.get(data);
			//System.out.println(new String(data));
			elapsedTime = System.nanoTime() - startTime;
			System.out.println("Elapsed Time is " + (elapsedTime / 1000000000.0) + " seconds");
		} catch (IOException ex) {
			System.err.println(ex);
		}
		deleteCopied(copy_to);
	}

	public static void directBuffer() {
		try (
			FileChannel fileChannel_from = FileChannel.open(copy_from, EnumSet.of(StandardOpenOption.READ));
			FileChannel fileChannel_to = FileChannel.open(copy_to, EnumSet.of(StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE));
		) {
			startTime = System.nanoTime();
			ByteBuffer bytebuffer = ByteBuffer.allocateDirect(bufferSize);
			int bytesCount;
			while ((bytesCount = fileChannel_from.read(bytebuffer)) > 0) {
				bytebuffer.flip();
				fileChannel_to.write(bytebuffer);
				bytebuffer.clear();
			}
			elapsedTime = System.nanoTime() - startTime;
			System.out.println("Elapsed Time is " + (elapsedTime / 1000000000.0) + " seconds");
		} catch (IOException ex) {
			System.err.println(ex);
		}
		deleteCopied(copy_to);
	}

	public static void mapperedBuffer() throws Exception {
		try (
			FileChannel fileChannel_from = (FileChannel.open(copy_from, EnumSet.of(StandardOpenOption.READ)));
			FileChannel fileChannel_to = (FileChannel.open(copy_to, EnumSet.of(StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)))
		) {
			startTime = System.nanoTime();
			int i = 0;
			long size = fileChannel_from.size() / 30;
			ByteBuffer rr, ww = null;
			while (i < fileChannel_from.size() && (fileChannel_from.size() - i) > size) {
				rr = fileChannel_from.map(MapMode.READ_ONLY, i, size);
				ww = fileChannel_to.map(MapMode.READ_WRITE, i, size);
				ww.put(rr);
				rr.clear();
				ww.clear();
				i += size;
			}
			rr = fileChannel_from.map(MapMode.READ_ONLY, i, fileChannel_from.size() - i);
			ww = fileChannel_to.map(MapMode.READ_WRITE, i, fileChannel_from.size() - i);
			ww.put(rr);
			rr.clear();
			ww.clear();
			elapsedTime = System.nanoTime() - startTime;
			System.out.println("Elapsed Time is " + (elapsedTime / 1000000000.0) + " seconds");
		} catch (IOException ex) {
			System.err.println(ex);
		}
		deleteCopied(copy_to);
	}

	public static void ioBufferedStream() {
		File inFileStr = copy_from.toFile();
		File outFileStr = copy_to.toFile();
		try (
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(inFileStr));
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outFileStr))
		) {
			startTime = System.nanoTime();
			byte[] byteArray = new byte[bufferSize];
			int bytesCount;
			while ((bytesCount = in.read(byteArray)) != -1) {
				out.write(byteArray, 0, bytesCount);
			}
			elapsedTime = System.nanoTime() - startTime;
			System.out.println("Elapsed Time is " + (elapsedTime / 1000000000.0) + " seconds");
		} catch (IOException ex) {
			System.err.println(ex);
		}
		deleteCopied(copy_to);
	}

	public static void ioUnBufferedStream() {
		File inFileStr = copy_from.toFile();
		File outFileStr = copy_to.toFile();
		try (
			FileInputStream in = new FileInputStream(inFileStr);
			FileOutputStream out = new FileOutputStream(outFileStr)
		) {
			startTime = System.nanoTime();
			byte[] byteArray = new byte[bufferSize];
			int bytesCount;
			while ((bytesCount = in.read(byteArray)) != -1) {
				out.write(byteArray, 0, bytesCount);
			}
			elapsedTime = System.nanoTime() - startTime;
			System.out.println("Elapsed Time is " + (elapsedTime / 1000000000.0) + " seconds");
		} catch (IOException ex) {
			System.err.println(ex);
		}
		deleteCopied(copy_to);
	}

	public static void copyPath2Path() {
		try {
			startTime = System.nanoTime();
			Files.copy(copy_from, copy_to, NOFOLLOW_LINKS);
			elapsedTime = System.nanoTime() - startTime;
			System.out.println("Elapsed Time is " + (elapsedTime / 1000000000.0) + " seconds");
		} catch (IOException e) {
			System.err.println(e);
		}
		deleteCopied(copy_to);
	}

	public static void copyInputStream2Path() {
		try (
			InputStream is = new FileInputStream(copy_from.toFile())
		) {
			startTime = System.nanoTime();
			Files.copy(is, copy_to);
			elapsedTime = System.nanoTime() - startTime;
			System.out.println("Elapsed Time is " + (elapsedTime / 1000000000.0) + " seconds");
		} catch (IOException e) {
			System.err.println(e);
		}
		deleteCopied(copy_to);
	}

	public static void copyPath2OutputStream() {
		try (
			OutputStream os = new FileOutputStream(copy_to.toFile())
		) {
			startTime = System.nanoTime();
			Files.copy(copy_from, os);
			elapsedTime = System.nanoTime() - startTime;
			System.out.println("Elapsed Time is " + (elapsedTime / 1000000000.0) + " seconds");
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	public static void randomReadFile() {
		try (
			RandomAccessFile read = new RandomAccessFile("C:\\Users\\asus\\Desktop\\cn_windows_7_ultimate_with_sp1_x86_dvd_618763.iso", "r");
			RandomAccessFile writer = new RandomAccessFile("C:\\Users\\asus\\Desktop\\dwTest\\cn_windows_7_ultimate_with_sp1_x86_dvd_618763.iso", "rw");
		) {
			startTime = System.nanoTime();
			byte[] b = new byte[200 * 1024 * 1024];
			while (read.read(b) != -1) {
				writer.write(b);
			}
			elapsedTime = System.nanoTime() - startTime;
			System.out.println("Elapsed Time is " + (elapsedTime / 1000000000.0) + " seconds");
		} catch (Exception e) {
			System.err.println(e);
		}
		deleteCopied(copy_to);
	}

	public static void deleteCopied(Path path) {
		//try {
			//Files.deleteIfExists(path);
		//} catch (IOException ex) {
			//System.err.println(ex);
		//}
	}

}
