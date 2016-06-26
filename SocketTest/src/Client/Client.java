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
 * TCP���䣬�ͻ��˽����Ĺ���
 * 1.�����ͻ���socket����ʹ��socket����Ҫ���ӵ�����
 * 	�����ö���һ��������ȷĿ�ĵأ�Ҫ���ӵ�����
 * 2.������ӽ����ɹ�˵�����ݴ���ͨ���Ѿ�������ͨ����ʵ��������socket������io��
 *   ��Ҫ����socket�л�ȡ���������������󣬿�����socket����ȡgetOutputStream��getInputStream
 * 3. �ͻ��˷����ݵ���������ʹ���������������д������������������д��
 * 4. �ر���Դ
 */
		//1.�����ͻ���socket����
		Socket socket = new Socket("127.0.0.1",10006);
		//2.��ȡsocket���е������
		OutputStream out = socket.getOutputStream();
		//3.ʹ���������ָ��������д��ȥ
		out.write("��������������".getBytes());
		//��ȡ����˷��ص����ݣ�ʹ��socket��ȡ��
		InputStream in = socket.getInputStream();
		byte[] buf = new byte[1024];
		int len= in.read(buf);
		String text = new String(buf,0,len);
		System.out.println(text);
		
		socket.close();
	}

}
