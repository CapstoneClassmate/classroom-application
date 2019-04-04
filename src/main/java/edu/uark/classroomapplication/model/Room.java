package edu.uark.classroomapplication.model;

import java.util.ArrayList;

public class Room {

	private String roomName;
	private User host;
	private ArrayList<User> users;
	private ArrayList<ChatMessage> messages;
	
	public static ArrayList<Room> allRooms = new ArrayList<Room>();
	
	public Room() {
		roomName = "ERROR";
		host = new User("ERROR");
		users = new ArrayList<User>();
		messages = new ArrayList<ChatMessage>();
	}
	
	public Room(String roomName, User host) {
		this.setRoomName(roomName);
		this.setHost(host);
		users = new ArrayList<User>();
		messages = new ArrayList<ChatMessage>();
	}
	
	public void addUser(User u) {
		users.add(u);
	}
	
	public void removeUser(String u) {
		for(User x : users) {
			if (x.getUsername().equals(u)) {
				users.remove(x);
				break;
			} else {
				// Send some message to the server that the user was not in the room.
				System.out.println("ERROR: User not in room!!!");
			}
		}
	}

	public void removeAllUsers() {
		users.clear();
	}
	
	public ArrayList<User> getUsers() {
		return users;
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
