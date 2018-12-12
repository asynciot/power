package controllers.device;

import play.Logger;
import play.mvc.WebSocket.Out;

import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

public class SocketManager {
	private static SocketManager s_instance = null;
	
	private HashMap<String, HashMap<String, Out>> m_roomMap = null;
	public synchronized static SocketManager getInstance() {
		if (s_instance == null) {
			s_instance = new SocketManager();
		}
		
		return s_instance;
	}
	
	private SocketManager() {
		m_roomMap = new HashMap<String, HashMap<String, Out>>();
	}
	
	public Out getRoom(String roomName, String username) {
		if (m_roomMap.get(roomName) != null) {
			return m_roomMap.get(roomName).get(username);
		}
		return null;
	}
	
	public int getRoomSize(String roomName) {
		if (m_roomMap.get(roomName) != null) {
			return m_roomMap.get(roomName).size();
		}
		
		return 0;
	}
	
	public synchronized void joinRoom(String roomName, String username, Out out) {
		HashMap<String, Out> userOutMap = null;
		if (m_roomMap.get(roomName) == null) {
			userOutMap = new HashMap<String, Out>();
			m_roomMap.put(roomName, userOutMap);
		}
		else {
			userOutMap = m_roomMap.get(roomName);
		}
		
		userOutMap.put(username, out);
	}
	
	public void broadcast(String roomName, Object msg) {

		try {
			Date s=new Date();
			Logger.info("broadcast ok at "+s);
			if (m_roomMap.get(roomName) != null) {
				for (Entry<String, Out> entry : m_roomMap.get(roomName).entrySet()) {
					try {
						entry.getValue().write(msg);
					}
					catch (Throwable e1) {
						
					}
				}
			}
			
		}
		catch (Throwable e) {
			
		}
	}
	
	public void leave(String roomName, String username) {
		if (m_roomMap.get(roomName) != null) {
			m_roomMap.get(roomName).remove(username);
			
			if (m_roomMap.get(roomName).size() == 0) {
				m_roomMap.remove(roomName);
			}
		}	
	}
}