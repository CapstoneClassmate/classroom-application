package edu.uark.classroomapplication.model;

import java.util.UUID;

public class User {

	private String username;
	private String id;
	
	public User(String username) {
		setId(UUID.randomUUID().toString());
		this.setUsername(username);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
