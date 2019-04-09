package edu.uark.classroomapplication.model;

public class ServerEvent {

	public String eventName;
	public Object eventObject;
	
	public ServerEvent(String eventName, Object eventObject) {
		this.eventName = eventName;
		this.eventObject = eventObject;
		
	}
	
	public ServerEvent() {
		this.eventName = "404";
	}
	
}
