package Client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public static void main(String[] args) throws UnknownHostException, IOException {
/*
 * TCP传输，客户端建立的过程
 * 1.创建客户端socket服务，使用socket创建要连接的主机
 * 	建立该对象一创建就明确目的地，要连接的主机
 * 2.如果连接建立成功说明数据传输通道已经建立，通道其实就是流，socket流网络io流
 *   想要流从socket中获取的输入或者输出对象，可以找socket来获取getOutputStream和getInputStream
 * 3. 客户端发数据到服务器，使用输出流，将数据写出到服务器，将数据写出
 * 4. 关闭资源
 */
		//1.创建客户端socket服务
		Socket socket = new Socket("127.0.0.1",10006);
		//2.获取socket流中的输出流
		OutputStream out = socket.getOutputStream();
		//3.使用输出流将指定的数据写出去
		out.write("我来拉拉你是是".getBytes());
		//读取服务端返回的数据，使用socket读取流
		InputStream in = socket.getInputStream();
		byte[] buf = new byte[1024];
		int len= in.read(buf);
		String text = new String(buf,0,len);
		System.out.println(text);
		
		socket.close();
	}

}
