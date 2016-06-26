package weibot;

import java.util.LinkedList;
import java.util.HashSet;
import java.util.Queue;

public class MsgQueue {
	int maxLimit;
	Queue<String> msgQueue = new LinkedList<String>();
	HashSet<String> msgHash = new HashSet<String>(); 
	public MsgQueue(int pMaxLimit){
		maxLimit = pMaxLimit;
	}
	public int msgCnt(){
		return msgQueue.size();
	}
	
	public boolean isNewOne(String mid){
		boolean newMsg=false;
		if(msgHash.contains(mid)){
			newMsg = false;
		}else{
			msgHash.add(mid);
			msgQueue.add(mid);
			
			if(msgQueue.size()>maxLimit){
				String tmid = msgQueue.poll();
				msgHash.remove(tmid);
				tmid=null;
			}
			
			newMsg = true;
		}
		return newMsg;
	}
	
}
