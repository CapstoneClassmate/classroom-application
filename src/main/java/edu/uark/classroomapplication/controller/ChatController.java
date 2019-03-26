package edu.uark.classroomapplication.controller;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import edu.uark.classroomapplication.model.ChatMessage;
import edu.uark.classroomapplication.model.User;

@Controller
public class ChatController {

	private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);
    
	@Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @Autowired JdbcTemplate database;
    
	@MessageMapping("/chat.sendMessage")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        logger.info(chatMessage.getContent());

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
        
        
        // Create a user, and add them to the database. 
        User u = new User(chatMessage.getSender());
        database.execute("INSERT INTO users(id, username) values " + u.getId() + ", " + u.getUsername());
        List<User> users = database.query("SELECT * FROM users", 
        		new RowMapper<User>() {
            		public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            			User u = new User(rs.getString("username"));
            			u.setId(rs.getString("id"));
            			return u;
            		}
        		});
        for(User k: users) {
        	logger.info(k.getUsername() + " " + k.getId());
        }
        
        
        
        return chatMessage;
    }

}
