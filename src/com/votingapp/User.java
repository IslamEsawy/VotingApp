package com.votingapp;

import java.io.Serializable;

@SuppressWarnings("serial")
public class User implements Serializable {
	

	private String username;
	private int id;
	private String email;
	private String password;
	private String gender;

	public User() {
	}

	public User(String username, int id, String email, String password,
			String gender) {
		super();
		this.username = username;
		this.id = id;
		this.email = email;
		this.password = password;
		this.gender = gender;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	@Override
	public boolean equals(Object object) {
		boolean sameSame = false;
		if (object != null && object instanceof User)
			sameSame = (this.email.equals(((User) object).email));
		return sameSame;
	}

}
