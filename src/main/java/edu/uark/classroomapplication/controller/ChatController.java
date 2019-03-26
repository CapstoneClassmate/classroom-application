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

}
