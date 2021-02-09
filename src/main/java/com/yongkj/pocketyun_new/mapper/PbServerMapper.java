package com.yongkj.pocketyun_new.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.yongkj.pocketyun_new.dto.ServerDto;

@Mapper
public interface PbServerMapper {
	
	@Select("SELECT * FROM py_server WHERE serverUUID=#{serverUUID}")
	ServerDto getServerByServerUUID(@Param("serverUUID") String serverUUID);
	
	@Insert("INSERT INTO py_server (serverUUID, accessKeyId, secretAccessKey, endPiont, endPiontInternal, bucketName) VALUES (#{serverUUID}, #{accessKeyId}, #{secretAccessKey}, #{endPiont}, #{endPiontInternal}, #{bucketName})")
	void addServer(ServerDto serverDto);

}
