package postTest;
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
		String urlName = "http://www.baidu.com/s?wd=%E5%93%88%E5%93%88%E5%93%88%E5%93%88&rsv_bp=0&ch=&tn=monline_5_dg&bar=&rsv_spt=3&ie=utf-8&rsv_sug3=1&rsv_sug=0&rsv_sug1=1&rsv_sug4=40&inputT=2816";
		URL url = new URL(urlName);

        // 设定连接的相关参数
        HttpURLConnection connection= (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("GET");
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
                 System.out.print(strResponse);
        }
	}

}
