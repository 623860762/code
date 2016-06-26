package cert;


import it.sauronsoftware.base64.Base64;

import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.*;
import javax.net.ssl.*;


public class ReceiveMethod {
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
    public ReceiveMethod() {
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
    // be added to deal with a "HTTPS" URL.  All of the trustce,String uid,Str
    // management, verification, is handled by the HttpsURLConnection.
    public void requestUrl(String url,String username,String password,String source,String uid,String since_id,int outTime) throws Exception {
    	String urlName = url+"?source="+source+"&uid="+uid+"&since_id="+since_id;
		URL urlObject = new URL(urlName);
		String authStr = username+":"+password;
		String authEncoded = Base64.encode(authStr);
        // 设定连接的相关参数
		HttpsURLConnection  connection= (HttpsURLConnection) urlObject.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestProperty("Authorization", "Basic " + authEncoded);
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(outTime);
        System.out.println(connection);

        String strLine="";
        String strResponse ="";
        InputStream in =connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        while((strLine =reader.readLine()) != null){
        	strResponse +=strLine +"\n";
        	System.out.println(strResponse);
        	}
    }

    public static void main(String[] args) {
        ReceiveMethod receiveMethod = new ReceiveMethod();
        try {
        	receiveMethod.requestUrl("https://m.api.weibo.com/2/messages/receive.json", "misiwbot@sina.cn", "sina62675663", "2544665843", "3306169867", "", 120000);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
} 
