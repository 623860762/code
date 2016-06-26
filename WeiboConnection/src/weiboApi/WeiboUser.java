package weiboApi;

import java.util.*;
import weibo4j.org.json.JSONArray;

public class WeiboUser {
	HashMap<String,String> userInfo = new HashMap<String,String>();
	
	public void setUserInfo(String pInfoName,String pInfoValue){
		userInfo.put(pInfoName, pInfoValue);
	}

	public String getUserInfo(String pInfoName){
		String infoValue = userInfo.get(pInfoName);
		if(infoValue==null){
			return "";
		}else{
			return infoValue;
		}
	}
	
	public String getUserID(){
		return getUserInfo("id");
	}
	public String getUserName(){
		return getUserInfo("screenName");
	}
	
	
}
