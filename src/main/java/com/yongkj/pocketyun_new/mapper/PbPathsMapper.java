package com.yongkj.pocketyun_new.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.yongkj.pocketyun_new.dto.PathsDto;

@Mapper
public interface PbPathsMapper {
	
	@Insert("INSERT INTO py_paths (pathsUUID, userUUID, path, filename, size, depth, addTime, modTime) VALUES (#{pathsUUID}, #{userUUID}, #{path}, #{filename}, #{size}, #{depth}, #{addTime}, #{modTime})")
	void addPaths(PathsDto pathsDto);
	
	@Delete("DELETE FROM py_paths WHERE pathsUUID=#{pathsUUID}")
	void delPathsByPathsUUID(@Param("pathsUUID") String pathsUUID);
	
	@Delete("DELETE FROM py_paths WHERE userUUID=#{userUUID}")
	void delPathsByUserUUID(@Param("userUUID") String userUUID);
	
	@Delete("DELETE FROM py_paths WHERE userUUID=#{userUUID} AND path like #{path} AND depth>=#{depth}")
	void delPathsByUserUUIDAndFilePathAndDepth(@Param("userUUID") String userUUID, @Param("path") String path, @Param("depth") int depth);
	
	@Select("SELECT * FROM py_paths")
	List<PathsDto> getPathsDtos();
	
	@Select("SELECT * FROM py_paths WHERE userUUID=#{userUUID}")
	List<PathsDto> getFilesByUserUUID(@Param("userUUID") String userUUID);
	
	@Select("SELECT * FROM py_paths WHERE userUUID=#{userUUID} AND size like '%MB'")
	List<PathsDto> getFilesByUserUUIDAndSize(@Param("userUUID") String userUUID);
	
	@Select("SELECT * FROM py_paths WHERE pathsUUID=#{pathsUUID}")
	PathsDto getFilesByPathsUUID(@Param("pathsUUID") String pathsUUID);
	
	@Select("SELECT * FROM py_paths WHERE userUUID=#{userUUID} AND path=#{path}")
	PathsDto getFolderByPathANDUserUUED(@Param("userUUID") String userUUID, @Param("path") String path);
	
	@Select("SELECT path FROM py_paths WHERE pathsUUID=#{pathsUUID}")
	String getFilePathByPathsUUID(@Param("pathsUUID") String pathsUUID);
	
	@Select("SELECT pathsUUID FROM py_paths WHERE userUUID=#{userUUID} AND path=\"/\"")
	String getRootPathsUUIDByUserUUID(@Param("userUUID") String userUUID);
	
	@Select("SELECT filename FROM py_paths WHERE pathsUUID=#{pathsUUID}")
	String getFileNameByPathsUUID(@Param("pathsUUID") String pathsUUID);
	
	@Select("SELECT * FROM py_paths WHERE userUUID=#{userUUID} AND path like #{path} AND depth=#{depth} ORDER BY filename")
	List<PathsDto> getFilesByUserUUIDAndFilePathANDDepth(@Param("userUUID") String userUUID, @Param("path") String path, @Param("depth") int depth);
	
	@Select("SELECT * FROM py_paths WHERE userUUID=#{userUUID} AND path like #{path} AND depth>=#{depth}")
	List<PathsDto> getReNameOrDeleteOrMoveFilesByUserUUIDAndFileNameANDDepth(@Param("userUUID") String userUUID, @Param("path") String path, @Param("depth") int depth);
	
	@Update("UPDATE py_paths SET path = #{path}, filename = #{filename}, modTime = #{modTime} WHERE pathsUUID = #{pathsUUID}")
	void modPathsByPathsUUID(@Param("path") String path, @Param("filename") String filename, @Param("modTime") String modTime, @Param("pathsUUID") String pathsUUID);
	
	@Update("UPDATE py_paths SET path = #{path}, depth = #{depth}, modTime = #{modTime} WHERE pathsUUID = #{pathsUUID}")
	void movePathsByPathsUUID(@Param("path") String path, @Param("depth") int depth, @Param("modTime") String modTime, @Param("pathsUUID") String pathsUUID);
	
	@Update("UPDATE py_paths SET size = #{nSize}, modTime = #{modTime} WHERE pathsUUID = #{pathsUUID}")
	void modPathsModTimeAndSizeByPathsUUID(@Param("nSize") String nSize, @Param("modTime") String modTime, @Param("pathsUUID") String pathsUUID);

}
