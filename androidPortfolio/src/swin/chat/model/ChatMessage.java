package swin.chat.model;

import java.util.Date;

public class ChatMessage {
	
	private String toUser;
	private String fromUser;
	private String messageId;
	private Date dateTime;
	private String message;
	
	public String getMessage() {
		return message;
	}
	
	public Date getDateTime() {
		return dateTime;
	}
	
	public String getFromUser() {
		return fromUser;
	}
	
	public String getToUser() {
		return toUser;
	}
	
	public String getMessageId() {
		return messageId;
	}
	
	public ChatMessage setDateTime(Date dateTime) {
		this.dateTime = dateTime;
		return this;
	}
	
	public ChatMessage setFromUser(String fromUser) {
		this.fromUser = fromUser;
		return this;
	}
	
	public ChatMessage setMessage(String message) {
		this.message = message;
		return this;
	}
	
	public ChatMessage setMessageId(String messageId) {
		this.messageId = messageId;
		return this;
	}
	
	public ChatMessage setToUser(String toUser) {
		this.toUser = toUser;
		return this;
	}
}
