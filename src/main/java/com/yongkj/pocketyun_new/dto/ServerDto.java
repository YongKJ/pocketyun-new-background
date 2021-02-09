package com.yongkj.pocketyun_new.dto;

import java.io.Serializable;

public class ServerDto implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String serverUUID;
	private String accessKeyId;
	private String secretAccessKey;
	private String endPiont;
	private String endPiontInternal;
	private String bucketName;
	
	public String getServerUUID() {
		return serverUUID;
	}
	
	public void setServerUUID(String serverUUID) {
		this.serverUUID = serverUUID;
	}

	public String getAccessKeyId() {
		return accessKeyId;
	}
	
	public void setAccessKeyId(String accessKeyId) {
		this.accessKeyId = accessKeyId;
	}
	
	public String getSecretAccessKey() {
		return secretAccessKey;
	}
	
	public void setSecretAccessKey(String secretAccessKey) {
		this.secretAccessKey = secretAccessKey;
	}

	public String getEndPiont() {
		return endPiont;
	}

	public void setEndPiont(String endPiont) {
		this.endPiont = endPiont;
	}

	public String getEndPiontInternal() {
		return endPiontInternal;
	}

	public void setEndPiontInternal(String endPiontInternal) {
		this.endPiontInternal = endPiontInternal;
	}

	public String getBucketName() {
		return bucketName;
	}

	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}

}
