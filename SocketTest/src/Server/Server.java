package Server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		/*
		 * ����tcp����˵�˼·��
		 * 1.���������socket����ͨ��serversocket����
		 * 2.����˱�������ṩһ���˿ڣ�����ͻ����޷�����
		 * 3.��ȡ���ӹ����Ŀͻ��˶���
		 * 4.ͨ���ͻ��˶����ȡsocket����ȡ�ͻ��˷���������
		 * 5.�ر���Դ���رտͻ��ˣ��رշ����
		 */
		//1.��������˶���
		ServerSocket ss = new ServerSocket(10006);
		//2.��ȡ���ӹ����Ŀͻ��˶���
		
		Socket s = ss.accept();
		String ip = s.getInetAddress().getHostAddress();
		System.out.println(ip);
		//3.ͨ��socket�����ȡ��������Ҫ��ȡ�ͻ��˷���������
		InputStream in = s.getInputStream();
		byte[] buf = new byte[1024];
		int len = in.read(buf);
		String text = new String(buf,0,len);
		System.out.println(ip+":"+text);
		
		//���ÿͻ���socket�������������ͻ��˷�������
		OutputStream out = s.getOutputStream();
		out.write("�������յ�����:".getBytes());
		s.close();
		ss.close();
	}

}

