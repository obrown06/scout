package socket;

import java.util.HashMap;
import java.io.IOException;

import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import scout.ScoutingResultsRecord;
import scout.URLScoutController;
 
/** 
 * 
 */
@ServerEndpoint(
		value = "/scout", 
		encoders = { ScoutingResultsRecordEncoder.class }
) 
public class ScoutServer {
	
	private HashMap<Session, URLScoutController> sessionsToControllers = new HashMap<Session, URLScoutController>(); 
	private HashMap<URLScoutController, Session> controllersToSessions = new HashMap<URLScoutController, Session>(); 
    /**
     * @OnOpen allows us to intercept the creation of a new session.
     * The session class allows us to send data to the user.
     * In the method onOpen, we'll let the user know that the handshake was 
     * successful.
     */
    @OnOpen
    public void onOpen(Session session){
        System.out.println(session.getId() + " has opened a connection"); 
        try {
            session.getBasicRemote().sendText("Connection Established");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
 
    /**
     * When a user sends a message to the server, this method will intercept the message
     * and allow us to react to it. For now the message is read as a String.
     */
    @OnMessage
    public void onMessage(String message, Session session){
        System.out.println("Message from " + session.getId() + ": " + message);
        
        if (this.sessionsToControllers.containsKey(session)) {
        	this.removeAndShutdownAssociatedController(session);
        }
        
        URLScoutController controller = new URLScoutController(this, message);
        
        this.sessionsToControllers.put(session, controller);
        this.controllersToSessions.put(controller, session);
        
        Thread thread = new Thread(controller);
        thread.start();
    }
    
    private void removeAndShutdownAssociatedController(Session session) {

    	URLScoutController controller = this.sessionsToControllers.get(session);
    	this.sessionsToControllers.remove(session);
    	this.controllersToSessions.remove(controller);
        
        if (controller != null) {
        	controller.shutdown();
        }
    }
    
	private void closeSession(Session session) {
		try {
	        session.close();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
 
    /**
     * The user closes the connection.
     * 
     * Note: you can't send messages to the client from this method
     */
    @OnClose
    public void onClose(Session session){
        System.out.println("Session " +session.getId()+" has ended");
        
        this.closeSession(session);
        this.removeAndShutdownAssociatedController(session);
    }

	
	public void sendMessage(URLScoutController controller, ScoutingResultsRecord record) {
		Session session = this.controllersToSessions.get(controller);
		
		if (!controller.isActive) {
			return;
		}
		
		try {
			session.getBasicRemote().sendObject(record);
			
		} catch (EncodeException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
