package weibo4j;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.HashMap;

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

import weibo4j.model.WeiboException;
import weibo4j.http.StreamEvent;
import weibo4j.model.pingpang.*;
import weibo4j.org.json.JSONObject;
import weibo4j.util.WeiboConfig;

public class Pingpang {
	private Queue<MessageEvent> eventQueue = new LinkedList<MessageEvent>();
	private Queue<MessageImage> imageQueue = new LinkedList<MessageImage>();
	private Queue<MessageMention> mentionQueue = new LinkedList<MessageMention>();
	private Queue<MessageText> textQueue = new LinkedList<MessageText>();
	private boolean waiting = false;	//是否正在等待消息
	private PingpangEvent ppEvent = null;
	private HashMap<String,String> listenInfo = new HashMap<String,String>(); 
	private ConnectionThread listenThread = null;
	
	public final HashMap<String,String> getListenInfo(){
		return this.listenInfo;
	} 
	public Pingpang(){
		ppEvent = new PingpangEvent(this);
		listenThread = new ConnectionThread(this);
		
		ReceiveMethod();
	}
	
	public boolean listenMessage(String pUName, String pPasswd, Long uid ){
		return listenMessage(pUName,  pPasswd, uid, 0L, 300);
	}	
	public boolean listenMessage(String pUName, String pPasswd, Long uid, Long since_id){
		return listenMessage(pUName,  pPasswd, uid, since_id, 300);
	}	
	public boolean listenMessage(String pUName, String pPasswd, Long uid, Long since_id, int maxWaitSecond){
		if(!waiting){
			waiting = true; //监听标志位进入锁状态
			listenInfo.put("username", pUName.trim());
			listenInfo.put("password", pPasswd.trim());
			listenInfo.put("listenuid", uid.toString());
			listenInfo.put("msgsinceid", since_id.toString());
			listenInfo.put("maxwaiting", String.valueOf(maxWaitSecond*1000));
			
			listenThread.start(); //启动监听线程
			
			return true;  //本次监听真正启动
		}else{
			return false; //本次监听没有真正启动
		}
	}
	protected void doListenMessage(){
		try{
			String pUName = listenInfo.get("username");
			String pPasswd = listenInfo.get("password");
			String uid = listenInfo.get("listenuid");
			String since_id = listenInfo.get("msgsinceid");
			int maxWaitSecond = Integer.valueOf(listenInfo.get("maxwaiting"));
			execMessageRecive(pUName, pPasswd, uid, this.ppEvent, since_id, maxWaitSecond);			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	private  void execMessageRecive(String pUName, String pPasswd, String uid, StreamEvent pSEvent, String since_id, int maxWaitSecond) throws WeiboException {
		try{
			messageReceive(WeiboConfig.getValue("pingpangURL"),
					pUName, pPasswd, WeiboConfig.getValue("client_ID").trim(),
					uid, since_id, maxWaitSecond*1000, pSEvent
				);
		}catch(Exception e){
			e.printStackTrace();
		}
		waiting = false; //结束
	}
	
	public ArrayList<MessageEvent> getNewMessageEvents(){
		ArrayList<MessageEvent> msgs = new ArrayList<MessageEvent>();
		MessageEvent msg = this.eventQueue.poll();
		while(msg!=null){
			msgs.add(msg);
			msg = this.eventQueue.poll();
		}
		return msgs;
	}
	public ArrayList<MessageImage> getNewMessageImages(){
		ArrayList<MessageImage> msgs = new ArrayList<MessageImage>();
		MessageImage msg = this.imageQueue.poll();
		while(msg!=null){
			msgs.add(msg);
			msg = this.imageQueue.poll();
		}
		return msgs;
	}
	public ArrayList<MessageMention> getNewMessageMentions(){
		ArrayList<MessageMention> msgs = new ArrayList<MessageMention>();
		MessageMention msg = this.mentionQueue.poll();
		while(msg!=null){
			msgs.add(msg);
			msg = this.mentionQueue.poll();
		}
		return msgs;
	}
	public ArrayList<MessageText> getNewMessageTexts(){
		ArrayList<MessageText> msgs = new ArrayList<MessageText>();
		MessageText msg = this.textQueue.poll();
		while(msg!=null){
			msgs.add(msg);
			msg = this.textQueue.poll();
		}
		return msgs;
	}
	
	class ConnectionThread extends Thread {
		Pingpang mainPp = null;
		public ConnectionThread(Pingpang pPingpang){
			mainPp = pPingpang;
		}
		public void run(){
			mainPp.doListenMessage();
		}
	}
	
	class PingpangEvent extends StreamEvent{
		Pingpang mainPp = null;
		public PingpangEvent(Pingpang pPingpang){
			mainPp = pPingpang;
		}
		public boolean doSegment(String pSegment){
			boolean succ = false;
			try{
				JSONObject json = new JSONObject(pSegment);
				String type = json.getString("type"); 
				if(type.equals("event")){
					mainPp.eventQueue.add(new MessageEvent(json));
					succ = true;
				}else if(type.equals("text")){
					mainPp.textQueue.add(new MessageText(json));
					succ = true;					
				}else if(type.equals("mention")){
					mainPp.mentionQueue.add(new MessageMention(json));
					succ = true;					
				}else if(type.equals("image")){
					mainPp.imageQueue.add(new MessageImage(json));
					succ = true;					
				}else {
					//succ = false;
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			return succ;
		}
	}

	//----------------------------------------------------
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
    private void ReceiveMethod() {
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
    public void messageReceive(String url,String username,String password,String source,String uid,String since_id,int outTime,StreamEvent pSEvent) throws Exception {
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
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"));
        while((strLine =reader.readLine()) != null){
        	strResponse += strLine.trim() ;
        	System.out.println(strResponse);
        	if(pSEvent.doSegment(strResponse) ||!strResponse.startsWith("{") ){
        		strResponse="";
        	}
        }
    }	
}
