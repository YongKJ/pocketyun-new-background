package com.yongkj.pocketyun_new.dto;

import java.io.Serializable;

public class UserDto implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String userUUID;
	private String userName;
	private String password;
	private String regSex;
	private int regAge;
	private String regEmail;
	private String regTime;
	private String loginTime;
	private String admin;
	
	public String getUserUUID() {
		return userUUID;
	}
	
	public void setUserUUID(String userUUID) {
		this.userUUID = userUUID;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getRegSex() {
		return regSex;
	}
	
	public void setRegSex(String regSex) {
		this.regSex = regSex;
	}
	
	public int getRegAge() {
		return regAge;
	}
	
	public void setRegAge(int regAge) {
		this.regAge = regAge;
	}
	
	public String getRegEmail() {
		return regEmail;
	}
	
	public void setRegEmail(String regEmail) {
		this.regEmail = regEmail;
	}
	
	public String getRegTime() {
		return regTime;
	}
	
	public void setRegTime(String regTime) {
		this.regTime = regTime;
	}
	
	public String getLoginTime() {
		return loginTime;
	}
	
	public void setLoginTime(String loginTime) {
		this.loginTime = loginTime;
	}
	
	public String getAdmin() {
		return admin;
	}
	
	public void setAdmin(String admin) {
		this.admin = admin;
	}

}
