package com.yongkj.pocketyun_new.controller;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PutObjectRequest;
import com.yongkj.pocketyun_new.basic.controller.BasicController;
import com.yongkj.pocketyun_new.dto.PathsDto;
import com.yongkj.pocketyun_new.dto.ServerDto;
import com.yongkj.pocketyun_new.dto.UserDto;
import com.yongkj.pocketyun_new.service.PathsService;
import com.yongkj.pocketyun_new.service.RecycleService;
import com.yongkj.pocketyun_new.service.ServerService;
import com.yongkj.pocketyun_new.service.UserService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/pathsController")
public class PathsController extends BasicController {
	
	@Autowired
	@Qualifier("userService")
	private UserService userService;
	
	@Autowired
	@Qualifier("pathsService")
	private PathsService pathsService;
	
	@Autowired
	@Qualifier("recycleService")
	private RecycleService recycleService;
	
	@Autowired
	@Qualifier("serverService")
	private ServerService serverService;
	
	/**
	 * 保存文本内容
	 * @param request
	 * @param response
	 * @param userUUID
	 * @param folderPathsUUID
	 * @param pathsUUID
	 * @param textContent
	 * @throws Exception
	 */
	@ModelAttribute
	@RequestMapping("/saveText")
	public void saveText(HttpServletRequest request,HttpServletResponse response, String userUUID,String folderPathsUUID, String pathsUUID, String textContent) throws Exception {
		JSONObject json = new JSONObject();
		json.put("message", "");
		
//		String realPath = request.getServletContext().getRealPath("");
//		String fileSystem = realPath + "fileSystem";
		
		//初始化相对路径
		UserDto userDto = userService.getUserDtosByUserUUID(userUUID);
		String rootPath = "fileSystem/" + ((userDto.getUserName() == null || userDto.getUserName().equals("")) ? userDto.getRegEmail() : userDto.getUserName());
		
		PathsDto pathsDto = pathsService.getFilesByPathsUUID(pathsUUID);
		PathsDto folderPathsDto = pathsService.getFilesByPathsUUID(folderPathsUUID);
		
//		System.out.println(folderPathsUUID);
//		System.out.println(folderPathsDto == null ? "null" : "not null");
		
		//连接阿里OSS
		String serverUUID = "0e7be8dea2bf4f00a664cbd16366fbc0";
		ServerDto serverDto = serverService.getServerByServerUUID(serverUUID);
		String endpoint = serverDto.getEndPiontInternal();
		String accessKeyId = serverDto.getAccessKeyId();
		String accessKeySecret = serverDto.getSecretAccessKey();
		String bucketName = serverDto.getBucketName();
		OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId,accessKeySecret);
		
		//修改阿里OSS对象
		String objectName = rootPath + pathsDto.getPath();
		if(ossClient.doesObjectExist(bucketName, objectName)) {
			//删除阿里OSS文件
			ossClient.deleteObject(bucketName, objectName);
			
			// 上传字符串
			PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, new ByteArrayInputStream(textContent.getBytes()));
			ossClient.putObject(putObjectRequest);
		}
		
//		String filePath = rootPath + pathsDto.getPath();
//		
//		FileWriter writeFile = new FileWriter(filePath);
//        BufferedWriter writer = new BufferedWriter(writeFile);
        
//        writer.write(textContent);
//        
//        writer.flush();
//        writeFile.close();
        
//        File file = new File(filePath);
		
		//获取新文件信息
		OSSObject ossObject = ossClient.getObject(bucketName, objectName);
        double size = (double) ossObject.getObjectMetadata().getContentLength() / 1048576;
		String nSize =  String.format("%.2f", size) + "MB";
		if(size < 0.005) {
			size = (double) ossObject.getObjectMetadata().getContentLength() / 1024;
			nSize =  String.format("%.2f", size) + "KB";
		}
        String modTime = this.getStringDate(new Date());
        
        //修改数据库文件信息
        pathsService.modPathsModTimeAndSizeByPathsUUID(nSize, modTime, pathsUUID);
        
      //获取文件总大小
        double totalSizes = 0;
		List<PathsDto> pathsDtosListBySize = pathsService.getFilesByUserUUIDAndSize(userUUID);
		for(int i = 0; i < pathsDtosListBySize.size(); i++) {
			double fileSize = Double.parseDouble(pathsDtosListBySize.get(i).getSize().replace("MB", ""));
			totalSizes += fileSize;
		}
		json.put("totalSizes", totalSizes);
        
		//获取文件列表
        List<PathsDto> pathsDtosList = pathsService.getFilesByUserUUIDAndFilePathANDDepth(userUUID, (folderPathsDto.getDepth() + 1 == 1 ? folderPathsDto.getPath() + "_%" : folderPathsDto.getPath() + "/_%"), folderPathsDto.getDepth() + 1);
        json.put("pathsDtosList", pathsDtosList);
		
		this.writeJson(json.toString(), response);
	}
	
	/**
	 * 获取文本内容
	 * @param request
	 * @param response
	 * @param userUUID
	 * @param pathsUUID
	 * @throws Exception
	 */
	@ModelAttribute
	@RequestMapping("/getText")
	public void getText(HttpServletRequest request,HttpServletResponse response, String userUUID, String pathsUUID) throws Exception {
		JSONObject json = new JSONObject();
		json.put("message", "");
		
//		String realPath = request.getServletContext().getRealPath("");
//		String fileSystem = realPath + "fileSystem";
		
		//初始化相对路径
		UserDto userDto = userService.getUserDtosByUserUUID(userUUID);
		String rootPath = "fileSystem/" + ((userDto.getUserName() == null || userDto.getUserName().equals("")) ? userDto.getRegEmail() : userDto.getUserName());
		
		//连接阿里OSS
		String serverUUID = "0e7be8dea2bf4f00a664cbd16366fbc0";
		ServerDto serverDto = serverService.getServerByServerUUID(serverUUID);
		String endpoint = serverDto.getEndPiontInternal();
		String accessKeyId = serverDto.getAccessKeyId();
		String accessKeySecret = serverDto.getSecretAccessKey();
		String bucketName = serverDto.getBucketName();
		OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId,accessKeySecret);
		
		PathsDto pathsDto = pathsService.getFilesByPathsUUID(pathsUUID);
		
		//获取阿里OSS对象
		String objectName = rootPath + pathsDto.getPath();
		OSSObject ossObject = ossClient.getObject(bucketName, objectName);	
		
//		String filePath = rootPath + pathsDto.getPath();
		
//		File file = new File(filePath);
//        FileReader reader = new FileReader(file);
		
		//读取文件内容
        BufferedReader bReader = new BufferedReader(new InputStreamReader(ossObject.getObjectContent()));
        StringBuilder sb = new StringBuilder();
        String s = "";
        while ((s =bReader.readLine()) != null) {
            sb.append(s + "\n");
        }
        bReader.close();
        String str = sb.toString();
        
        ossClient.shutdown();
        
        json.put("textContent", str);
        this.writeJson(json.toString(), response);
	}
	
	/**
	 * 文件流式下载
	 * @param request
	 * @param response
	 * @param pathsUUID
	 * @throws Exception
	 */
	@ModelAttribute
	@RequestMapping("/download")
	public void uploadFile(HttpServletRequest request,HttpServletResponse response, String pathsUUID) throws Exception {
		PathsDto pathsDto = pathsService.getFilesByPathsUUID(pathsUUID);
		
//		String realPath = request.getServletContext().getRealPath("");
//		String fileSystem = realPath + "fileSystem";
		
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
		
		//获取阿里OSS对象
		String objectName = rootPath + pathsDto.getPath();
		OSSObject ossObject = ossClient.getObject(bucketName, objectName);
		
		//设置文件元信息
//		String filePath = rootPath + pathsDto.getPath();
		String fileName = URLEncoder.encode(pathsDto.getFilename(),"UTF-8");
		fileName = fileName.replaceAll("\\+", "%20");
        response.addHeader("Content-Disposition", "attachment;filename=" + fileName);
        response.setHeader("Content-Length",String.valueOf(ossObject.getObjectMetadata().getContentLength()));
        response.setContentType("multipart/form-data");
        
        //读取文件内容
//        FileInputStream in = new FileInputStream(filePath);
        InputStream in = ossObject.getObjectContent();
        OutputStream out = response.getOutputStream();
        byte buffer[] = new byte[1024];
        int len = 0;
        while((len = in.read(buffer)) > 0){
        	out.write(buffer, 0, len);
        }
        in.close();
        out.close();
        
        ossClient.shutdown();
	}
	
	/**
	 * 文件离线下载
	 * @param request
	 * @param response
	 * @param pathsUUID
	 * @param fileUrl
	 * @throws Exception
	 */
	@ModelAttribute
	@RequestMapping("/getHttpFile")
	public void getHttpFile(HttpServletRequest request,HttpServletResponse response, String pathsUUID, String fileUrl) throws Exception {
		JSONObject json = new JSONObject();
		json.put("message", "");
		
//		String realPath = request.getServletContext().getRealPath("");
//		String fileSystem = realPath + "fileSystem";
		
		//初始化相对路径
		PathsDto pathsDto = pathsService.getFilesByPathsUUID(pathsUUID);
		UserDto userDto = userService.getUserDtosByUserUUID(pathsDto.getUserUUID());
		String rootPath = "fileSystem/" + ((userDto.getUserName() == null || userDto.getUserName().equals("")) ? userDto.getRegEmail() : userDto.getUserName());
		
		//获取文件名
		URL url = new URL(fileUrl);
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		String fileName = urlConnection.getHeaderField("Content-Disposition");
		if(fileName == null) {
            fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        }else {
            fileName = new String(fileName.getBytes("ISO-8859-1"), "GBK");
            fileName = URLDecoder.decode(fileName.substring(fileName.indexOf("filename=") + 9), "UTF-8");
        }
        
		//构建文件路径信息
		String newPath = "";
		String nPath = "";
		if(pathsDto.getPath().equals("/")) {
			newPath = rootPath + pathsDto.getPath() + fileName;
			nPath = pathsDto.getPath() + fileName;
		}else {
			newPath = rootPath + pathsDto.getPath() + "/" + fileName;
			nPath = pathsDto.getPath() + "/" + fileName;
		}
		
		//连接阿里OSS
		String serverUUID = "0e7be8dea2bf4f00a664cbd16366fbc0";
		ServerDto serverDto = serverService.getServerByServerUUID(serverUUID);
		String endpoint = serverDto.getEndPiontInternal();
		String accessKeyId = serverDto.getAccessKeyId();
		String accessKeySecret = serverDto.getSecretAccessKey();
		String bucketName = serverDto.getBucketName();
		OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId,accessKeySecret);
		
//		File file = new File(newPath);
		
		//判断文件是否存在
		String objectName = newPath;
		if(!ossClient.doesObjectExist(bucketName, objectName)) {
//			FileUtils.copyURLToFile(url, new File(newPath));
			
			// 上传网络流到阿里OSS
			InputStream inputStream = url.openStream();
			ossClient.putObject(bucketName, objectName, inputStream);

			//将文件信息存入数据库
			if(ossClient.doesObjectExist(bucketName, objectName)) {
				String nPathsUUID = this.getUUID();
				
				double size = (double) urlConnection.getContentLengthLong() / 1048576;
				String nSize =  String.format("%.2f", size) + "MB";
				if(size < 0.005) {
					size = (double) urlConnection.getContentLengthLong() / 1024;
					nSize =  String.format("%.2f", size) + "KB";
				}
				int nDepth = pathsDto.getDepth() + 1;
				String addTime = this.getStringDate(new Date());
				String modTime = addTime;
				
				PathsDto nPathsDto = new PathsDto();
				nPathsDto.setPathsUUID(nPathsUUID);
				nPathsDto.setUserUUID(pathsDto.getUserUUID());
				nPathsDto.setPath(nPath);
				nPathsDto.setFilename(fileName);
				nPathsDto.setSize(nSize);
				nPathsDto.setDepth(nDepth);
				nPathsDto.setAddTime(addTime);
				nPathsDto.setModTime(modTime);
	            pathsService.addPaths(nPathsDto);
	            
	            json.put("fileName", fileName);
			}else {
				json.put("message", "服务器网络错误！");
			}
		}else {
			json.put("message", "文件已存在！");
		}
		
		ossClient.shutdown();
		
		//获取文件总大小
		double totalSizes = 0;
		List<PathsDto> pathsDtosListBySize = pathsService.getFilesByUserUUIDAndSize(pathsDto.getUserUUID());
		for(int i = 0; i < pathsDtosListBySize.size(); i++) {
			double fileSize = Double.parseDouble(pathsDtosListBySize.get(i).getSize().replace("MB", ""));
			totalSizes += fileSize;
		}
		json.put("totalSizes", totalSizes);
		
		//获取文件列表
		List<PathsDto> pathsDtosList = pathsService.getFilesByUserUUIDAndFilePathANDDepth(pathsDto.getUserUUID(), (pathsDto.getDepth() + 1 == 1 ? pathsDto.getPath() + "_%" : pathsDto.getPath() + "/_%"), pathsDto.getDepth() + 1);
        json.put("pathsDtosList", pathsDtosList);
		
		this.writeJson(json.toString(), response);
	}
	
	/**
	 * 文件批量上传
	 * @param request
	 * @param response
	 * @param userUUID
	 * @param pathsUUID
	 * @param files
	 * @throws Exception
	 */
	@ModelAttribute
	@RequestMapping("/uploads")
	public void uploadFiles(HttpServletRequest Request,HttpServletResponse Response, @RequestParam("userUUID")String userUUID, @RequestParam("pathsUUID") String pathsUUID, @RequestParam("files") MultipartFile[] files) throws Exception {
		JSONObject json = new JSONObject();
		json.put("message", "");
//		json.put("filesSize", files.length);
		
//		String realPath = request.getServletContext().getRealPath("");
//		String fileSystem = realPath + "fileSystem";
		
		//初始化相对路径
		UserDto userDto = userService.getUserDtosByUserUUID(userUUID);
		String rootPath = "fileSystem/" + ((userDto.getUserName() == null || userDto.getUserName().equals("")) ? userDto.getRegEmail() : userDto.getUserName());
		
		
		//连接阿里OSS
		String serverUUID = "0e7be8dea2bf4f00a664cbd16366fbc0";
		ServerDto serverDto = serverService.getServerByServerUUID(serverUUID);
		String endpoint = serverDto.getEndPiontInternal();
		String accessKeyId = serverDto.getAccessKeyId();
		String accessKeySecret = serverDto.getSecretAccessKey();
		String bucketName = serverDto.getBucketName();
		OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId,accessKeySecret);
		
		PathsDto pathsDto = pathsService.getFilesByPathsUUID(pathsUUID);
		for(int i = 0; i < files.length; i++) {
			//构建文件信息
			String nPathsUUID = this.getUUID();
			String newPath = "";
			String nPath = "";
			if(pathsDto.getPath().equals("/")) {
				newPath = rootPath + pathsDto.getPath() + files[i].getOriginalFilename();
				nPath = pathsDto.getPath() + files[i].getOriginalFilename();
			}else {
				newPath = rootPath + pathsDto.getPath() + "/" + files[i].getOriginalFilename();
				nPath = pathsDto.getPath() + "/" + files[i].getOriginalFilename();
			}
			double size = (double) files[i].getSize() / 1048576;
			String nSize =  String.format("%.2f", size) + "MB";
			if(size < 0.005) {
				size = (double) files[i].getSize() / 1024;
				nSize =  String.format("%.2f", size) + "KB";
			}
			int nDepth = pathsDto.getDepth() + 1;
			String addTime = this.getStringDate(new Date());
			String modTime = addTime;

			//将文件信息存入数据库
			PathsDto nPathsDto = new PathsDto();
			nPathsDto.setPathsUUID(nPathsUUID);
			nPathsDto.setUserUUID(userUUID);
			nPathsDto.setPath(nPath);
			nPathsDto.setFilename(files[i].getOriginalFilename());
			nPathsDto.setSize(nSize);
			nPathsDto.setDepth(nDepth);
			nPathsDto.setAddTime(addTime);
			nPathsDto.setModTime(modTime);
            pathsService.addPaths(nPathsDto);
            
            //文件上传到阿里OSS
            String objectName = newPath;
            ossClient.putObject(bucketName, objectName, files[i].getInputStream());
            
//            //文件分片上传到阿里OSS
//            String objectName = newPath;
//            InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, objectName);
//            
//            //初始化分片
//            InitiateMultipartUploadResult upresult = ossClient.initiateMultipartUpload(request);
//            String uploadId = upresult.getUploadId();
//            
//         // 计算文件有多少个分片。
//            List<PartETag> partETags =  new ArrayList<PartETag>();
//            final long partSize = 1 * 1024 * 1024L;
//            long fileLength = files[i].getSize();
//            int partCount = (int) (fileLength / partSize);
//            if (fileLength % partSize != 0) {
//                partCount++;
//            }
//            
//         // 遍历分片上传。
//            for (int j = 0; j < partCount; j++) {
//                long startPos = j * partSize;
//                long curPartSize = (j + 1 == partCount) ? (fileLength - startPos) : partSize;
//                InputStream instream = files[i].getInputStream();
//                // 跳过已经上传的分片。
//                instream.skip(startPos);
//                UploadPartRequest uploadPartRequest = new UploadPartRequest();
//                uploadPartRequest.setBucketName(bucketName);
//                uploadPartRequest.setKey(objectName);
//                uploadPartRequest.setUploadId(uploadId);
//                uploadPartRequest.setInputStream(instream);
//                // 设置分片大小。除了最后一个分片没有大小限制，其他的分片最小为100 KB。
//                uploadPartRequest.setPartSize(curPartSize);
//                // 设置分片号。每一个上传的分片都有一个分片号，取值范围是1~10000，如果超出这个范围，OSS将返回InvalidArgument的错误码。
//                uploadPartRequest.setPartNumber( j + 1);
//                // 每个分片不需要按顺序上传，甚至可以在不同客户端上传，OSS会按照分片号排序组成完整的文件。
//                UploadPartResult uploadPartResult = ossClient.uploadPart(uploadPartRequest);
//                // 每次上传分片之后，OSS的返回结果包含PartETag。PartETag将被保存在partETags中。
//                partETags.add(uploadPartResult.getPartETag());
//            }
//            
//	         // 合并文件
//	        CompleteMultipartUploadRequest completeMultipartUploadRequest = new CompleteMultipartUploadRequest(bucketName, objectName, uploadId, partETags);
//	        ossClient.completeMultipartUpload(completeMultipartUploadRequest);
		}
		
		ossClient.shutdown();
		
		//获取文件总大小
		double totalSizes = 0;
		List<PathsDto> pathsDtosListBySize = pathsService.getFilesByUserUUIDAndSize(userUUID);
		for(int i = 0; i < pathsDtosListBySize.size(); i++) {
			double fileSize = Double.parseDouble(pathsDtosListBySize.get(i).getSize().replace("MB", ""));
			totalSizes += fileSize;
		}
		json.put("totalSizes", totalSizes);
		
		//获取文件列表
		List<PathsDto> pathsDtosList = pathsService.getFilesByUserUUIDAndFilePathANDDepth(userUUID, (pathsDto.getDepth() + 1 == 1 ? pathsDto.getPath() + "_%" : pathsDto.getPath() + "/_%"), pathsDto.getDepth() + 1);
        json.put("pathsDtosList", pathsDtosList);
		
		this.writeJson(json.toString(), Response);
	}
	

	
	@ModelAttribute
	@RequestMapping("/createNewFile")
	public void createNewFile(HttpServletRequest request,HttpServletResponse response, String userUUID, String pathsUUID, String newFileName) throws Exception {
		JSONObject json = new JSONObject();
		json.put("message", "");

		PathsDto pathsDto = pathsService.getFilesByPathsUUID(pathsUUID);
		String folderRelativePath = "";
		if(pathsDto.getPath().equals("/")) {
			folderRelativePath = pathsDto.getPath() + newFileName;
		}else {
			folderRelativePath = pathsDto.getPath() + "/" + newFileName;
		}
		
		//判断文件是否存在
		PathsDto oldFile = pathsService.getFolderByPathANDUserUUED(userUUID, folderRelativePath);
		if(oldFile == null) {
			String realPath = "";
	        if(System.getProperty("os.name").contains("dows")) {
	        	realPath = ResourceUtils.getURL("classpath:").getPath();
	        }else {
	        	realPath = new File(ResourceUtils.getURL("classpath:").getPath()).getParentFile().getParentFile().getParent().replace("file:", "");
	        }
	        
	        String uploadPath = realPath +  File.separator + "static" + File.separator + "temp";
	        File upload = new File(uploadPath);
	        if(!upload.exists()) {
	        	upload.mkdirs();
	        }
	        
	        String filePath = uploadPath + File.separator + newFileName;
	        File file = new File(filePath);
	        if(!file.exists()) {
	        	file.createNewFile();
	        }
	        
	        //初始化相对路径
			UserDto userDto = userService.getUserDtosByUserUUID(userUUID);
			String rootPath = "fileSystem/" + ((userDto.getUserName() == null || userDto.getUserName().equals("")) ? userDto.getRegEmail() : userDto.getUserName());
			
			//连接阿里OSS
			String serverUUID = "0e7be8dea2bf4f00a664cbd16366fbc0";
			ServerDto serverDto = serverService.getServerByServerUUID(serverUUID);
			String endpoint = serverDto.getEndPiontInternal();
			String accessKeyId = serverDto.getAccessKeyId();
			String accessKeySecret = serverDto.getSecretAccessKey();
			String bucketName = serverDto.getBucketName();
			OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId,accessKeySecret);
			
			String nPathsUUID = this.getUUID();
			String newPath = "";
			String nPath = "";
			if(pathsDto.getPath().equals("/")) {
				newPath = rootPath + pathsDto.getPath() + newFileName;
				nPath = pathsDto.getPath() + newFileName;
			}else {
				newPath = rootPath + pathsDto.getPath() + "/" + newFileName;
				nPath = pathsDto.getPath() + "/" + newFileName;
			}
			double size = (double) file.length() / 1048576;
			String nSize =  String.format("%.2f", size) + "MB";
			if(size < 0.005) {
				size = (double) file.length() / 1024;
				nSize =  String.format("%.2f", size) + "KB";
			}
			int nDepth = pathsDto.getDepth() + 1;
			String addTime = this.getStringDate(new Date());
			String modTime = addTime;

			//将文件信息存入数据库
			PathsDto nPathsDto = new PathsDto();
			nPathsDto.setPathsUUID(nPathsUUID);
			nPathsDto.setUserUUID(userUUID);
			nPathsDto.setPath(nPath);
			nPathsDto.setFilename(newFileName);
			nPathsDto.setSize(nSize);
			nPathsDto.setDepth(nDepth);
			nPathsDto.setAddTime(addTime);
			nPathsDto.setModTime(modTime);
	        pathsService.addPaths(nPathsDto);
	        
	      //文件上传到阿里OSS
	        String objectName = newPath;
	        InputStream fileInputStream = new FileInputStream(file);
	        ossClient.putObject(bucketName, objectName, fileInputStream);
	        ossClient.shutdown();
	        
	        if(file.exists()) {
	        	FileUtils.deleteQuietly(file);
	        }
		}else {
			json.put("message", "文件已存在！");
		}
		
		//获取文件列表
		List<PathsDto> pathsDtosList = pathsService.getFilesByUserUUIDAndFilePathANDDepth(userUUID, (pathsDto.getDepth() + 1 == 1 ? pathsDto.getPath() + "_%" : pathsDto.getPath() + "/_%"), pathsDto.getDepth() + 1);
        json.put("pathsDtosList", pathsDtosList);
		
		this.writeJson(json.toString(), response);
	}
	
	@ModelAttribute
	@RequestMapping("/uploadMdImg")
	public void uploadMdImg(HttpServletRequest request,HttpServletResponse response, @RequestParam("userUUID") String userUUID, @RequestParam("pathsUUID") String pathsUUID, @RequestParam("mdTextName") String mdTextName, @RequestParam("image") MultipartFile image) throws Exception {
		JSONObject json = new JSONObject();
		json.put("message", "");
		
		String mdTextNameFolder = mdTextName.substring(0, mdTextName.lastIndexOf("."));
		
		PathsDto pathsDto = pathsService.getFilesByPathsUUID(pathsUUID);
		String folderRelativePath = "";
		if(pathsDto.getPath().equals("/")) {
			folderRelativePath = pathsDto.getPath() + mdTextNameFolder;
		}else {
			folderRelativePath = pathsDto.getPath() + "/" + mdTextNameFolder;
		}
		
		//判断文件夹是否存在
		PathsDto oldFile = pathsService.getFolderByPathANDUserUUED(userUUID, folderRelativePath);
		if(oldFile == null) {
			String folderPathsUUID = this.getUUID();
	        String size = "--";
	        String addTime = this.getStringDate(new Date());
	        String modTime = this.getStringDate(new Date());
	        
	      //将文件夹信息存入数据库
            PathsDto folderPathsDto = new PathsDto();
            folderPathsDto.setPathsUUID(folderPathsUUID);
            folderPathsDto.setUserUUID(userUUID);
            folderPathsDto.setPath(folderRelativePath);
            folderPathsDto.setFilename("/" + mdTextNameFolder);
            folderPathsDto.setSize(size);
            folderPathsDto.setDepth(pathsDto.getDepth() + 1);
            folderPathsDto.setAddTime(addTime);
            folderPathsDto.setModTime(modTime);
            pathsService.addPaths(folderPathsDto);
		}
		
		//初始化相对路径
		UserDto userDto = userService.getUserDtosByUserUUID(userUUID);
		String rootPath = "fileSystem/" + ((userDto.getUserName() == null || userDto.getUserName().equals("")) ? userDto.getRegEmail() : userDto.getUserName());
		
		//连接阿里OSS
		String serverUUID = "0e7be8dea2bf4f00a664cbd16366fbc0";
		ServerDto serverDto = serverService.getServerByServerUUID(serverUUID);
		String endpoint = serverDto.getEndPiontInternal();
		String accessKeyId = serverDto.getAccessKeyId();
		String accessKeySecret = serverDto.getSecretAccessKey();
		String bucketName = serverDto.getBucketName();
		OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId,accessKeySecret);
		
		//构建文件信息
		String nPathsUUID = this.getUUID();
		String newPath = "";
		String nPath = "";
		if(pathsDto.getPath().equals("/")) {
			newPath = rootPath + pathsDto.getPath() + mdTextNameFolder + "/" + nPathsUUID + "-" + image.getOriginalFilename();
			nPath = pathsDto.getPath() + mdTextNameFolder + "/" + nPathsUUID + "-" + image.getOriginalFilename();
		}else {
			newPath = rootPath + pathsDto.getPath() + "/" + mdTextNameFolder + "/" + nPathsUUID + "-" + image.getOriginalFilename();
			nPath = pathsDto.getPath() + "/" + mdTextNameFolder + "/" + nPathsUUID + "-" + image.getOriginalFilename();
		}
		double size = (double) image.getSize() / 1048576;
		String nSize =  String.format("%.2f", size) + "MB";
		if(size < 0.005) {
			size = (double) image.getSize() / 1024;
			nSize =  String.format("%.2f", size) + "KB";
		}
		int nDepth = pathsDto.getDepth() + 2;
		String addTime = this.getStringDate(new Date());
		String modTime = addTime;

		//将文件信息存入数据库
		PathsDto nPathsDto = new PathsDto();
		nPathsDto.setPathsUUID(nPathsUUID);
		nPathsDto.setUserUUID(userUUID);
		nPathsDto.setPath(nPath);
		nPathsDto.setFilename(nPathsUUID + "-" + image.getOriginalFilename());
		nPathsDto.setSize(nSize);
		nPathsDto.setDepth(nDepth);
		nPathsDto.setAddTime(addTime);
		nPathsDto.setModTime(modTime);
        pathsService.addPaths(nPathsDto);
        
        //文件上传到阿里OSS
        String objectName = newPath;
        ossClient.putObject(bucketName, objectName, image.getInputStream());
        ossClient.shutdown();
        
        json.put("pathsDto", nPathsDto);
        
        //获取文件总大小
  		double totalSizes = 0;
  		List<PathsDto> pathsDtosListBySize = pathsService.getFilesByUserUUIDAndSize(userUUID);
  		for(int i = 0; i < pathsDtosListBySize.size(); i++) {
  			double fileSize = Double.parseDouble(pathsDtosListBySize.get(i).getSize().replace("MB", ""));
  			totalSizes += fileSize;
  		}
  		json.put("totalSizes", totalSizes);
  		
  		//获取文件列表
  		List<PathsDto> pathsDtosList = pathsService.getFilesByUserUUIDAndFilePathANDDepth(userUUID, (pathsDto.getDepth() + 1 == 1 ? pathsDto.getPath() + "_%" : pathsDto.getPath() + "/_%"), pathsDto.getDepth() + 1);
        json.put("pathsDtosList", pathsDtosList);
		
		this.writeJson(json.toString(), response);
	}
	
	@ModelAttribute
	@RequestMapping("/delUploadMdImg")
	public void delUploadMdImg(HttpServletRequest request,HttpServletResponse response, String userUUID, String pathsUUID, String mdTextName, String imageNameUrl) throws Exception {
		JSONObject json = new JSONObject();
		json.put("message", "");

		String mdTextNameFolder = mdTextName.substring(0, mdTextName.lastIndexOf("."));
		
		PathsDto pathsDto = pathsService.getFilesByPathsUUID(pathsUUID);
		String folderRelativePath = "";
		if(pathsDto.getPath().equals("/")) {
			folderRelativePath = pathsDto.getPath() + mdTextNameFolder;
		}else {
			folderRelativePath = pathsDto.getPath() + "/" + mdTextNameFolder;
		}
		PathsDto mdTextNameFolderPathsDto = pathsService.getFolderByPathANDUserUUED(userUUID, folderRelativePath);
		
		String imageName = imageNameUrl.substring(imageNameUrl.lastIndexOf("/") + 1, imageNameUrl.length());
		PathsDto imageNamePathsDto = pathsService.getFolderByPathANDUserUUED(userUUID, folderRelativePath + "/" + imageName);
		
		UserDto userDto = userService.getUserDtosByUserUUID(userUUID);
		String rootPath = "fileSystem/" + ((userDto.getUserName() == null || userDto.getUserName().equals("")) ? userDto.getRegEmail() : userDto.getUserName());
		
		//连接阿里OSS
		String serverUUID = "0e7be8dea2bf4f00a664cbd16366fbc0";
		ServerDto serverDto = serverService.getServerByServerUUID(serverUUID);
		String endpoint = serverDto.getEndPiontInternal();
		String accessKeyId = serverDto.getAccessKeyId();
		String accessKeySecret = serverDto.getSecretAccessKey();
		String bucketName = serverDto.getBucketName();
		OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId,accessKeySecret);
		
		String objectName = rootPath + imageNamePathsDto.getPath();
		ossClient.deleteObject(bucketName, objectName);
		ossClient.shutdown();
		
		pathsService.delPathsByPathsUUID(imageNamePathsDto.getPathsUUID());
		
		//删除图片文件夹
		List<PathsDto> imagePathsDtosList = pathsService.getFilesByUserUUIDAndFilePathANDDepth(userUUID, mdTextNameFolderPathsDto.getPath() + "/_%", mdTextNameFolderPathsDto.getDepth() + 1);
		if(imagePathsDtosList.size() == 0) {
			pathsService.delPathsByPathsUUID(mdTextNameFolderPathsDto.getPathsUUID());
		}
		
		//获取文件总大小
  		double totalSizes = 0;
  		List<PathsDto> pathsDtosListBySize = pathsService.getFilesByUserUUIDAndSize(userUUID);
  		for(int i = 0; i < pathsDtosListBySize.size(); i++) {
  			double fileSize = Double.parseDouble(pathsDtosListBySize.get(i).getSize().replace("MB", ""));
  			totalSizes += fileSize;
  		}
  		json.put("totalSizes", totalSizes);
  		
  		//获取文件列表
  		List<PathsDto> pathsDtosList = pathsService.getFilesByUserUUIDAndFilePathANDDepth(userUUID, (pathsDto.getDepth() + 1 == 1 ? pathsDto.getPath() + "_%" : pathsDto.getPath() + "/_%"), pathsDto.getDepth() + 1);
        json.put("pathsDtosList", pathsDtosList);
		
		this.writeJson(json.toString(), response);
	}
	
	/**
	 * 创建文件夹
	 * @param request
	 * @param response
	 * @param userUUID
	 * @param path
	 * @param depth
	 * @param folder
	 */
	@ModelAttribute
	@RequestMapping("/mkdir")
	public void mkdirFolder(HttpServletRequest request,HttpServletResponse response, String userUUID, String path, int depth, String folder) {
		JSONObject json = new JSONObject();
		json.put("message", "");
		
//		String realPath = request.getServletContext().getRealPath("");
//		String fileSystem = realPath + "fileSystem";
		
		//初始化相对路径
//		UserDto userDto = userService.getUserDtosByUserUUID(userUUID);
//		String rootPath = "fileSystem/" + ((userDto.getUserName() == null || userDto.getUserName().equals("")) ? userDto.getRegEmail() : userDto.getUserName());
		
		//构建文件夹信息
		String pathsUUID = this.getUUID();
        String size = "--";
        String addTime = this.getStringDate(new Date());
        String modTime = this.getStringDate(new Date());
        
        String folderRelativePath = "";
		if(path.equals("/")) {
			folderRelativePath = path + folder;
		}else {
			folderRelativePath = path + "/" + folder;
		}
		
		//判断文件夹是否存在
		PathsDto oldFolder = pathsService.getFolderByPathANDUserUUED(userUUID, folderRelativePath);
        if (oldFolder == null) {
            
        	//将文件夹信息存入数据库
            PathsDto pathsDto = new PathsDto();
            pathsDto.setPathsUUID(pathsUUID);
            pathsDto.setUserUUID(userUUID);
            pathsDto.setPath(folderRelativePath);
            pathsDto.setFilename("/" + folder);
            pathsDto.setSize(size);
            pathsDto.setDepth(depth);
            pathsDto.setAddTime(addTime);
            pathsDto.setModTime(modTime);
            pathsService.addPaths(pathsDto);
        }else {
        	json.put("message", "文件夹已存在！");
        }
        
      //获取文件列表
        List<PathsDto> pathsDtosList = pathsService.getFilesByUserUUIDAndFilePathANDDepth(userUUID, (depth == 1 ? path + "_%" : path + "/_%"), depth);
        json.put("pathsDtosList", pathsDtosList);
		
		this.writeJson(json.toString(), response);
	}
	
	/**
	 * 删除单个文件或文件夹
	 * @param request
	 * @param response
	 * @param userUUID
	 * @param lastPathsUUID
	 * @param pathsUUID
	 * @throws Exception
	 */
	@ModelAttribute
	@RequestMapping("/delFile")
	public void delFileOrFolder(HttpServletRequest request,HttpServletResponse response, String userUUID, String pathsUUID) throws Exception {
		JSONObject json = new JSONObject();
		json.put("message", "");
		
//		String realPath = request.getServletContext().getRealPath("");
//		String fileSystem = realPath + "fileSystem";
		
		//初始化相对路径
		UserDto userDto = userService.getUserDtosByUserUUID(userUUID);
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
//		PathsDto lastPathsDto = pathsService.getFilesByPathsUUID(lastPathsUUID);
		PathsDto pathsDto = pathsService.getFilesByPathsUUID(pathsUUID);
//		String lastPath = pathsDto.getPath().replace("/" + pathsDto.getFilename().replace("/", ""), "");
//		PathsDto lastPathsDto = pathsService.getFolderByPathANDUserUUED(userUUID, lastPath);
		
		int filenameIndex = pathsDto.getPath().lastIndexOf(pathsDto.getDepth() == 1 ? pathsDto.getFilename().replace("/", "") : "/" + pathsDto.getFilename().replace("/", ""));
		String lastPath = pathsDto.getPath().substring(0, filenameIndex);
		PathsDto lastPathsDto = pathsService.getFolderByPathANDUserUUED(userUUID, lastPath);
		
//		String lastPath = pathsDto.getPath().replace("/" + pathsDto.getFilename().replace("/", ""), "");
//		PathsDto lasPathsDtoNew = pathsService.getFolderByPathANDUserUUED(userUUID, lastPath);
//		System.out.println(lastPathsUUID);
//		System.out.println(lasPathsDtoNew.getPathsUUID());
//		System.out.println("----------------------------------------------------" );
		
		if(pathsDto.getFilename().indexOf("/") != -1) {
//			String path = pathsDto.getPath();
//			String folderPath = rootPath + path;
			
//			String cmd = "rm -r " + folderPath;
//			Process process = Runtime.getRuntime().exec(cmd);
//			process.waitFor();
			
//			FileUtils.deleteQuietly(new File(folderPath));
			
			//删除当前或子目录下文件
			List<PathsDto> pathsDtosList = pathsService.getReNameOrDeleteOrMoveFilesByUserUUIDAndFileNameANDDepth(userUUID, pathsDto.getPath() + "/_%", pathsDto.getDepth() + 1);
			for(int i = 0; i < pathsDtosList.size(); i++) {
				if(pathsDtosList.get(i).getFilename().indexOf("/") == -1) {
					//删除阿里OSS文件
					String objectName = rootPath + pathsDtosList.get(i).getPath();
					ossClient.deleteObject(bucketName, objectName);
				}
			}
			
			//删除数据库文件夹及其文件夹中的文件或文件夹信息
			pathsService.delPathsByPathsUUID(pathsUUID);
			pathsService.delPathsByUserUUIDAndFilePathAndDepth(userUUID, pathsDto.getPath() + "/_%", pathsDto.getDepth() + 1);
		}else {
			//删除阿里OSS文件
			String path = pathsDto.getPath();
			String objectName = rootPath + path;
			ossClient.deleteObject(bucketName, objectName);
//			File file = new File(filePath);
//			if (!file.isDirectory()) {
//				file.delete();
//			}
			
			//删除数据库文件信息
			pathsService.delPathsByPathsUUID(pathsUUID);
		}
		
		ossClient.shutdown();
		
		//获取文件总大小
		double totalSizes = 0;
		List<PathsDto> pathsDtosListBySize = pathsService.getFilesByUserUUIDAndSize(userUUID);
		for(int i = 0; i < pathsDtosListBySize.size(); i++) {
			double fileSize = Double.parseDouble(pathsDtosListBySize.get(i).getSize().replace("MB", ""));
			totalSizes += fileSize;
		}
		json.put("totalSizes", totalSizes);
		
		//获取文件列表
		List<PathsDto> pathsDtosList = pathsService.getFilesByUserUUIDAndFilePathANDDepth(userUUID, (lastPathsDto.getDepth() + 1 == 1 ? lastPathsDto.getPath() + "_%" : lastPathsDto.getPath() + "/_%"), lastPathsDto.getDepth() + 1);
		json.put("pathsDtosList", pathsDtosList);
		
		this.writeJson(json.toString(), response);
	}
	
	@ModelAttribute
	@RequestMapping("/delRecycleFile")
	public void delRecycleFileOrFolder(HttpServletRequest request,HttpServletResponse response, String userUUID, String pathsUUID) throws Exception {
		JSONObject json = new JSONObject();
		json.put("message", "");
		
		
		//初始化相对路径
		UserDto userDto = userService.getUserDtosByUserUUID(userUUID);
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
		PathsDto pathsDto = recycleService.getFilesByPathsUUID(pathsUUID);
		pathsDto.setFilename(pathsDto.getFilename().replace("*", ""));
		
		
		if(pathsDto.getFilename().indexOf("/") != -1) {
			
			//删除当前或子目录下文件
			List<PathsDto> pathsDtosList = recycleService.getReNameOrDeleteOrMoveFilesByUserUUIDAndFileNameANDDepth(userUUID, pathsDto.getPath() + "/_%", pathsDto.getDepth() + 1);
			for(int i = 0; i < pathsDtosList.size(); i++) {
				if(pathsDtosList.get(i).getFilename().indexOf("/") == -1) {
					//删除阿里OSS文件
					String objectName = rootPath + pathsDtosList.get(i).getPath();
					ossClient.deleteObject(bucketName, objectName);
				}
			}
			
			//删除数据库文件夹及其文件夹中的文件或文件夹信息
			recycleService.delPathsByPathsUUID(pathsUUID);
			recycleService.delPathsByUserUUIDAndFilePathAndDepth(userUUID, pathsDto.getPath() + "/_%", pathsDto.getDepth() + 1);
		}else {
			//删除阿里OSS文件
			String path = pathsDto.getPath();
			String objectName = rootPath + path;
			ossClient.deleteObject(bucketName, objectName);
			
			//删除数据库文件信息
			recycleService.delPathsByPathsUUID(pathsUUID);
		}
		
		ossClient.shutdown();
		
		//获取文件列表
		List<PathsDto> pathsDtosList = recycleService.getRecycleFilesByUserUUIDAndFileName(userUUID);
		if(pathsDtosList != null) {
			for(PathsDto pathsDtoNew : pathsDtosList) {
				pathsDtoNew.setFilename(pathsDtoNew.getFilename().replace("*", ""));
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				long modDate = sdf.parse(pathsDtoNew.getModTime()).getTime();
				long nowDate = new Date().getTime();
				int totalSeconds = (int) ((nowDate - modDate) / 1000);

				int daySeconds = 24 * 60 * 60;
				int hourSeconds = 60 * 60;
				int minuteSeconds = 60;
				
				if(totalSeconds >= 7 * daySeconds) {
					pathsDtoNew.setDeleteTime("稍后");
				}else if(totalSeconds > 6 * daySeconds) {
					if(totalSeconds > (6 * daySeconds + 23 * hourSeconds)) {
						if(totalSeconds > (6 * daySeconds + 23 * hourSeconds + 59 * minuteSeconds)) {
							pathsDtoNew.setDeleteTime("稍后");
						}else {
							pathsDtoNew.setDeleteTime(((7 * daySeconds - totalSeconds) / minuteSeconds) + "分钟后");
						}
					}else {
						pathsDtoNew.setDeleteTime(((7 * daySeconds - totalSeconds) / hourSeconds) + "小时后");
					}
				}else {
					pathsDtoNew.setDeleteTime(((7 * daySeconds - totalSeconds) / daySeconds) + "天后");
				}
			}
		}else {
			json.put("message", "空的文件回收站！");
		}
		
		json.put("pathsDtosList", pathsDtosList);
		
		this.writeJson(json.toString(), response);
	}
	
	@ModelAttribute
	@RequestMapping("/restoreRecycleFile")
	public void restoreRecycleFileOrFolder(HttpServletRequest request,HttpServletResponse response, String userUUID, String pathsUUID) throws Exception {
		JSONObject json = new JSONObject();
		json.put("message", "");
		
		PathsDto pathsDto = recycleService.getFilesByPathsUUID(pathsUUID);
		String modTime = this.getStringDate(new Date());
		pathsDto.setModTime(modTime);
		pathsDto.setFilename(pathsDto.getFilename().replace("*", ""));
		
		//添加父文件夹
		String filename = pathsDto.getFilename();
		String path = pathsDto.getPath();
		int depth = pathsDto.getDepth();
		for(int i = 0; i < pathsDto.getDepth() - 1; i++) {
			int filenameIndex = path.lastIndexOf("/" + filename.replace("/", ""));
			String lastPath = path.substring(0, filenameIndex);
			
			int filenameIndex1 = lastPath.lastIndexOf("/");
			String lastFilename = lastPath.substring(filenameIndex1, lastPath.length());
			
			int lastDepth = depth - 1;
			
//			System.out.println(lastPath + "----------------" + lastFilename);
			
			PathsDto pathsDto2 = pathsService.getFolderByPathANDUserUUED(userUUID, lastPath);
			if(pathsDto2 == null) {
				String pathsUUID1 = this.getUUID();
		        String size = "--";
		        String addTime = this.getStringDate(new Date());
		        String modTime1 = this.getStringDate(new Date());
				
				//将文件夹信息存入数据库
	            PathsDto pathsDto3 = new PathsDto();
	            pathsDto3.setPathsUUID(pathsUUID1);
	            pathsDto3.setUserUUID(userUUID);
	            pathsDto3.setPath(lastPath);
	            pathsDto3.setFilename(lastFilename);
	            pathsDto3.setSize(size);
	            pathsDto3.setDepth(lastDepth);
	            pathsDto3.setAddTime(addTime);
	            pathsDto3.setModTime(modTime1);
	            pathsService.addPaths(pathsDto3);
			}
			
			filename = lastFilename;
			path = lastPath;
			depth = lastDepth;
		}
		
		if(pathsDto.getFilename().indexOf("/") != -1) {
			PathsDto pathsDto2 = pathsService.getFolderByPathANDUserUUED(userUUID, pathsDto.getPath());
			if(pathsDto2 == null) {
				pathsService.addPaths(pathsDto);
			}
			
			//将当前或子目录下文件信息存入回收站数据表
			List<PathsDto> pathsDtosList = recycleService.getReNameOrDeleteOrMoveFilesByUserUUIDAndFileNameANDDepth(userUUID, pathsDto.getPath() + "/_%", pathsDto.getDepth() + 1);
			for(int i = 0; i < pathsDtosList.size(); i++) {
				PathsDto pathsDto3 = pathsService.getFolderByPathANDUserUUED(userUUID, pathsDtosList.get(i).getPath());
				if(pathsDto3 == null) {
					pathsDtosList.get(i).setModTime(modTime);
					pathsService.addPaths(pathsDtosList.get(i));
				}
			}
			
			//删除数据库文件夹及其文件夹中的文件或文件夹信息
			recycleService.delPathsByPathsUUID(pathsUUID);
			recycleService.delPathsByUserUUIDAndFilePathAndDepth(userUUID, pathsDto.getPath() + "/_%", pathsDto.getDepth() + 1);
		}else {
			PathsDto pathsDto2 = pathsService.getFolderByPathANDUserUUED(userUUID, pathsDto.getPath());
			if(pathsDto2 == null) {
				pathsService.addPaths(pathsDto);
			}
			recycleService.delPathsByPathsUUID(pathsUUID);
		}
		
		//获取文件列表
		List<PathsDto> pathsDtosList = recycleService.getRecycleFilesByUserUUIDAndFileName(userUUID);
		if(pathsDtosList != null) {
			for(PathsDto pathsDtoNew : pathsDtosList) {
				pathsDtoNew.setFilename(pathsDtoNew.getFilename().replace("*", ""));
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				long modDate = sdf.parse(pathsDtoNew.getModTime()).getTime();
				long nowDate = new Date().getTime();
				int totalSeconds = (int) ((nowDate - modDate) / 1000);

				int daySeconds = 24 * 60 * 60;
				int hourSeconds = 60 * 60;
				int minuteSeconds = 60;
				
				if(totalSeconds >= 7 * daySeconds) {
					pathsDtoNew.setDeleteTime("稍后");
				}else if(totalSeconds > 6 * daySeconds) {
					if(totalSeconds > (6 * daySeconds + 23 * hourSeconds)) {
						if(totalSeconds > (6 * daySeconds + 23 * hourSeconds + 59 * minuteSeconds)) {
							pathsDtoNew.setDeleteTime("稍后");
						}else {
							pathsDtoNew.setDeleteTime(((7 * daySeconds - totalSeconds) / minuteSeconds) + "分钟后");
						}
					}else {
						pathsDtoNew.setDeleteTime(((7 * daySeconds - totalSeconds) / hourSeconds) + "小时后");
					}
				}else {
					pathsDtoNew.setDeleteTime(((7 * daySeconds - totalSeconds) / daySeconds) + "天后");
				}
			}
		}else {
			json.put("message", "空的文件回收站！");
		}
		
		json.put("pathsDtosList", pathsDtosList);
		
		this.writeJson(json.toString(), response);
	}
	
	/**
	 * 回收单个文件或文件夹
	 * @param request
	 * @param response
	 * @param userUUID
	 * @param pathsUUID
	 * @throws Exception
	 */
	@ModelAttribute
	@RequestMapping("/recycleFile")
	public void recycleFileOrFolder(HttpServletRequest request,HttpServletResponse response, String userUUID, String pathsUUID) throws Exception {
		JSONObject json = new JSONObject();
		json.put("message", "");
		
		PathsDto pathsDto = pathsService.getFilesByPathsUUID(pathsUUID);
		
		int filenameIndex = pathsDto.getPath().lastIndexOf(pathsDto.getDepth() == 1 ? pathsDto.getFilename().replace("/", "") : "/" + pathsDto.getFilename().replace("/", ""));
		String lastPath = pathsDto.getPath().substring(0, filenameIndex);
		PathsDto lastPathsDto = pathsService.getFolderByPathANDUserUUED(userUUID, lastPath);

		String modTime = this.getStringDate(new Date());
		pathsDto.setModTime(modTime);
		pathsDto.setFilename(pathsDto.getFilename() + "*");
		
		if(pathsDto.getFilename().indexOf("/") != -1) {
			recycleService.addPaths(pathsDto);
			
			//将当前或子目录下文件信息存入回收站数据表
			List<PathsDto> pathsDtosList = pathsService.getReNameOrDeleteOrMoveFilesByUserUUIDAndFileNameANDDepth(userUUID, pathsDto.getPath() + "/_%", pathsDto.getDepth() + 1);
			for(int i = 0; i < pathsDtosList.size(); i++) {
				recycleService.addPaths(pathsDtosList.get(i));
			}
			
			//删除数据库文件夹及其文件夹中的文件或文件夹信息
			pathsService.delPathsByPathsUUID(pathsUUID);
			pathsService.delPathsByUserUUIDAndFilePathAndDepth(userUUID, pathsDto.getPath() + "/_%", pathsDto.getDepth() + 1);
		}else {
			recycleService.addPaths(pathsDto);
			pathsService.delPathsByPathsUUID(pathsUUID);
		}
		
		//获取文件总大小
		double totalSizes = 0;
		List<PathsDto> pathsDtosListBySize = pathsService.getFilesByUserUUIDAndSize(userUUID);
		for(int i = 0; i < pathsDtosListBySize.size(); i++) {
			double fileSize = Double.parseDouble(pathsDtosListBySize.get(i).getSize().replace("MB", ""));
			totalSizes += fileSize;
		}
		json.put("totalSizes", totalSizes);
		
		//获取文件列表
		List<PathsDto> pathsDtosList = pathsService.getFilesByUserUUIDAndFilePathANDDepth(userUUID, (lastPathsDto.getDepth() + 1 == 1 ? lastPathsDto.getPath() + "_%" : lastPathsDto.getPath() + "/_%"), lastPathsDto.getDepth() + 1);
		json.put("pathsDtosList", pathsDtosList);
		
		this.writeJson(json.toString(), response);
	}
	
	@ModelAttribute
	@RequestMapping("/searchFiles")
	public void searchFiles(HttpServletRequest request,HttpServletResponse response, String userUUID, String lastPathsUUID, String search) throws Exception {
		JSONObject json = new JSONObject();
		json.put("message", "");
		
		PathsDto lastPathsDto = pathsService.getFilesByPathsUUID(lastPathsUUID);
		List<PathsDto> pathsDtosList = pathsService.getReNameOrDeleteOrMoveFilesByUserUUIDAndFileNameANDDepth(userUUID, (lastPathsDto.getDepth() + 1 == 1 ? lastPathsDto.getPath() + "_%" : lastPathsDto.getPath() + "/_%"), lastPathsDto.getDepth() + 1);
		List<PathsDto> pathsDtosListSearch = new ArrayList<PathsDto>();
//		System.out.println(search);
		for(int i = 0; i < pathsDtosList.size(); i++) {
			
			if(pathsDtosList.get(i).getFilename().indexOf(search) != -1) {
				pathsDtosListSearch.add(pathsDtosList.get(i));
			}
		}
		json.put("pathsDtosList", pathsDtosListSearch);
		
		//获取文件总大小
		double totalSizes = 0;
		List<PathsDto> pathsDtosListBySize = pathsService.getFilesByUserUUIDAndSize(userUUID);
		for(int i = 0; i < pathsDtosListBySize.size(); i++) {
			double fileSize = Double.parseDouble(pathsDtosListBySize.get(i).getSize().replace("MB", ""));
			totalSizes += fileSize;
		}
		json.put("totalSizes", totalSizes);
		
		this.writeJson(json.toString(), response);
	}
	
	/**
	 * 删除文件或文件夹列表
	 * @param request
	 * @param response
	 * @param userUUID
	 * @param lastPathsUUID
	 * @param filesJsonArray
	 * @throws Exception
	 */
	@ModelAttribute
	@RequestMapping("/delFiles")
	public void delFilesOrFolders(HttpServletRequest request,HttpServletResponse response, String userUUID, String lastPathsUUID, String filesJsonArray) throws Exception {
		JSONObject json = new JSONObject();
		json.put("message", "");
		
//		String realPath = request.getServletContext().getRealPath("");
//		String fileSystem = realPath + "fileSystem";
		
		//初始化相对路径
		UserDto userDto = userService.getUserDtosByUserUUID(userUUID);
		String rootPath = "fileSystem/" + ((userDto.getUserName() == null || userDto.getUserName().equals("")) ? userDto.getRegEmail() : userDto.getUserName());
		
		//连接阿里OSS
		String serverUUID = "0e7be8dea2bf4f00a664cbd16366fbc0";
		ServerDto serverDto = serverService.getServerByServerUUID(serverUUID);
		String endpoint = serverDto.getEndPiontInternal();
		String accessKeyId = serverDto.getAccessKeyId();
		String accessKeySecret = serverDto.getSecretAccessKey();
		String bucketName = serverDto.getBucketName();
		OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId,accessKeySecret);
		
		//删除文件或文件夹列表
		PathsDto lastPathsDto = pathsService.getFilesByPathsUUID(lastPathsUUID);
		JSONArray jsonArray = JSONArray.fromObject(filesJsonArray);
		for(int i = 0; i < jsonArray.size(); i++) {
			//判断文件或文件夹
			JSONObject jsonObject = JSONObject.fromObject(jsonArray.get(i));
			if(jsonObject.getString("filename").indexOf("/") != -1) {
//				String path = jsonObject.getString("path");
//				String folderPath = rootPath + path;
				
//				String cmd = "rm -r " + folderPath;
//				Process process = Runtime.getRuntime().exec(cmd);
//				process.waitFor();
				
//				FileUtils.deleteQuietly(new File(folderPath));
				
				//删除当前或子目录下文件
				List<PathsDto> pathsDtosList = pathsService.getReNameOrDeleteOrMoveFilesByUserUUIDAndFileNameANDDepth(userUUID, jsonObject.getString("path") + "/_%", jsonObject.getInt("depth") + 1);
				for(int j = 0; j < pathsDtosList.size(); j++) {
					if(pathsDtosList.get(j).getFilename().indexOf("/") == -1) {
						//删除阿里OSS文件
						String objectName = rootPath + pathsDtosList.get(j).getPath();
						ossClient.deleteObject(bucketName, objectName);
					}
				}
				
				//删除数据库文件夹及其文件夹中的文件或文件夹信息
				pathsService.delPathsByPathsUUID(jsonObject.getString("pathsUUID"));
				pathsService.delPathsByUserUUIDAndFilePathAndDepth(userUUID, jsonObject.getString("path") + "/_%", jsonObject.getInt("depth") + 1);
			}else {
				//删除阿里OSS文件
				String path = jsonObject.getString("path");
				String objectName = rootPath + path;
				ossClient.deleteObject(bucketName, objectName);
//				File file = new File(filePath);
//				if (!file.isDirectory()) {
//					file.delete();
//				}
				
				//删除数据库文件信息
				pathsService.delPathsByPathsUUID(jsonObject.getString("pathsUUID"));
			}
		}
		
		ossClient.shutdown();
		
		//获取文件总大小
		double totalSizes = 0;
		List<PathsDto> pathsDtosListBySize = pathsService.getFilesByUserUUIDAndSize(userUUID);
		for(int i = 0; i < pathsDtosListBySize.size(); i++) {
			double fileSize = Double.parseDouble(pathsDtosListBySize.get(i).getSize().replace("MB", ""));
			totalSizes += fileSize;
		}
		json.put("totalSizes", totalSizes);
		
		//获取文件列表
		List<PathsDto> pathsDtosList = pathsService.getFilesByUserUUIDAndFilePathANDDepth(userUUID, (lastPathsDto.getDepth() + 1 == 1 ? lastPathsDto.getPath() + "_%" : lastPathsDto.getPath() + "/_%"), lastPathsDto.getDepth() + 1);
		json.put("pathsDtosList", pathsDtosList);
		
		this.writeJson(json.toString(), response);
	}
	
	@ModelAttribute
	@RequestMapping("/delRecycleFiles")
	public void delRecycleFilesOrFolders(HttpServletRequest request,HttpServletResponse response, String userUUID, String filesJsonArray) throws Exception {
		JSONObject json = new JSONObject();
		json.put("message", "");
		
		//初始化相对路径
		UserDto userDto = userService.getUserDtosByUserUUID(userUUID);
		String rootPath = "fileSystem/" + ((userDto.getUserName() == null || userDto.getUserName().equals("")) ? userDto.getRegEmail() : userDto.getUserName());
		
		//连接阿里OSS
		String serverUUID = "0e7be8dea2bf4f00a664cbd16366fbc0";
		ServerDto serverDto = serverService.getServerByServerUUID(serverUUID);
		String endpoint = serverDto.getEndPiontInternal();
		String accessKeyId = serverDto.getAccessKeyId();
		String accessKeySecret = serverDto.getSecretAccessKey();
		String bucketName = serverDto.getBucketName();
		OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId,accessKeySecret);
		
		//删除文件或文件夹列表
		JSONArray jsonArray = JSONArray.fromObject(filesJsonArray);
		for(int i = 0; i < jsonArray.size(); i++) {
			//判断文件或文件夹
			JSONObject jsonObject = JSONObject.fromObject(jsonArray.get(i));
			if(jsonObject.getString("filename").indexOf("/") != -1) {
				
				//删除当前或子目录下文件
				List<PathsDto> pathsDtosList = recycleService.getReNameOrDeleteOrMoveFilesByUserUUIDAndFileNameANDDepth(userUUID, jsonObject.getString("path") + "/_%", jsonObject.getInt("depth") + 1);
				for(int j = 0; j < pathsDtosList.size(); j++) {
					if(pathsDtosList.get(j).getFilename().indexOf("/") == -1) {
						//删除阿里OSS文件
						String objectName = rootPath + pathsDtosList.get(j).getPath();
						ossClient.deleteObject(bucketName, objectName);
					}
				}
				
				//删除数据库文件夹及其文件夹中的文件或文件夹信息
				recycleService.delPathsByPathsUUID(jsonObject.getString("pathsUUID"));
				recycleService.delPathsByUserUUIDAndFilePathAndDepth(userUUID, jsonObject.getString("path") + "/_%", jsonObject.getInt("depth") + 1);
			}else {
				//删除阿里OSS文件
				String path = jsonObject.getString("path");
				String objectName = rootPath + path;
				ossClient.deleteObject(bucketName, objectName);
				
				//删除数据库文件信息
				recycleService.delPathsByPathsUUID(jsonObject.getString("pathsUUID"));
			}
		}
		
		ossClient.shutdown();
		
		//获取文件列表
		List<PathsDto> pathsDtosList = recycleService.getRecycleFilesByUserUUIDAndFileName(userUUID);
		if(pathsDtosList != null) {
			for(PathsDto pathsDto : pathsDtosList) {
				pathsDto.setFilename(pathsDto.getFilename().replace("*", ""));
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				long modDate = sdf.parse(pathsDto.getModTime()).getTime();
				long nowDate = new Date().getTime();
				int totalSeconds = (int) ((nowDate - modDate) / 1000);

				int daySeconds = 24 * 60 * 60;
				int hourSeconds = 60 * 60;
				int minuteSeconds = 60;
				
				if(totalSeconds >= 7 * daySeconds) {
					pathsDto.setDeleteTime("稍后");
				}else if(totalSeconds > 6 * daySeconds) {
					if(totalSeconds > (6 * daySeconds + 23 * hourSeconds)) {
						if(totalSeconds > (6 * daySeconds + 23 * hourSeconds + 59 * minuteSeconds)) {
							pathsDto.setDeleteTime("稍后");
						}else {
							pathsDto.setDeleteTime(((7 * daySeconds - totalSeconds) / minuteSeconds) + "分钟后");
						}
					}else {
						pathsDto.setDeleteTime(((7 * daySeconds - totalSeconds) / hourSeconds) + "小时后");
					}
				}else {
					pathsDto.setDeleteTime(((7 * daySeconds - totalSeconds) / daySeconds) + "天后");
				}
			}
		}else {
			json.put("message", "空的文件回收站！");
		}
		
		json.put("pathsDtosList", pathsDtosList);
		
		this.writeJson(json.toString(), response);
	}
	
	@ModelAttribute
	@RequestMapping("/recycleFiles")
	public void recycleFilesOrFolders(HttpServletRequest request,HttpServletResponse response, String userUUID, String lastPathsUUID, String filesJsonArray) throws Exception {
		JSONObject json = new JSONObject();
		json.put("message", "");
		
		//删除文件或文件夹列表
		PathsDto lastPathsDto = pathsService.getFilesByPathsUUID(lastPathsUUID);
		JSONArray jsonArray = JSONArray.fromObject(filesJsonArray);
		for(int i = 0; i < jsonArray.size(); i++) {
			//判断文件或文件夹
			JSONObject jsonObject = JSONObject.fromObject(jsonArray.get(i));

			PathsDto pathsDto = pathsService.getFilesByPathsUUID(jsonObject.getString("pathsUUID"));
			String modTime = this.getStringDate(new Date());
			pathsDto.setModTime(modTime);
			pathsDto.setFilename(pathsDto.getFilename() + "*");
			
			if(jsonObject.getString("filename").indexOf("/") != -1) {
				recycleService.addPaths(pathsDto);
				
				//删除当前或子目录下文件
				List<PathsDto> pathsDtosList = pathsService.getReNameOrDeleteOrMoveFilesByUserUUIDAndFileNameANDDepth(userUUID, jsonObject.getString("path") + "/_%", jsonObject.getInt("depth") + 1);
				for(int j = 0; j < pathsDtosList.size(); j++) {
					recycleService.addPaths(pathsDtosList.get(j));
				}
				
				//删除数据库文件夹及其文件夹中的文件或文件夹信息
				pathsService.delPathsByPathsUUID(jsonObject.getString("pathsUUID"));
				pathsService.delPathsByUserUUIDAndFilePathAndDepth(userUUID, jsonObject.getString("path") + "/_%", jsonObject.getInt("depth") + 1);
			}else {
				recycleService.addPaths(pathsDto);
				
				//删除数据库文件信息
				pathsService.delPathsByPathsUUID(jsonObject.getString("pathsUUID"));
			}
		}
		
		//获取文件总大小
		double totalSizes = 0;
		List<PathsDto> pathsDtosListBySize = pathsService.getFilesByUserUUIDAndSize(userUUID);
		for(int i = 0; i < pathsDtosListBySize.size(); i++) {
			double fileSize = Double.parseDouble(pathsDtosListBySize.get(i).getSize().replace("MB", ""));
			totalSizes += fileSize;
		}
		json.put("totalSizes", totalSizes);
		
		//获取文件列表
		List<PathsDto> pathsDtosList = pathsService.getFilesByUserUUIDAndFilePathANDDepth(userUUID, (lastPathsDto.getDepth() + 1 == 1 ? lastPathsDto.getPath() + "_%" : lastPathsDto.getPath() + "/_%"), lastPathsDto.getDepth() + 1);
		json.put("pathsDtosList", pathsDtosList);
		
		this.writeJson(json.toString(), response);
	}
	
	@ModelAttribute
	@RequestMapping("/restoreRecycleFiles")
	public void restoreRecycleFilesOrFolders(HttpServletRequest request,HttpServletResponse response, String userUUID, String filesJsonArray) throws Exception {
		JSONObject json = new JSONObject();
		json.put("message", "");
		
		//删除文件或文件夹列表
		JSONArray jsonArray = JSONArray.fromObject(filesJsonArray);
		for(int i = 0; i < jsonArray.size(); i++) {
			//判断文件或文件夹
			JSONObject jsonObject = JSONObject.fromObject(jsonArray.get(i));

			PathsDto pathsDto = recycleService.getFilesByPathsUUID(jsonObject.getString("pathsUUID"));
			String modTime = this.getStringDate(new Date());
			pathsDto.setModTime(modTime);
			pathsDto.setFilename(pathsDto.getFilename().replace("*", ""));
			
			//添加父文件夹
			String filename = pathsDto.getFilename();
			String path = pathsDto.getPath();
			int depth = pathsDto.getDepth();
			for(int j = 0; j < pathsDto.getDepth() - 1; j++) {
				int filenameIndex = path.lastIndexOf("/" + filename.replace("/", ""));
				String lastPath = path.substring(0, filenameIndex);
				
				int filenameIndex1 = lastPath.lastIndexOf("/");
				String lastFilename = lastPath.substring(filenameIndex1, lastPath.length());
				
				int lastDepth = depth - 1;
				
//				System.out.println(lastPath + "----------" + lastFilename);
				
				PathsDto pathsDto2 = pathsService.getFolderByPathANDUserUUED(userUUID, lastPath);
				if(pathsDto2 == null) {
					String pathsUUID1 = this.getUUID();
			        String size = "--";
			        String addTime = this.getStringDate(new Date());
			        String modTime1 = this.getStringDate(new Date());
					
					//将文件夹信息存入数据库
		            PathsDto pathsDto3 = new PathsDto();
		            pathsDto3.setPathsUUID(pathsUUID1);
		            pathsDto3.setUserUUID(userUUID);
		            pathsDto3.setPath(lastPath);
		            pathsDto3.setFilename(lastFilename);
		            pathsDto3.setSize(size);
		            pathsDto3.setDepth(lastDepth);
		            pathsDto3.setAddTime(addTime);
		            pathsDto3.setModTime(modTime1);
		            pathsService.addPaths(pathsDto3);
				}
				
				filename = lastFilename;
				path = lastPath;
				depth = lastDepth;
			}
			
			if(jsonObject.getString("filename").indexOf("/") != -1) {
				PathsDto pathsDto2 = pathsService.getFolderByPathANDUserUUED(userUUID, pathsDto.getPath());
				if(pathsDto2 == null) {
					pathsService.addPaths(pathsDto);
				}
				
				//删除当前或子目录下文件
				List<PathsDto> pathsDtosList = recycleService.getReNameOrDeleteOrMoveFilesByUserUUIDAndFileNameANDDepth(userUUID, jsonObject.getString("path") + "/_%", jsonObject.getInt("depth") + 1);
				for(int j = 0; j < pathsDtosList.size(); j++) {
					PathsDto pathsDto3 = pathsService.getFolderByPathANDUserUUED(userUUID, pathsDtosList.get(j).getPath());
					if(pathsDto3 == null) {
						pathsDtosList.get(j).setModTime(modTime);
						pathsService.addPaths(pathsDtosList.get(j));
					}
				}
				
				//删除数据库文件夹及其文件夹中的文件或文件夹信息
				recycleService.delPathsByPathsUUID(jsonObject.getString("pathsUUID"));
				recycleService.delPathsByUserUUIDAndFilePathAndDepth(userUUID, jsonObject.getString("path") + "/_%", jsonObject.getInt("depth") + 1);
			}else {
				PathsDto pathsDto2 = pathsService.getFolderByPathANDUserUUED(userUUID, pathsDto.getPath());
				if(pathsDto2 == null) {
					pathsService.addPaths(pathsDto);
				}
				
				//删除数据库文件信息
				recycleService.delPathsByPathsUUID(jsonObject.getString("pathsUUID"));
			}
		}
		
		//获取文件列表
		List<PathsDto> pathsDtosList = recycleService.getRecycleFilesByUserUUIDAndFileName(userUUID);
		if(pathsDtosList != null) {
			for(PathsDto pathsDto : pathsDtosList) {
				pathsDto.setFilename(pathsDto.getFilename().replace("*", ""));
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				long modDate = sdf.parse(pathsDto.getModTime()).getTime();
				long nowDate = new Date().getTime();
				int totalSeconds = (int) ((nowDate - modDate) / 1000);

				int daySeconds = 24 * 60 * 60;
				int hourSeconds = 60 * 60;
				int minuteSeconds = 60;
				
				if(totalSeconds >= 7 * daySeconds) {
					pathsDto.setDeleteTime("稍后");
				}else if(totalSeconds > 6 * daySeconds) {
					if(totalSeconds > (6 * daySeconds + 23 * hourSeconds)) {
						if(totalSeconds > (6 * daySeconds + 23 * hourSeconds + 59 * minuteSeconds)) {
							pathsDto.setDeleteTime("稍后");
						}else {
							pathsDto.setDeleteTime(((7 * daySeconds - totalSeconds) / minuteSeconds) + "分钟后");
						}
					}else {
						pathsDto.setDeleteTime(((7 * daySeconds - totalSeconds) / hourSeconds) + "小时后");
					}
				}else {
					pathsDto.setDeleteTime(((7 * daySeconds - totalSeconds) / daySeconds) + "天后");
				}
			}
		}else {
			json.put("message", "空的文件回收站！");
		}
		
		json.put("pathsDtosList", pathsDtosList);
		
		this.writeJson(json.toString(), response);
	}
	
	@ModelAttribute
	@RequestMapping("/getSearchFolders")
	public void getSearchFolders(HttpServletRequest request,HttpServletResponse response, String userUUID, String path, int depth) throws Exception {
		JSONObject json = new JSONObject();
		
		List<PathsDto> folders = new ArrayList<PathsDto>();
		PathsDto rootPathsDto = pathsService.getFolderByPathANDUserUUED(userUUID, "/");
		folders.add(rootPathsDto);
		

		List<PathsDto> pathsDtosList = pathsService.getFilesByUserUUIDAndFilePathANDDepth(userUUID, "/_%", 1);
		for(int i = 0; i < pathsDtosList.size(); i++) {
			if(path.indexOf(pathsDtosList.get(i).getPath() + "/") != -1) {
				folders.add(pathsDtosList.get(i));
				break;
			}
		}
		

//		System.out.println(path + " ------------- " + depth);
//		System.out.println(pathsDtosList.size());
//		System.out.println(folders.size());
		
		if(folders.size() > 0) {
			for(int i = 0; i < depth - 3; i++) {
				PathsDto laPathsDto = folders.get(folders.size() - 1);
				pathsDtosList = pathsService.getFilesByUserUUIDAndFilePathANDDepth(userUUID, laPathsDto.getPath() + "/_%", laPathsDto.getDepth() + 1);
				
				for(int j = 0; j < pathsDtosList.size(); j++) {
					if(path.indexOf(pathsDtosList.get(j).getPath() + "/") != -1) {
						folders.add(pathsDtosList.get(j));
						break;
					}
				}
			}
		}
		
		json.put("folders", folders);
		
		this.writeJson(json.toString(), response);
	}
	
	@ModelAttribute
	@RequestMapping("/getFileInfo")
	public void getFileInfo(HttpServletRequest request,HttpServletResponse response, String pathsUUID) throws Exception {
		JSONObject json = new JSONObject();
		
		json.put("message", "");
		PathsDto pathsDto = pathsService.getFilesByPathsUUID(pathsUUID);
		if(pathsDto != null) {
			UserDto userDto = userService.getUserDtosByUserUUID(pathsDto.getUserUUID());
			json.put("pathsDto", pathsDto);
			json.put("userDto", userDto);
		}else {
			json.put("message", "错误的文件ID！");
		}
		
		this.writeJson(json.toString(), response);
	}
	
	/**
	 * 移动文件或文件夹列表
	 * @param request
	 * @param response
	 * @param userUUID
	 * @param oldPathsUUID
	 * @param newPathsUUID
	 * @param filesJsonArray
	 * @throws Exception
	 */
	@ModelAttribute
	@RequestMapping("/moveFiles")
	public void moveFilesOrFolders(HttpServletRequest request,HttpServletResponse response, String userUUID, String newPathsUUID, String filesJsonArray) throws Exception {
		JSONObject json = new JSONObject();
		json.put("message", "");
		
//		String realPath = request.getServletContext().getRealPath("");
//		String fileSystem = realPath + "fileSystem";
		
		//初始化相对路径
		UserDto userDto = userService.getUserDtosByUserUUID(userUUID);
		String rootPath = "fileSystem/" + ((userDto.getUserName() == null || userDto.getUserName().equals("")) ? userDto.getRegEmail() : userDto.getUserName());
		
		//连接阿里OSS
		String serverUUID = "0e7be8dea2bf4f00a664cbd16366fbc0";
		ServerDto serverDto = serverService.getServerByServerUUID(serverUUID);
		String endpoint = serverDto.getEndPiontInternal();
		String accessKeyId = serverDto.getAccessKeyId();
		String accessKeySecret = serverDto.getSecretAccessKey();
		String bucketName = serverDto.getBucketName();
		OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId,accessKeySecret);
		
		//移动文件或文件夹列表
//		PathsDto oldPathsDto = pathsService.getFilesByPathsUUID(oldPathsUUID);
		PathsDto newPathsDto = pathsService.getFilesByPathsUUID(newPathsUUID);
		JSONArray jsonArray = JSONArray.fromObject(filesJsonArray);
		for(int i = 0; i < jsonArray.size(); i++) {
			JSONObject jsonObject = JSONObject.fromObject(jsonArray.get(i));
			
			int filenameIndex = jsonObject.getString("path").lastIndexOf(jsonObject.getInt("depth") == 1 ? jsonObject.getString("filename").replace("/", "") : "/" + jsonObject.getString("filename").replace("/", ""));
			String oldPathParentPath = jsonObject.getString("path").substring(0, filenameIndex);
			PathsDto oldPathsDto = pathsService.getFolderByPathANDUserUUED(userUUID, oldPathParentPath);
			
//			String oldPathParentPath = jsonObject.getString("path").replace("/" + jsonObject.getString("filename").replace("/", ""), "");
//			PathsDto oldPathsDto = pathsService.getFolderByPathANDUserUUED(userUUID, oldPathParentPath);
			
//			PathsDto oldPathsDto = null;
//			
//			System.out.println(jsonObject.getString("path"));
//			System.out.println("/" + jsonObject.getString("filename").replace("/", ""));
//			System.out.println(oldPathParentPath);
//			System.out.println(oldPathsDto == null);
//			System.out.println("---------------");
			
			if(oldPathsDto != null) {
				//获取文件或文件夹信息
				String oldPath = rootPath + jsonObject.getString("path");
				String newPath = "";
				String nPath = "";
				if(newPathsDto.getPath().equals("/")) {
					newPath = rootPath + newPathsDto.getPath() + jsonObject.getString("filename").replace("/", "");
					nPath = newPathsDto.getPath() + jsonObject.getString("filename").replace("/", "");
				}else {
					newPath = rootPath + newPathsDto.getPath() + "/" + jsonObject.getString("filename").replace("/", "");
					nPath = newPathsDto.getPath() + "/" + jsonObject.getString("filename").replace("/", "");
				}
				
				int nDepth = jsonObject.getInt("depth") - oldPathsDto.getDepth() + newPathsDto.getDepth();
				String modTime = this.getStringDate(new Date());
				
//				int nDepthNew = jsonObject.getInt("depth") - oldPathsDtoNew.getDepth() + newPathsDto.getDepth();
//				System.out.println(nDepth);
//				System.out.println(nDepthNew);
//				System.out.println("--------------------");
				
				//判断文件或文件夹
				if(jsonObject.getString("filename").indexOf("/") != -1) {
					
//					String cmd = "";
//					if(System.getProperty("os.name").toLowerCase().contains("windows")) {
//						cmd = "mv \"" + oldPath + "\" \"" + newPath + "\"";
//						writeToLog(request, cmd);
//					}else {
//						cmd = "mv " + oldPath.replace(" ", "\\ ") + " " + newPath.replace(" ", "\\ ");
//						writeToLog(request, cmd);
//					}
//					Process process = Runtime.getRuntime().exec(cmd);
//					process.waitFor();
					
//					FileUtils.moveDirectory(new File(oldPath), new File(newPath));
					
					//移动文件夹的文件或子文件夹
					List<PathsDto> pathsDtosList = pathsService.getReNameOrDeleteOrMoveFilesByUserUUIDAndFileNameANDDepth(userUUID, jsonObject.getString("path") + "/_%", jsonObject.getInt("depth") + 1);
					for(int j = 0; j < pathsDtosList.size(); j++) {
						
						String _nPath = pathsDtosList.get(j).getPath().replaceFirst(jsonObject.getString("path"), nPath);
						int _nDepth = pathsDtosList.get(j).getDepth() - jsonObject.getInt("depth") + nDepth;
						
						//移动阿里OSS文件
						if(pathsDtosList.get(j).getFilename().indexOf("/") == -1) {
							//拷贝阿里OSS文件
							String sourceObjectName = rootPath + pathsDtosList.get(j).getPath();
							String destinationObjectName = rootPath + _nPath;
							ossClient.copyObject(bucketName, sourceObjectName, bucketName, destinationObjectName);
							//删除阿里OSS文件
							ossClient.deleteObject(bucketName, sourceObjectName);
						}
						
						//修改数据库文件信息
						pathsService.movePathsByPathsUUID(_nPath, _nDepth, modTime, pathsDtosList.get(j).getPathsUUID());
					}
				}else {
//					String cmd = "mv '" + oldPath + "' '" + newPath + "'";
//					Process process = Runtime.getRuntime().exec(cmd);
//					process.waitFor();
					
//					File oldName = new File(oldPath);
//			        File newName = new File(newPath);
//			        oldName.renameTo(newName);
					
					//拷贝阿里OSS文件
					String sourceObjectName = oldPath;
					String destinationObjectName = newPath;
					ossClient.copyObject(bucketName, sourceObjectName, bucketName, destinationObjectName);
					//删除阿里OSS文件
					ossClient.deleteObject(bucketName, sourceObjectName);
				}
				
				//修改数据库文件信息
				pathsService.movePathsByPathsUUID(nPath, nDepth, modTime, jsonObject.getString("pathsUUID"));
			}
			
		}
		
		ossClient.shutdown();
		
		//获取文件列表
		List<PathsDto> pathsDtosList = pathsService.getFilesByUserUUIDAndFilePathANDDepth(userUUID, (newPathsDto.getDepth() + 1 == 1 ? newPathsDto.getPath() + "_%" : newPathsDto.getPath() + "/_%"), newPathsDto.getDepth() + 1);
		json.put("pathsDtosList", pathsDtosList);
		
		this.writeJson(json.toString(), response);
	}
	
//	public void writeToLog(HttpServletRequest request, String i) throws Exception {
//        String fileName = request.getSession().getServletContext().getRealPath("") + "Log.txt";
//        FileWriter writeFile = new FileWriter(fileName, true);
//        BufferedWriter writer = new BufferedWriter(writeFile);
//        writer.write(i + "\n");
//        
//        writer.flush();
//        writeFile.close();
//    }
	
	/**
	 * 获取文件列表
	 * @param request
	 * @param response
	 * @param userUUID
	 * @param path
	 * @param depth
	 * @throws Exception
	 */
	@ModelAttribute
	@RequestMapping("/getFiles")
	public void getFileAndFolder(HttpServletRequest request, HttpServletResponse response, String userUUID, String path, int depth) throws Exception {
		JSONObject json = new JSONObject();
		json.put("message", "");
		
//		String realPath = request.getServletContext().getRealPath("");
//		String fileSystem = realPath + "fileSystem";
//		
//		UserDto userDto = userService.getUserDtosByUserUUID(userUUID);
//		String rootPath = fileSystem + "/" + userDto.getUserName();
		
		//将根目录信息存入数据库
		PathsDto rootFolder = pathsService.getFolderByPathANDUserUUED(userUUID, "/");
		if(rootFolder == null) {
			
//			file.mkdirs();
	        
	        String pathsUUID = this.getUUID();
	        String thePath = "/";
	        String thefilename = "/";
	        String size = "--";
	        int theDepth = 0;
	        String addTime = this.getStringDate(new Date());
	        String modTime = this.getStringDate(new Date());
	        
	        PathsDto pathsDto = new PathsDto();
	        pathsDto.setPathsUUID(pathsUUID);
	        pathsDto.setUserUUID(userUUID);
	        pathsDto.setPath(thePath);
	        pathsDto.setFilename(thefilename);
	        pathsDto.setSize(size);
	        pathsDto.setDepth(theDepth);
	        pathsDto.setAddTime(addTime);
	        pathsDto.setModTime(modTime);
	        pathsService.addPaths(pathsDto);
		}
		
		//获取文件列表
		List<PathsDto> pathsDtosList = pathsService.getFilesByUserUUIDAndFilePathANDDepth(userUUID, (depth == 1 ? path + "_%" : path + "/_%"), depth);
		json.put("pathsDtosList", pathsDtosList);

        if(depth == 1) {
			json.put("rootPathUUID", pathsService.getRootPathsUUIDByUserUUID(userUUID));
        }
        
      //获取文件总大小
        double totalSizes = 0;
		List<PathsDto> pathsDtosListBySize = pathsService.getFilesByUserUUIDAndSize(userUUID);
		for(int i = 0; i < pathsDtosListBySize.size(); i++) {
			double fileSize = Double.parseDouble(pathsDtosListBySize.get(i).getSize().replace("MB", ""));
			totalSizes += fileSize;
		}
		
		json.put("totalSizes", totalSizes);
		
		this.writeJson(json.toString(), response);
	}
	
	@ModelAttribute
	@RequestMapping("/getDefaultFiles")
	public void getDefaultFiles(HttpServletRequest request, HttpServletResponse response, String userUUID, String path, int depth) throws Exception {
		JSONObject json = new JSONObject();
		json.put("message", "");
		
		List<PathsDto> folders = new ArrayList<PathsDto>();
		PathsDto rootPathsDto = pathsService.getFolderByPathANDUserUUED(userUUID, "/");
		folders.add(rootPathsDto);
		

		List<PathsDto> pathsDtosListFolders = pathsService.getFilesByUserUUIDAndFilePathANDDepth(userUUID, "/_%", 1);
		for(int i = 0; i < pathsDtosListFolders.size(); i++) {
			if(path.indexOf(pathsDtosListFolders.get(i).getPath() + "/") != -1) {
				folders.add(pathsDtosListFolders.get(i));
				break;
			}
		}
		
		if(folders.size() > 0) {
			for(int i = 0; i < depth - 3; i++) {
				PathsDto laPathsDto = folders.get(folders.size() - 1);
				pathsDtosListFolders = pathsService.getFilesByUserUUIDAndFilePathANDDepth(userUUID, laPathsDto.getPath() + "/_%", laPathsDto.getDepth() + 1);
				
				for(int j = 0; j < pathsDtosListFolders.size(); j++) {
					if(path.indexOf(pathsDtosListFolders.get(j).getPath() + "/") != -1) {
						folders.add(pathsDtosListFolders.get(j));
						break;
					}
				}
			}
		}
		
		json.put("folders", folders);
		
		PathsDto nowFolder = pathsService.getFolderByPathANDUserUUED(userUUID, path);
		json.put("nowFolder", nowFolder);
		
		//获取文件列表
		List<PathsDto> pathsDtosList = pathsService.getFilesByUserUUIDAndFilePathANDDepth(userUUID, (depth == 1 ? path + "_%" : path + "/_%"), depth);
		json.put("pathsDtosList", pathsDtosList);

        if(depth == 1) {
			json.put("rootPathUUID", pathsService.getRootPathsUUIDByUserUUID(userUUID));
        }
        
      //获取文件总大小
        double totalSizes = 0;
		List<PathsDto> pathsDtosListBySize = pathsService.getFilesByUserUUIDAndSize(userUUID);
		for(int i = 0; i < pathsDtosListBySize.size(); i++) {
			double fileSize = Double.parseDouble(pathsDtosListBySize.get(i).getSize().replace("MB", ""));
			totalSizes += fileSize;
		}
		
		json.put("totalSizes", totalSizes);
		
		this.writeJson(json.toString(), response);
	}
	
	@ModelAttribute
	@RequestMapping("/getRecycleFiles")
	public void getRecycleFiles(HttpServletRequest request, HttpServletResponse response, String userUUID) throws Exception {
		JSONObject json = new JSONObject();
		json.put("message", "");
		
		boolean flag = false;
		List<PathsDto> pathsDtosList = recycleService.getRecycleFilesByUserUUIDAndFileName(userUUID);
		if(pathsDtosList != null) {
			for(PathsDto pathsDto : pathsDtosList) {
				pathsDto.setFilename(pathsDto.getFilename().replace("*", ""));
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				long modDate = sdf.parse(pathsDto.getModTime()).getTime();
				long nowDate = new Date().getTime();
				int totalSeconds = (int) ((nowDate - modDate) / 1000);
				
				int daySeconds = 24 * 60 * 60;
				int hourSeconds = 60 * 60;
				int minuteSeconds = 60;
				
//				System.out.println(totalSeconds);
//				System.out.println(daySeconds);
//				System.out.println(totalSeconds / (daySeconds * 1.0));
				
				if(totalSeconds >= 7 * daySeconds) {
					//初始化相对路径
					UserDto userDto = userService.getUserDtosByUserUUID(userUUID);
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
						List<PathsDto> pathsDtosListDelete = recycleService.getReNameOrDeleteOrMoveFilesByUserUUIDAndFileNameANDDepth(userUUID, pathsDtoDelete.getPath() + "/_%", pathsDtoDelete.getDepth() + 1);
						for(int i = 0; i < pathsDtosListDelete.size(); i++) {
							if(pathsDtosListDelete.get(i).getFilename().indexOf("/") == -1) {
								//删除阿里OSS文件
								String objectName = rootPath + pathsDtosListDelete.get(i).getPath();
								ossClient.deleteObject(bucketName, objectName);
							}
						}
						
						//删除数据库文件夹及其文件夹中的文件或文件夹信息
						recycleService.delPathsByPathsUUID(pathsDto.getPathsUUID());
						recycleService.delPathsByUserUUIDAndFilePathAndDepth(userUUID, pathsDto.getPath() + "/_%", pathsDto.getDepth() + 1);
					}else {
						//删除阿里OSS文件
						String path = pathsDto.getPath();
						String objectName = rootPath + path;
						ossClient.deleteObject(bucketName, objectName);
						
						//删除数据库文件信息
						recycleService.delPathsByPathsUUID(pathsDto.getPathsUUID());
					}
					
					ossClient.shutdown();
					
					flag = true;
				}else if(totalSeconds > 6 * daySeconds) {
					if(totalSeconds > (6 * daySeconds + 23 * hourSeconds)) {
						if(totalSeconds > (6 * daySeconds + 23 * hourSeconds + 59 * minuteSeconds)) {
							pathsDto.setDeleteTime("稍后");
						}else {
							pathsDto.setDeleteTime(((7 * daySeconds - totalSeconds) / minuteSeconds) + "分钟后");
						}
					}else {
						pathsDto.setDeleteTime(((7 * daySeconds - totalSeconds) / hourSeconds) + "小时后");
					}
				}else {
					pathsDto.setDeleteTime(((7 * daySeconds - totalSeconds) / daySeconds) + "天后");
				}
			}
		}else {
			json.put("message", "空的文件回收站！");
		}
		
		if(flag) {
			pathsDtosList = recycleService.getRecycleFilesByUserUUIDAndFileName(userUUID);
			if(pathsDtosList != null) {
				for(PathsDto pathsDto : pathsDtosList) {
					pathsDto.setFilename(pathsDto.getFilename().replace("*", ""));
					
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					long modDate = sdf.parse(pathsDto.getModTime()).getTime();
					long nowDate = new Date().getTime();
					int totalSeconds = (int) ((nowDate - modDate) / 1000);

					int daySeconds = 24 * 60 * 60;
					int hourSeconds = 60 * 60;
					int minuteSeconds = 60;
					
					if(totalSeconds >= 7 * daySeconds) {
						pathsDto.setDeleteTime("稍后");
					}else if(totalSeconds > 6 * daySeconds) {
						if(totalSeconds > (6 * daySeconds + 23 * hourSeconds)) {
							if(totalSeconds > (6 * daySeconds + 23 * hourSeconds + 59 * minuteSeconds)) {
								pathsDto.setDeleteTime("稍后");
							}else {
								pathsDto.setDeleteTime(((7 * daySeconds - totalSeconds) / minuteSeconds) + "分钟后");
							}
						}else {
							pathsDto.setDeleteTime(((7 * daySeconds - totalSeconds) / hourSeconds) + "小时后");
						}
					}else {
						pathsDto.setDeleteTime(((7 * daySeconds - totalSeconds) / daySeconds) + "天后");
					}
				}
			}else {
				json.put("message", "空的文件回收站！");
			}
		}
		
		json.put("pathsDtosList", pathsDtosList);
		this.writeJson(json.toString(), response);
	}
	
	/**
	 * 文件或文件夹重命名
	 * @param request
	 * @param response
	 * @param userUUID
	 * @param lastPathsUUID
	 * @param pathsUUID
	 * @param newFilename
	 * @throws Exception
	 */
	@ModelAttribute
	@RequestMapping("/renameFile")
	public void renameFileOrFolder(HttpServletRequest request,HttpServletResponse response, String userUUID, String pathsUUID, String newFilename) throws Exception {
		JSONObject json = new JSONObject();
		json.put("message", "");
		
//		String realPath = request.getServletContext().getRealPath("");
//		String fileSystem = realPath + "fileSystem";
		
		//初始化相对路径
		UserDto userDto = userService.getUserDtosByUserUUID(userUUID);
		String rootPath = "fileSystem/" + ((userDto.getUserName() == null || userDto.getUserName().equals("")) ? userDto.getRegEmail() : userDto.getUserName());
		
		//连接阿里OSS
		String serverUUID = "0e7be8dea2bf4f00a664cbd16366fbc0";
		ServerDto serverDto = serverService.getServerByServerUUID(serverUUID);
		String endpoint = serverDto.getEndPiontInternal();
		String accessKeyId = serverDto.getAccessKeyId();
		String accessKeySecret = serverDto.getSecretAccessKey();
		String bucketName = serverDto.getBucketName();
		OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId,accessKeySecret);
		
		
		PathsDto pathsDto = pathsService.getFilesByPathsUUID(pathsUUID);
		
//		String lastPath = pathsDto.getPath().replace("/" + pathsDto.getFilename().replace("/", ""), "");
//		PathsDto lastPathsDto = pathsService.getFolderByPathANDUserUUED(userUUID, lastPath);
		
		int filenameIndex = pathsDto.getPath().lastIndexOf(pathsDto.getDepth() == 1 ? pathsDto.getFilename().replace("/", "") : "/" + pathsDto.getFilename().replace("/", ""));
		String lastPath = pathsDto.getPath().substring(0, filenameIndex);
		PathsDto lastPathsDto = pathsService.getFolderByPathANDUserUUED(userUUID, lastPath);
		
//		String lastPath = pathsDto.getPath().replace("/" + pathsDto.getFilename().replace("/", ""), "");
//		PathsDto lasPathsDtoNew = pathsService.getFolderByPathANDUserUUED(userUUID, lastPath);
//		System.out.println(lastPathsUUID);
//		System.out.println(lasPathsDtoNew.getPathsUUID());
//		System.out.println("----------------------------------------------------" );
		
		//获取文件路径信息
		String oldFilePath = rootPath + pathsDto.getPath();
		String newFilePath = "";
		if(lastPathsDto.getPath().equals("/")) {
			newFilePath = rootPath + lastPathsDto.getPath() + newFilename;
		}else {
			newFilePath = rootPath + lastPathsDto.getPath() + "/" + newFilename;
		}
		
//		File oldfile = new File(oldFilePath);
//		File newfile = new File(newFilePath);
//		oldfile.renameTo(newfile);
		
//		String cmd = "mv " + oldFilePath + " " + newFilePath;
//		Process process = Runtime.getRuntime().exec(cmd);
//		process.waitFor();
		
		//修改文件夹下的文件、子文件夹的路径
		String modTime = this.getStringDate(new Date());
		if(pathsDto.getFilename().indexOf("/") != -1) {
			
			//修改数据库文件信息
			if(lastPathsDto.getPath().equals("/")) {
				pathsService.modPathsByPathsUUID(lastPathsDto.getPath() + newFilename, "/" + newFilename, modTime, pathsUUID);
			}else {
				pathsService.modPathsByPathsUUID(lastPathsDto.getPath() + "/" + newFilename, "/" + newFilename, modTime, pathsUUID);
			}
			
			//修改文件夹下的文件信息
			List<PathsDto> pathsDtosList = pathsService.getReNameOrDeleteOrMoveFilesByUserUUIDAndFileNameANDDepth(userUUID, pathsDto.getPath() + "/_%", pathsDto.getDepth() + 1);
			for(int i = 0; i < pathsDtosList.size(); i++) {
				
				String nPath = "";
				if(lastPathsDto.getPath().equals("/")) {
					nPath = pathsDtosList.get(i).getPath().replaceFirst(pathsDto.getPath(), lastPathsDto.getPath() + newFilename);
				}else {
					nPath = pathsDtosList.get(i).getPath().replaceFirst(pathsDto.getPath(), lastPathsDto.getPath() + "/" + newFilename);
				}
				
				if(pathsDtosList.get(i).getFilename().lastIndexOf("/") == -1) {
					//拷贝阿里OSS文件
					String sourceObjectName = rootPath + pathsDtosList.get(i).getPath();
					String destinationObjectName = rootPath + nPath;
					ossClient.copyObject(bucketName, sourceObjectName, bucketName, destinationObjectName);
					//删除阿里OSS文件
					ossClient.deleteObject(bucketName, sourceObjectName);
				}
				
				pathsService.modPathsByPathsUUID(nPath, pathsDtosList.get(i).getFilename(), modTime, pathsDtosList.get(i).getPathsUUID());
			}
		}else {
			//拷贝阿里OSS文件
			String sourceObjectName = oldFilePath;
			String destinationObjectName = newFilePath;
			ossClient.copyObject(bucketName, sourceObjectName, bucketName, destinationObjectName);
			//删除阿里OSS文件
			ossClient.deleteObject(bucketName, sourceObjectName);
			
			//修改数据库文件信息
			pathsService.modPathsByPathsUUID(lastPathsDto.getPath() + "/" + newFilename, newFilename, modTime, pathsUUID);
		}
		
		ossClient.shutdown();
		
		//获取文件列表
		List<PathsDto> pathsDtosList = pathsService.getFilesByUserUUIDAndFilePathANDDepth(userUUID, (lastPathsDto.getDepth() + 1 == 1 ? lastPathsDto.getPath() + "_%" : lastPathsDto.getPath() + "/_%"), lastPathsDto.getDepth() + 1);
		json.put("pathsDtosList", pathsDtosList);
		
		this.writeJson(json.toString(), response);
	}
	
//	@ModelAttribute
//	@RequestMapping("/moveFile")
//	public void moveFileOrFolder(HttpServletRequest request,HttpServletResponse response, String oldPathsUUID, String userUUID, String newPathsUUID) throws Exception {
//		String realPath = request.getServletContext().getRealPath("");
//		String fileSystem = realPath + "fileSystem";
//		
//		UserDto userDto = userService.getUserDtosByUserUUID(userUUID);
//		String rootPath = fileSystem + "/" + userDto.getUserName();
//		
//		PathsDto oldPathsDto = pathsService.getFilesByPathsUUID(oldPathsUUID);
//		PathsDto newPathsDto = pathsService.getFilesByPathsUUID(newPathsUUID);
//		
//		if(oldPathsDto.getFilename().indexOf("/") != -1) {
//			String oldPath = rootPath + oldPathsDto.getPath();
//			String newPath = rootPath + newPathsDto.getPath() + oldPathsDto.getFilename();
//			String cmd = "mv " + oldPath + " " + newPath;
//			Process process = Runtime.getRuntime().exec(cmd);
//			process.waitFor();
//			
//			List<PathsDto> pathsDtosList = pathsService.getFilesByUserUUIDAndFilePathANDDepth(userUUID, "%" + oldPathsDto.getPath() + "%", oldPathsDto.getDepth());
//			String oldFolderName = oldPathsDto.getFilename().replace("/", "");
//			for(int i = 0; i < pathsDtosList.size(); i++) {
//				
//				String[] paths = pathsDtosList.get(i).getPath().split("/");
//				String newFilePath = "";
//				for(int j = 0; j < paths.length; j++) {
//					if(paths[j].equals(oldFolderName) && j == oldPathsDto.getDepth() - 1) {
//						for(int k = j + 1; k < paths.length; k++) {
//							newFilePath += ("/" + paths[k]);
//						}
//						break;
//					}
//				}
//				if(newFilePath.equals("")) {
//					newFilePath = newPathsDto.getPath() + "/" + oldPathsDto.getFilename();
//				}else {
//					newFilePath = newPathsDto.getPath() + newFilePath;
//				}
//				
//				int newFileDepth = pathsDtosList.get(i).getDepth() - oldPathsDto.getDepth() + newPathsDto.getDepth() + 1;
//				String modTime = this.getStringDate(new Date());
//				
//				pathsService.movePathsByPathsUUID(newFilePath, newFileDepth, modTime, pathsDtosList.get(i).getPathsUUID());
//			}
//		}else {
//			String oldPath = rootPath + oldPathsDto.getPath();
//			String newPath = rootPath + newPathsDto.getPath() + "/" + oldPathsDto.getFilename();
//			String cmd = "mv " + oldPath + " " + newPath;
//			Process process = Runtime.getRuntime().exec(cmd);
//			process.waitFor();
//			
//			String modTime = this.getStringDate(new Date());
//			pathsService.movePathsByPathsUUID(newPath, newPathsDto.getDepth() + 1, modTime, oldPathsUUID);
//		}
//	}
//	
//	@ModelAttribute
//	@RequestMapping("/test")
//	public void test(HttpServletRequest request,HttpServletResponse response, String json) throws Exception {
//		JSONArray jsonArray = JSONArray.fromObject(json);
//		System.out.println(jsonArray.size());
//	}

}
