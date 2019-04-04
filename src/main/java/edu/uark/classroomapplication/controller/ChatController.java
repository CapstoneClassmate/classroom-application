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

@Controller
public class ChatController {

	private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);
    
	@Autowired
    private SimpMessageSendingOperations messagingTemplate;
    @Autowired 
    JdbcTemplate database;
    
    ArrayList<Room> allRooms = new ArrayList<Room>();
    
    
    @MessageMapping("/chat.createRoom")
    public Room createRoom(@Payload Room room) {
    	logger.info("Room created.");
    	logger.info(room.getRoomName());
    	// Add the host as a member of the room
    	allRooms.add(room);
    	
    	return room;
    }
    
    
	@MessageMapping("/chat.sendMessage")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        messagingTemplate.convertAndSend("/room/" + chatMessage.getRoomName(), chatMessage);
        
        if(chatMessage.getRole().equals("host")) {
        	for (Room r : allRooms) {		
        		if(r.getRoomName().equals(chatMessage.getRoomName())) {
        			for(User u : r.getUsers())  {
        				System.out.println("/room/" + chatMessage.getRoomName() + "/" + u.getUsername());
        				messagingTemplate.convertAndSend("/room/" + chatMessage.getRoomName() + "/" + u.getUsername(), chatMessage);
        			}
        		}
        	}

        } else if (chatMessage.getRole().equals("member")) {
        	messagingTemplate.convertAndSend("/room/" + chatMessage.getRoomName() + "/" + chatMessage.getSender(), chatMessage);
        }
        
        return chatMessage;
    }

    @MessageMapping("/chat.addUser")
    public ChatMessage addUser(@Payload ChatMessage chatMessage) {
    	logger.info("User added to room " + chatMessage.getRoomName() + " with name " + chatMessage.getSender());
        logger.info(chatMessage.getRoomName());
        
        for(Room r : allRooms) {
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

        for (int i = 0; i < allRooms.size(); i++) {
            Room r = allRooms.get(i);
            if (r.getRoomName().equals(chatMessage.getRoomName())) {
                ArrayList<User> users = allRooms.get(i).getUsers();
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

        if (chatMessage.getRole().equals("user")) {
            for (Room r : allRooms) {
                if (r.getRoomName().equals(chatMessage.getRoomName())) {
                    for (User u : r.getUsers()) {
                        messagingTemplate.convertAndSend("/room/" + chatMessage.getRoomName() + "/" + u.getUsername(), chatMessage);
                        messagingTemplate.convertAndSend("/room/" + chatMessage.getRoomName() + "/" + chatMessage.getSender(), chatMessage);
                    }
                }
            }
        } else if (chatMessage.getRole().equals("host")) {
            for (Room r : allRooms) {
                if (r.getRoomName().equals(chatMessage.getRoomName())) {
                    messagingTemplate.convertAndSend("/room/" + chatMessage.getRoomName() + "/" + r.getHost(), chatMessage);
                    messagingTemplate.convertAndSend("/room/" + chatMessage.getRoomName() + "/" + chatMessage.getSender(), chatMessage);
                }
            }
        }

        return chatMessage;
    }

    @MessageMapping("/chat.removeAllUsers")
    public void removeAllUsers(@Payload ChatMessage chatMessage) {
        logger.info("All users removed from room " + chatMessage.getRoomName() + " with host " + chatMessage.getSender());
        logger.info(chatMessage.getRoomName());

        for (Room room : allRooms) {
            if (room.getRoomName().equals(chatMessage.getRoomName())) {
                room.removeAllUsers();
            }
        }
    }

    @MessageMapping("/chat.terminateRoom")
    public void terminateRoom(@Payload ChatMessage chatMessage) {
        messagingTemplate.convertAndSend("/room/" + chatMessage.getRoomName(), chatMessage);

        // Remove room from allRooms list
        allRooms.removeIf((Room r) -> r.getRoomName().equals(chatMessage.getRoomName()));
    }
}
