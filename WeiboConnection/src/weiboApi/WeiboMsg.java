package weiboApi;

import weibo4j.model.*;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Date;
import java.util.ArrayList;

public class WeiboMsg  implements Serializable{
	private static final long serialVersionUID = 834527962046630178L;
	private User user = null; 
	private Date createdat = null;
	private double longitude=-1,latitude=-1;
	private HashMap<String,String> msgInfo = new HashMap<String,String>();
	private String geoPosition="";
	
	public void setUserInfo(User pUser){
		this.user = pUser;
	}
	public User getUserInfo(){
		return this.user;
	}
	
	public void setMsgInfo(String pInfoName,String pInfoValue){
		msgInfo.put(pInfoName, pInfoValue);
	}
	public String getMsgInfo(String pInfoName){
		String pInfoValue = msgInfo.get(pInfoName);
		if(pInfoValue!=null){
			return pInfoValue;
		}else{
			return "";
		}
	}
	public void setGeo(double pLongitude,double pLatitude){
		this.longitude=pLongitude;
		this.latitude=pLatitude;
	}	
	public ArrayList<Double> getGeo(){
		ArrayList<Double> geo = new ArrayList<Double>();
		geo.add(longitude);
		geo.add(latitude);
		return geo;
	}
	public double getLongitude() {
		return longitude;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setGeoPosition(String pPosition){
		this.geoPosition = pPosition;
	}
	public final String getGeoPosition(){
		return this.geoPosition;
	}
	public void setCreatedTime(Date dt){
		this.createdat = dt;
	}
	public Date getCreatedTime(){
		return this.createdat;
	}
	
	public Timestamp getTimestamp(){
		return new Timestamp(createdat.getTime());
	}
	
	public String toString(){
		String info="";
		
		info="user="+this.user.toString()+";msg="+this.msgInfo.toString()
			+";Geo="+this.geoPosition+";lat="+this.latitude+";lon="+this.longitude;
		
		return info;
	}
}
