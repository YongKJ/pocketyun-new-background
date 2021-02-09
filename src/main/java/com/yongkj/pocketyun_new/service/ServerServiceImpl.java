package com.yongkj.pocketyun_new.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yongkj.pocketyun_new.dto.ServerDto;
import com.yongkj.pocketyun_new.mapper.PbServerMapper;

@Service("serverService")
public class ServerServiceImpl implements ServerService {
	
	@Autowired
	private PbServerMapper pbServerMapper;

	public ServerDto getServerByServerUUID(String serverUUID) {
		return pbServerMapper.getServerByServerUUID(serverUUID);
	}

	public void addServer(ServerDto serverDto) {
		pbServerMapper.addServer(serverDto);
	}

}
