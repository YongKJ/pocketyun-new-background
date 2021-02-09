package com.yongkj.pocketyun_new.service;

import com.yongkj.pocketyun_new.dto.ServerDto;

public interface ServerService {
	
	ServerDto getServerByServerUUID(String serverUUID);
	
	void addServer(ServerDto serverDto);

}
