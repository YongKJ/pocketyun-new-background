package com.yongkj.pocketyun_new.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yongkj.pocketyun_new.dto.PathsDto;
import com.yongkj.pocketyun_new.mapper.PbRecycleMapper;

@Service("recycleService")
public class RecycleServiceImpl implements RecycleService {
	
	@Autowired
	private PbRecycleMapper pbRecycleMapper;
	
	
	public void addPaths(PathsDto pathsDto) {
		pbRecycleMapper.addPaths(pathsDto);
	}
	
	
	public void delPathsByPathsUUID(String pathsUUID) {
		pbRecycleMapper.delPathsByPathsUUID(pathsUUID);
	}
	
	
	public void delPathsByUserUUID(String userUUID) {
		pbRecycleMapper.delPathsByUserUUID(userUUID);
	}
	
	
	public void delPathsByUserUUIDAndFilePathAndDepth(String userUUID, String path, int depth) {
		pbRecycleMapper.delPathsByUserUUIDAndFilePathAndDepth(userUUID, path, depth);
	}
	
	public List<PathsDto> getPathsDtos() {
		return pbRecycleMapper.getPathsDtos();
	}
	
	public List<PathsDto> getFilesByUserUUID(String userUUID) {
		return pbRecycleMapper.getFilesByUserUUID(userUUID);
	}
	
	public List<PathsDto> getFilesByUserUUIDAndSize(String userUUID) {
		return pbRecycleMapper.getFilesByUserUUIDAndSize(userUUID);
	}
	
	
	public PathsDto getFilesByPathsUUID(String pathsUUID) {
		return pbRecycleMapper.getFilesByPathsUUID(pathsUUID);
	}
	
	
	public String getFilePathByPathsUUID(String pathsUUID) {
		return pbRecycleMapper.getFilePathByPathsUUID(pathsUUID);
	}
	
	
	public String getRootPathsUUIDByUserUUID(String userUUID) {
		return pbRecycleMapper.getRootPathsUUIDByUserUUID(userUUID);
	}
	
	
	public String getFileNameByPathsUUID(String pathsUUID) {
		return pbRecycleMapper.getFileNameByPathsUUID(pathsUUID);
	}
	
	
	public List<PathsDto> getFilesByUserUUIDAndFilePathANDDepth(String userUUID, String path, int depth) {
		return pbRecycleMapper.getFilesByUserUUIDAndFilePathANDDepth(userUUID, path, depth);
	}
	
	
	public List<PathsDto> getReNameOrDeleteOrMoveFilesByUserUUIDAndFileNameANDDepth( String userUUID, String path, int depth) {
		return pbRecycleMapper.getReNameOrDeleteOrMoveFilesByUserUUIDAndFileNameANDDepth(userUUID, path, depth);
	}
	
	
	public void movePathsByPathsUUID(String path, int depth, String modTime, String pathsUUID) {
		pbRecycleMapper.movePathsByPathsUUID(path, depth, modTime, pathsUUID);
	}
	
	
	public void modPathsByPathsUUID(String path, String filename, String modTime, String pathsUUID) {
		pbRecycleMapper.modPathsByPathsUUID(path, filename, modTime, pathsUUID);
	}
	
	
	public void modPathsModTimeAndSizeByPathsUUID(String nSize, String modTime, String pathsUUID) {
		pbRecycleMapper.modPathsModTimeAndSizeByPathsUUID(nSize, modTime, pathsUUID);
	}


	public PathsDto getFolderByPathANDUserUUED(String userUUID, String path) {
		return pbRecycleMapper.getFolderByPathANDUserUUED(userUUID, path);
	}


	public List<PathsDto> getRecycleFilesByUserUUIDAndFileName(String userUUID) {
		return pbRecycleMapper.getRecycleFilesByUserUUIDAndFileName(userUUID);
	}

}
