package swin.chat.model;

import java.io.Serializable;

public class User implements Serializable{
	
	private String name;
	
	public User setName(String name) {
		this.name = name;
		return this;
	}
	
	public String getName() {
		return name;
	}
	
}
