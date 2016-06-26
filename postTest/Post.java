package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class Post {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		
		new Post().getDataFromUrl();

	}

	private  void getDataFromUrl() throws MalformedURLException,
			IOException, ProtocolException {
		String urlName = "http://localhost:8080/TestRead/";
		URL url = new URL(urlName);
		
        // 设定连接的相关参数
        HttpURLConnection connection= (HttpURLConnection) url.openConnection();
        System.out.println(connection);
        connection.setDoOutput(true);
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(60000*5);
//        OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
//        
//        // 向服务端发送key = value对
//        out.write("data=");
//        out.flush();
//        out.close();
        
        // 获取服务端的反馈
        String strLine="";
        String strResponse ="";
        InputStream in =connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        while((strLine =reader.readLine()) != null)
        {
                 strResponse +=strLine +"\n";
                 System.out.println(strResponse);
        }
	}

}
