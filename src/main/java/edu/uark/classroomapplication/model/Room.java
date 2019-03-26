package edu.uark.classroomapplication.model;

import java.util.ArrayList;

public class Room {

	private String roomName;
	private User host;
	private ArrayList<User> users;
	private ArrayList<ChatMessage> messages;
	
	public Room() {
		roomName = "ERROR";
		host = new User("ERROR");
	}
	
	public Room(String roomName, User host) {
		this.setRoomName(roomName);
		this.setHost(host);
		users = new ArrayList<User>();
		messages = new ArrayList<ChatMessage>();
	}
	
	public void addUser(User u) {
		if(!users.contains(u)) {
			users.add(u);
		} else {
			// Send some message to the server that the user is already in the room.
		}
	}
	
	public void removeUser(User u) {
		if(users.contains(u)) {
			users.remove(u);
		} else {
			// Send some message to the server that the user was not in the room.
		}
	}
	
	public void addMessage(ChatMessage m) {
		messages.add(m);
	}

	public String getRoomName() {
		return roomName;
	}

	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}

	public User getHost() {
		return host;
	}

	public void setHost(User host) {
		this.host = host;
	}
	
}
