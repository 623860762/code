package cert;


import it.sauronsoftware.base64.Base64;

import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.*;
import java.util.*;
import javax.net.ssl.*;

import weibo4j.org.json.JSONArray;
import weibo4j.org.json.JSONException;
import weibo4j.org.json.JSONObject;

public class Copy_2_of_HttpsTest {
	public JSONArray jsonArray = new JSONArray();
    private String url = "https://m.api.weibo.com/2/messages/receive.json?source=2544665843&uid=3306169867";
    private X509TrustManager xtm = new X509TrustManager() {
        public void checkClientTrusted(X509Certificate[] chain, String authType) {}

            public void checkServerTrusted(X509Certificate[] chain, String authType) {
//                System.out.println("cert: " + chain[0].toString() + ", authType: " + authType);
            }

            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
    }; 

    // Create an class to trust all hosts
    private HostnameVerifier hnv = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            System.out.println("hostname: " + hostname);
            return true;
        }
    }; 

    // In this function we configure our system with a less stringent
    // hostname verifier and X509 trust manager.  This code is
    // executed once, and calls the static methods of HttpsURLConnection
    public Copy_2_of_HttpsTest() {
        // Initialize the TLS SSLContext with
        // our TrustManager
        SSLContext sslContext = null;

        try {
            sslContext = SSLContext.getInstance("TLS");
            X509TrustManager[] xtmArray = new X509TrustManager[] { xtm };
            sslContext.init(null, xtmArray, new java.security.SecureRandom());
        } catch(GeneralSecurityException gse) {
            // Print out some error message and deal with this exception
        }

        // Set the default SocketFactory and HostnameVerifier
        // for javax.net.ssl.HttpsURLConnection
        if(sslContext != null) {
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        }

        HttpsURLConnection.setDefaultHostnameVerifier(hnv);
    }

    // This function is called periodically, the important thing
    // to note here is that there is no special code that needs to
    // be added to deal with a "HTTPS" URL.  All of the trust
    // management, verification, is handled by the HttpsURLConnection.
    public void run() throws Exception {
    	
		URL urlObject = new URL(url);
		String authStr = "misiwbot@sina.cn"+":"+"sina62675663";
		String authEncoded = Base64.encode(authStr);
        // 设定连接的相关参数
//		HttpURLConnection connection= (HttpURLConnection) url.openConnection();
		HttpsURLConnection  connection= (HttpsURLConnection) urlObject.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestProperty("Authorization", "Basic " + authEncoded);
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
        	System.out.println(b);
        	BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        	while((strLine =reader.readLine()) != null){
        		strResponse +=strLine +"\n";
        		System.out.println(strResponse);
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
    }

    public static void main(String[] args) {
        Copy_2_of_HttpsTest httpsTest = new Copy_2_of_HttpsTest();
        try {
			httpsTest.run();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
} 
