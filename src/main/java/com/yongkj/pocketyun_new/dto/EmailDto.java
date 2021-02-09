package com.yongkj.pocketyun_new.dto;

import java.io.Serializable;

public class EmailDto implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String emailUUID;
	private String sendUserUUID;
	private String sendUserName;
	private String receiveUserUUID;
	private String receiveUserName;
	private String emailTitle;
	private String emailContent;
	private String sendTime;
	
	public String getSendUserName() {
		return sendUserName;
	}

	public void setSendUserName(String sendUserName) {
		this.sendUserName = sendUserName;
	}

	public String getReceiveUserName() {
		return receiveUserName;
	}

	public void setReceiveUserName(String receiveUserName) {
		this.receiveUserName = receiveUserName;
	}

	public String getEmailUUID() {
		return emailUUID;
	}
	
	public void setEmailUUID(String emailUUID) {
		this.emailUUID = emailUUID;
	}
	
	public String getSendUserUUID() {
		return sendUserUUID;
	}
	
	public void setSendUserUUID(String sendUserUUID) {
		this.sendUserUUID = sendUserUUID;
	}
	
	public String getReceiveUserUUID() {
		return receiveUserUUID;
	}
	
	public void setReceiveUserUUID(String receiveUserUUID) {
		this.receiveUserUUID = receiveUserUUID;
	}
	
	public String getEmailTitle() {
		return emailTitle;
	}
	
	public void setEmailTitle(String emailTitle) {
		this.emailTitle = emailTitle;
	}
	
	public String getEmailContent() {
		return emailContent;
	}
	
	public void setEmailContent(String emailContent) {
		this.emailContent = emailContent;
	}
	
	public String getSendTime() {
		return sendTime;
	}
	
	public void setSendTime(String sendTime) {
		this.sendTime = sendTime;
	}

}
