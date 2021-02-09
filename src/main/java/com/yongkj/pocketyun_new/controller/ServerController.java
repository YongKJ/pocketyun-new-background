package com.yongkj.pocketyun_new.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.yongkj.pocketyun_new.basic.controller.BasicController;
import com.yongkj.pocketyun_new.dto.ServerDto;
import com.yongkj.pocketyun_new.service.ServerService;

import net.sf.json.JSONObject;

@Controller
@RequestMapping("/serverController")
public class ServerController extends BasicController {
	
	@Autowired
	@Qualifier("serverService")
	private ServerService serverService;
	
	/***
	 * 将后台服务器信息存入数据库
	 * @param request
	 * @param response
	 */
	@ModelAttribute
	@RequestMapping("/addServerInfo")
	public void userLogin(HttpServletRequest request,HttpServletResponse response) {
		JSONObject json = new JSONObject();
		json.put("message", "插入成功！");
		
		String serverUUID = this.getUUID();
		String accessKeyId = "LTAI4G2sEV5Ntxyaz1DAFSLN";
		String secretAccessKey = "GE2E1bdrnGskAzE9FYIoeXpLr0Z72I";
		String endPiont = "http://oss-cn-shenzhen.aliyuncs.com";
		String endPiontInternal = "http://oss-cn-shenzhen-internal.aliyuncs.com";
		String bucketName = "pocketyun";
		
		ServerDto serverDto = new ServerDto();
		serverDto.setServerUUID(serverUUID);
		serverDto.setAccessKeyId(accessKeyId);
		serverDto.setSecretAccessKey(secretAccessKey);
		serverDto.setEndPiont(endPiont);
		serverDto.setEndPiontInternal(endPiontInternal);
		serverDto.setBucketName(bucketName);
//		serverService.addServer(serverDto);
		
		this.writeJson(json.toString(), response);
	}

}
