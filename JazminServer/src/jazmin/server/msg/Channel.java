/**
 * 
 */
package jazmin.server.msg;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yama
 * 26 Dec, 2014
 */
public class Channel {
	String id;
	Map<String,Session>sessions;
	long createTime;
	Object userObject;
	MessageServer messageServer;
	//
	Channel(MessageServer messageServer,String id) {
		this.id=id;
		this.messageServer=messageServer;
		this.sessions=new ConcurrentHashMap<>();
		createTime=System.currentTimeMillis();
	}
	//--------------------------------------------------------------------------
	//public interface
	/**
	 * return id of this channel
	 */
	public String id(){
		return id;
	}
	/**
	 * add session to this channel
	 */
	public void addSession(Session session){
		if(session.principal==null){
			throw new IllegalArgumentException("principal can not be null.");
		}
		session.enterChannel(this);
		sessions.put(session.principal,session);
	}
	/** 
	 *remove session from channel by session's principal
	 */
	public Session removeSession(String principal){
		Session s=sessions.remove(principal);
		if(s!=null){
			s.leaveChannel(this);
		}
		return s;
	}
	/**
	 *remove all session from channel.
	 */
	public List<Session>removeAllSessions(){
		List<Session>allSessions=new ArrayList<>(sessions.values());
		allSessions.forEach(s->s.leaveChannel(this));
		sessions.clear();
		return allSessions;
	}
	/**
	 *get all session in this channel.
	 */
	public List<Session>sessions(){
		List<Session>allSessions=new ArrayList<>(sessions.values());
		return allSessions;
	}
	/** 
	 *get session by principal
	 */
	public Session sessionByPrincipal(String principal){
		if(principal==null){
			throw new IllegalArgumentException("principal can not be null.");
		}
		return sessions.get(principal);
	}
	/**
	 *destroy this channel.
	 */
	public void destroy(){
		removeAllSessions();
		messageServer.removeChannelInternal(id);
	}
	/**
	 *get channel create time.
	 */
	public long createTime(){
		return createTime;
	}
	/** 
	 *broadcast message to all sessions in this channel.
	 */
	public void broadcast(String serviceId,Object payload){
		sessions.values().forEach(s->{
			if(s.isActive()){
				s.push(serviceId, payload);
			}
		});
	}
	/**
	 *broadcast message to all sessions in this channel expect session in blockPrincipalSet.
	 */
	public void broadcast(String serviceId,Object payload,Set<String>blockPrincipalSet){
		sessions.values().forEach(s->{
			if(s.isActive()){
				if(!blockPrincipalSet.contains(s.principal)){
					s.push(serviceId, payload);
				}
			}
		});
	}
	/** 
	 *get user object 
	 */
	public Object userObject() {
		return userObject;
	}
	/** 
	 *set user object
	 */
	public void userObject(Object userObject) {
		this.userObject = userObject;
	}
}
