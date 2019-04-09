package edu.uark.classroomapplication.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import edu.uark.classroomapplication.model.ChatMessage;
import edu.uark.classroomapplication.model.Room;
import edu.uark.classroomapplication.model.ServerEvent;

@Controller
public class RoomController {
	private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);
	@Autowired
    private SimpMessageSendingOperations messagingTemplate;
	
	
	
    @MessageMapping("/chat.createRoom")
    public Room createRoom(@Payload Room room) {
    	
    	// Check if the room has already been made
    	for(Room r : Room.allRooms) {
    		if(r.getRoomName().equals(room.getRoomName())) {
    			logger.info("Duplicate room name " + r.getRoomName());
    			// Send message to server and restart process.
    			ChatMessage cm = new ChatMessage();
    			cm.setType(ChatMessage.MessageType.ERROR);
    			cm.setContent("Duplicate room detected.");
    			messagingTemplate.convertAndSend("/room/" + r.getRoomName(), cm);
    			return null;
    		}
    	}
    	// If the room does not exist we will get to here and create it.
    	
    	logger.info("Room created.");
    	logger.info(room.getRoomName());
    	Room.allRooms.add(room);
    	return room;
    }
    
    
    
    
}
