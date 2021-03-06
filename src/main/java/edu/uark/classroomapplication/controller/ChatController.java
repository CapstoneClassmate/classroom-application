package edu.uark.classroomapplication.controller;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import edu.uark.classroomapplication.model.ChatMessage;
import edu.uark.classroomapplication.model.Room;
import edu.uark.classroomapplication.model.User;
import edu.uark.classroomapplication.model.ChatMessage.MessageType;

@Controller
public class ChatController {

	private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);
    
	@Autowired
    private SimpMessageSendingOperations messagingTemplate;
    @Autowired 
    JdbcTemplate database;
    

    /*
     * This method sends messages to all needed parties. 
     * If the user is a host it will send a message to all the members in the classroom. 
     * If the user is a member it will send the message to the hosts chat room which only they will be able to see.
     */
	@MessageMapping("/chat.sendMessage")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
		
		// Send the message to admin room
        messagingTemplate.convertAndSend("/room/" + chatMessage.getRoomName(), chatMessage);
        
        // Send the message to all the users in the room, if the host sends the message.
        if(chatMessage.getRole().equals("host")) {
        	for (Room r : Room.allRooms) {		
        		if(r.getRoomName().equals(chatMessage.getRoomName())) {
        			for(User u : r.getUsers()) {
        				System.out.println("/room/" + chatMessage.getRoomName() + "/" + u.getUsername());
        				messagingTemplate.convertAndSend("/room/" + chatMessage.getRoomName() + "/" + u.getUsername(), chatMessage);
        			}
        		}
        	}
        // Send the message to the user's own room if user is a member. 
        } else if (chatMessage.getRole().equals("member")) {
        	messagingTemplate.convertAndSend("/room/" + chatMessage.getRoomName() + "/" + chatMessage.getSender(), chatMessage);
        }
        
        return chatMessage;
    }

    @MessageMapping("/chat.addUser")
    public ChatMessage addUser(@Payload ChatMessage chatMessage) {
    	logger.info("User added to room " + chatMessage.getRoomName() + " with name " + chatMessage.getSender());
        logger.info(chatMessage.getRoomName());
        
        for(Room r : Room.allRooms) {
        	if(r.getRoomName().equals(chatMessage.getRoomName())) {
        		r.addUser(new User(chatMessage.getSender()));
        	}
        }
       
        return chatMessage;
    }

    @MessageMapping("/chat.removeUser")
    public ChatMessage removeUser(@Payload ChatMessage chatMessage) {
        logger.info("User removed from room " + chatMessage.getRoomName() + " with name " + chatMessage.getSender());
        logger.info(chatMessage.getRoomName());

        for (int i = 0; i < Room.allRooms.size(); i++) {
            Room r = Room.allRooms.get(i);
            if (r.getRoomName().equals(chatMessage.getRoomName())) {
                ArrayList<User> users = Room.allRooms.get(i).getUsers();
                for (int j = 0; j < users.size(); j++) {
                    User u = users.get(j);
                    if (u.equals(chatMessage.getSender())) {
//                        messagingTemplate.convertAndSend("/room/" + chatMessage.getRoomName() + "/" + u, chatMessage);
//                        messagingTemplate.convertAndSend("/room/" + chatMessage.getRoomName() + "/" + chatMessage.getSender(), chatMessage);
                        r.removeUser(u.getUsername());
                    }
                }
            }
        }

        return chatMessage;
    }

    @MessageMapping("/chat.userLeft")
    public ChatMessage userLeft(@Payload ChatMessage chatMessage) {
        messagingTemplate.convertAndSend("/room/" + chatMessage.getRoomName(), chatMessage);
        
        if(chatMessage.getType() == MessageType.LEAVE) {
	        if (chatMessage.getRole().equals("member")) 
	        	messagingTemplate.convertAndSend("/room/" + chatMessage.getRoomName() + "/" + chatMessage.getSender(), chatMessage);
	        else if (chatMessage.getRole().equals("host")) 
	            for (Room r : Room.allRooms) 
	                if (r.getRoomName().equals(chatMessage.getRoomName())) {
	                	for(User u : r.getUsers()) {
	                		messagingTemplate.convertAndSend("/room/" + chatMessage.getRoomName() + "/" + u.getUsername(), chatMessage);
	                	}
	                }
	        return chatMessage;
        } else {
        	throw new RuntimeException("Your going to the wrong route. You want to " + chatMessage.getType() + ", but this route is for leaving");
        }
        
    }

    @MessageMapping("/chat.removeAllUsers")
    public void removeAllUsers(@Payload ChatMessage chatMessage) {
        logger.info("All users removed from room " + chatMessage.getRoomName() + " with host " + chatMessage.getSender());
        logger.info(chatMessage.getRoomName());

        for (Room room : Room.allRooms) {
            if (room.getRoomName().equals(chatMessage.getRoomName())) {
                room.removeAllUsers();
            }
        }
    }

    @MessageMapping("/chat.terminateRoom")
    public void terminateRoom(@Payload ChatMessage chatMessage) {
    	logger.info("Terminating room " + chatMessage.getRoomName());
        messagingTemplate.convertAndSend("/room/" + chatMessage.getRoomName(), chatMessage);

        // Remove room from allRooms list
        Room.allRooms.removeIf((Room r) -> r.getRoomName().equals(chatMessage.getRoomName()));
    }
}
