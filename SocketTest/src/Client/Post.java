package Client;


import it.sauronsoftware.base64.Base64;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Timer;

import javax.net.ssl.HttpsURLConnection;



import weibo4j.org.json.JSONArray;
import weibo4j.org.json.JSONException;
import weibo4j.org.json.JSONObject;

public class Post {
	
	public JSONArray jsonArray = new JSONArray();
	public JSONArray getJsonArray() {
		return jsonArray;
	}

	public void setJsonArray(JSONArray jsonArray) {
		this.jsonArray = jsonArray;
	}
	//内部类
	 private class ReadJson extends java.util.TimerTask{
			@Override
			public void run() {
				System.out.println(jsonArray+"当前时间"+System.currentTimeMillis());
			}
		}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		
		Post post = new Post();
//		Timer timer = new Timer();
//		timer.schedule(post.new ReadJson(), 1000, 2000);
		post.getDataFromUrl();
	}

	private  void getDataFromUrl() throws Exception {
//		String urlName = "http://localhost:8080/readHttp/";
		//证书，创建SSLContext对象，并使用我们指定的信任管理器初始化 
		
		String urlName = "https://m.api.weibo.com/2/messages/receive.json?source=2544665843&uid=3306169867";
		URL url = new URL(urlName);
		String authStr = "misiwbot@sina.cn"+":"+"sina62675663";
		String authEncoded = Base64.encode(authStr);
        // 设定连接的相关参数
//		HttpURLConnection connection= (HttpURLConnection) url.openConnection();
		HttpsURLConnection  connection= (HttpsURLConnection ) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("Authorization", "Basic " + authEncoded);
        connection.setAllowUserInteraction(true); 
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(1000*60);
        System.out.println(connection);
//        OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
//        
//        // 向服务端发送key = value对
//        out.write("data=");
//        out.flush();
//        out.close();
        
        // 获取服务端的反馈
        long a = System.currentTimeMillis();
//        while(connection!=null){
        	long b = System.currentTimeMillis();
        	String strLine="";
        	String strResponse ="";
        	InputStream in =connection.getInputStream();
        	BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        	while((strLine =reader.readLine()) != null){
        		strResponse +=strLine +"\n";
        		JSONObject temp = null;
				try {
					temp = new JSONObject(strResponse);
					System.out.println("这个对象加入jsonArray"+temp);
					if(temp != null){
						this.jsonArray.put(temp);
						long c = System.currentTimeMillis();
						System.out.println("写入jsonArray时间"+c);
						System.out.println("json数组："+jsonArray);
						strResponse = "";
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
//					e.printStackTrace();
				}
        		System.out.println(strResponse);
        	}
//        	if(b-a>60000){//链接持续1min
//        		connection=null;
//        		System.out.println(b-a);
//        	}
//        }
//        System.out.println("链接断开......");
	}

}
