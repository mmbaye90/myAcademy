import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.google.gson.Gson;

@ServerEndpoint("/ws/users")
public class UserServiceWebSocket {
	
	private static final Map<String,Session> sessions = Collections.synchronizedMap(new HashMap<>());
	
	@OnOpen
	public void onOpen(Session session) {
		
		String userId = session.getId();
		
		sessions.put(userId, session);
		
		System.out.println("Connexion with id : " + userId);
		
	};
	
	
	@OnMessage
	public void onMessage(String message,Session session) {
		
		System.out.println("OnMessage triggered.");
		System.out.println("Contenu du Message => " + message + " " + session.getId());
		
		try {
			
			session.getBasicRemote().sendText(new Gson().toJson("Messe reçu de "));
			
		} catch (Exception e) {
			// TODO: handle exception
		}
	};
	
//	============================= END ========================

}
