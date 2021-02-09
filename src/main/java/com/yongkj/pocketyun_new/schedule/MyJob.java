package com.yongkj.pocketyun_new.schedule;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.yongkj.pocketyun_new.dto.PathsDto;
import com.yongkj.pocketyun_new.dto.ServerDto;
import com.yongkj.pocketyun_new.dto.UserDto;
import com.yongkj.pocketyun_new.service.RecycleService;
import com.yongkj.pocketyun_new.service.ServerService;
import com.yongkj.pocketyun_new.service.UserService;

@Component
public class MyJob extends QuartzJobBean {
	
	@Value("${quartz.myJobCountCron}")
	private String myJobCountCron;
	
	@Autowired
	@Qualifier("userService")
	private UserService userService;
	
	@Autowired
	@Qualifier("recycleService")
	private RecycleService recycleService;
	
	@Autowired
	@Qualifier("serverService")
	private ServerService serverService;

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
//		System.out.println(myJobCountCron);
		
		List<PathsDto> pathsDtosList = recycleService.getPathsDtos();
		if(pathsDtosList != null) {
			for(PathsDto pathsDto : pathsDtosList) {
				pathsDto.setFilename(pathsDto.getFilename().replace("*", ""));
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				
				long modDate = 0;
				try {
					modDate = sdf.parse(pathsDto.getModTime()).getTime();
				} catch (ParseException e) {
					e.printStackTrace();
				}
				
				long nowDate = new Date().getTime();
				int totalSeconds = (int) ((nowDate - modDate) / 1000);
				
				int daySeconds = 24 * 60 * 60;
				
				if(totalSeconds >= 7 * daySeconds) {
					//初始化相对路径
					UserDto userDto = userService.getUserDtosByUserUUID(pathsDto.getUserUUID());
					String rootPath = "fileSystem/" + ((userDto.getUserName() == null || userDto.getUserName().equals("")) ? userDto.getRegEmail() : userDto.getUserName());
					
					//连接阿里OSS
					String serverUUID = "0e7be8dea2bf4f00a664cbd16366fbc0";
					ServerDto serverDto = serverService.getServerByServerUUID(serverUUID);
					String endpoint = serverDto.getEndPiontInternal();
					String accessKeyId = serverDto.getAccessKeyId();
					String accessKeySecret = serverDto.getSecretAccessKey();
					String bucketName = serverDto.getBucketName();
					OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId,accessKeySecret);
					
					//判断是文件还是文件夹
					PathsDto pathsDtoDelete = recycleService.getFilesByPathsUUID(pathsDto.getPathsUUID());
					pathsDtoDelete.setFilename(pathsDtoDelete.getFilename().replace("*", ""));
					
					
					if(pathsDtoDelete.getFilename().indexOf("/") != -1) {
						
						//删除当前或子目录下文件
						List<PathsDto> pathsDtosListDelete = recycleService.getReNameOrDeleteOrMoveFilesByUserUUIDAndFileNameANDDepth(pathsDto.getUserUUID(), pathsDtoDelete.getPath() + "/_%", pathsDtoDelete.getDepth() + 1);
						for(int i = 0; i < pathsDtosListDelete.size(); i++) {
							if(pathsDtosListDelete.get(i).getFilename().indexOf("/") == -1) {
								//删除阿里OSS文件
								String objectName = rootPath + pathsDtosListDelete.get(i).getPath();
								ossClient.deleteObject(bucketName, objectName);
							}
						}
						
						//删除数据库文件夹及其文件夹中的文件或文件夹信息
						recycleService.delPathsByPathsUUID(pathsDto.getPathsUUID());
						recycleService.delPathsByUserUUIDAndFilePathAndDepth(pathsDto.getUserUUID(), pathsDto.getPath() + "/_%", pathsDto.getDepth() + 1);
					}else {
						//删除阿里OSS文件
						String path = pathsDto.getPath();
						String objectName = rootPath + path;
						ossClient.deleteObject(bucketName, objectName);
						
						//删除数据库文件信息
						recycleService.delPathsByPathsUUID(pathsDto.getPathsUUID());
					}
					
					ossClient.shutdown();
				}
			}
		}
	}

}
