package edu.uark.classroomapplication.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import edu.uark.classroomapplication.model.Room;

@Controller
public class RoomController {
	private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);
	
	
    @MessageMapping("/chat.createRoom")
    public Room createRoom(@Payload Room room) {
    	logger.info("Room created.");
    	logger.info(room.getRoomName());
    	// Add the host as a member of the room
    	Room.allRooms.add(room);
    	
    	return room;
    }

}
