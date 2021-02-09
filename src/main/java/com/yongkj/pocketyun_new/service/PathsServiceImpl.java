package com.yongkj.pocketyun_new.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yongkj.pocketyun_new.dto.PathsDto;
import com.yongkj.pocketyun_new.mapper.PbPathsMapper;


@Service("pathsService")
public class PathsServiceImpl implements PathsService {
	
	@Autowired
	private PbPathsMapper pbPathsMapper;
	
	
	public void addPaths(PathsDto pathsDto) {
		pbPathsMapper.addPaths(pathsDto);
	}
	
	
	public void delPathsByPathsUUID(String pathsUUID) {
		pbPathsMapper.delPathsByPathsUUID(pathsUUID);
	}
	
	
	public void delPathsByUserUUID(String userUUID) {
		pbPathsMapper.delPathsByUserUUID(userUUID);
	}
	
	
	public void delPathsByUserUUIDAndFilePathAndDepth(String userUUID, String path, int depth) {
		pbPathsMapper.delPathsByUserUUIDAndFilePathAndDepth(userUUID, path, depth);
	}
	
	public List<PathsDto> getPathsDtos() {
		return pbPathsMapper.getPathsDtos();
	}
	
	public List<PathsDto> getFilesByUserUUID(String userUUID) {
		return pbPathsMapper.getFilesByUserUUID(userUUID);
	}
	
	public List<PathsDto> getFilesByUserUUIDAndSize(String userUUID) {
		return pbPathsMapper.getFilesByUserUUIDAndSize(userUUID);
	}
	
	
	public PathsDto getFilesByPathsUUID(String pathsUUID) {
		return pbPathsMapper.getFilesByPathsUUID(pathsUUID);
	}
	
	
	public String getFilePathByPathsUUID(String pathsUUID) {
		return pbPathsMapper.getFilePathByPathsUUID(pathsUUID);
	}
	
	
	public String getRootPathsUUIDByUserUUID(String userUUID) {
		return pbPathsMapper.getRootPathsUUIDByUserUUID(userUUID);
	}
	
	
	public String getFileNameByPathsUUID(String pathsUUID) {
		return pbPathsMapper.getFileNameByPathsUUID(pathsUUID);
	}
	
	
	public List<PathsDto> getFilesByUserUUIDAndFilePathANDDepth(String userUUID, String path, int depth) {
		return pbPathsMapper.getFilesByUserUUIDAndFilePathANDDepth(userUUID, path, depth);
	}
	
	
	public List<PathsDto> getReNameOrDeleteOrMoveFilesByUserUUIDAndFileNameANDDepth( String userUUID, String path, int depth) {
		return pbPathsMapper.getReNameOrDeleteOrMoveFilesByUserUUIDAndFileNameANDDepth(userUUID, path, depth);
	}
	
	
	public void movePathsByPathsUUID(String path, int depth, String modTime, String pathsUUID) {
		pbPathsMapper.movePathsByPathsUUID(path, depth, modTime, pathsUUID);
	}
	
	
	public void modPathsByPathsUUID(String path, String filename, String modTime, String pathsUUID) {
		pbPathsMapper.modPathsByPathsUUID(path, filename, modTime, pathsUUID);
	}
	
	
	public void modPathsModTimeAndSizeByPathsUUID(String nSize, String modTime, String pathsUUID) {
		pbPathsMapper.modPathsModTimeAndSizeByPathsUUID(nSize, modTime, pathsUUID);
	}


	public PathsDto getFolderByPathANDUserUUED(String userUUID, String path) {
		return pbPathsMapper.getFolderByPathANDUserUUED(userUUID, path);
	}

}
