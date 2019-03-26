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
    	// Add the host as a member of the room
    	room.addUser(room.getHost());
    	allRooms.add(room);
    	
    	return room;
    }
    
    
	@MessageMapping("/chat.sendMessage")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        logger.info(chatMessage.getContent());

        if(chatMessage.getRole() == "host") {
        	Room r;
        
        	
        } else if (chatMessage.getRole() == "member") {
        	
        	
        }
        
        
        messagingTemplate.convertAndSend("/room/" + chatMessage.getRoomNumber(), chatMessage);
        messagingTemplate.convertAndSend("/room/" + chatMessage.getRoomNumber() + "/" + chatMessage.getSender(), chatMessage);
        
        // Add message to the database. 
        return chatMessage;
    }

    @MessageMapping("/chat.addUser")
    public ChatMessage addUser(@Payload ChatMessage chatMessage) {
        logger.info(chatMessage.getContent());

        messagingTemplate.convertAndSend("/room/" + chatMessage.getRoomNumber(), chatMessage);
        messagingTemplate.convertAndSend("/room/" + chatMessage.getRoomNumber() + "/" + chatMessage.getSender(), chatMessage);
        
        
        
        return chatMessage;
    }

}
