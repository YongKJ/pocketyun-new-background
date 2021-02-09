package com.yongkj.pocketyun_new.dto;

import java.io.Serializable;

public class PathsDto implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String pathsUUID;
	private String userUUID;
	private String path;
	private String filename;
	private String size;
	private int depth;
	private String addTime;
	private String modTime;
	private String deleteTime;
	
	public String getDeleteTime() {
		return deleteTime;
	}

	public void setDeleteTime(String deleteTime) {
		this.deleteTime = deleteTime;
	}

	public String getPathsUUID() {
		return pathsUUID;
	}
	
	public void setPathsUUID(String pathsUUID) {
		this.pathsUUID = pathsUUID;
	}
	
	public String getUserUUID() {
		return userUUID;
	}
	
	public void setUserUUID(String userUUID) {
		this.userUUID = userUUID;
	}
	
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	public String getSize() {
		return size;
	}
	
	public void setSize(String size) {
		this.size = size;
	}
	
	public int getDepth() {
		return depth;
	}
	
	public void setDepth(int depth) {
		this.depth = depth;
	}
	
	public String getAddTime() {
		return addTime;
	}
	
	public void setAddTime(String addTime) {
		this.addTime = addTime;
	}
	
	public String getModTime() {
		return modTime;
	}
	
	public void setModTime(String modTime) {
		this.modTime = modTime;
	}

}
