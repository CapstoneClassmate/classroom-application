package edu.uark.classroomapplication.model;

public class ChatMessage {
    private MessageType type;
    private String content;
    private String sender;
    private String roomName;
    private String role;

    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE,
        ERROR
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

	public String getRoomName() {
		return roomName;
	}

	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
}
