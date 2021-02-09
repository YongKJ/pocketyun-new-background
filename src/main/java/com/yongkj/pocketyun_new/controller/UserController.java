package com.yongkj.pocketyun_new.controller;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Type;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.yongkj.pocketyun_new.basic.controller.BasicController;
import com.yongkj.pocketyun_new.dto.EmailDto;
import com.yongkj.pocketyun_new.dto.PathsDto;
import com.yongkj.pocketyun_new.dto.ServerDto;
import com.yongkj.pocketyun_new.dto.UserDto;
import com.yongkj.pocketyun_new.service.EmailService;
import com.yongkj.pocketyun_new.service.PathsService;
import com.yongkj.pocketyun_new.service.RecycleService;
import com.yongkj.pocketyun_new.service.ServerService;
import com.yongkj.pocketyun_new.service.UserService;

import net.sf.json.JSONObject;


@Controller
@RequestMapping("/userController")
public class UserController extends BasicController {
	
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
	
	@Autowired
	@Qualifier("emailService")
	private EmailService emailService;
	
	/**
	 * 用户登录
	 * @param request
	 * @param response
	 * @param userName
	 * @param password
	 */
	@ModelAttribute
	@RequestMapping("/login")
	public void userLogin(HttpServletRequest request,HttpServletResponse response, String userName, String password) throws Exception {
		JSONObject json = new JSONObject();
		json.put("message", "");
		String md5Password=this.md5(password);
		UserDto userDtoUserName = userService.getUserByUserNameAndPassword(userName, md5Password);
		UserDto userDtoEmail = userService.getUserByRegEmailAndPassword(userName, md5Password);
		
		if(userDtoUserName == null && userDtoEmail == null) {
			json.put("message", "账号或密码错误！");
		}else {
//			HttpSession session = request.getSession();
//			
//			session.setAttribute("username", userDto.getUserName());
//			session.setAttribute("admin", userDto.getAdmin());
//			session.setAttribute("id", userDto.getUserUUID());
			
//			Date datetime = this.getDate();
//			if(userDto.getLoginTime() == null) {
//				session.setAttribute("datatime", datetime);
//			}else{
//				session.setAttribute("datatime", userDto.getLoginTime());
//			}
			
			String userUUID = "";
			if(userDtoUserName != null) {
				userUUID = userDtoUserName.getUserUUID();
			}else {
				userUUID = userDtoEmail.getUserUUID();
			}
			
			List<PathsDto> pathsDtosList = recycleService.getRecycleFilesByUserUUIDAndFileName(userUUID);
			if(pathsDtosList != null) {
				for(PathsDto pathsDto : pathsDtosList) {
					pathsDto.setFilename(pathsDto.getFilename().replace("*", ""));
					
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					long modDate = sdf.parse(pathsDto.getModTime()).getTime();
					long nowDate = new Date().getTime();
					int totalSeconds = (int) ((nowDate - modDate) / 1000);
					
					int daySeconds = 24 * 60 * 60;
					
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
					}
				}
			}
			
			if(userDtoUserName != null) {
				String loginTime = this.getStringDate(new Date());
				userService.modUserLoginTimeByUserUUID(loginTime, userDtoUserName.getUserUUID());
				json.put("userUUID", userDtoUserName.getUserUUID());
				if(userDtoUserName.getUserName() == null) {
					json.put("userName", userDtoUserName.getRegEmail());
				}else {
					if(userDtoUserName.getUserName().equals("")) {
						json.put("userName", userDtoUserName.getRegEmail());
					}else {
						json.put("userName", userDtoUserName.getUserName());
					}
				}
			}else {
				String loginTime = this.getStringDate(new Date());
				userService.modUserLoginTimeByUserUUID(loginTime, userDtoEmail.getUserUUID());
				json.put("userUUID", userDtoEmail.getUserUUID());
				if(userDtoEmail.getUserName() == null) {
					json.put("userName", userDtoEmail.getRegEmail());
				}else {
					if(userDtoEmail.getUserName().equals("")) {
						json.put("userName", userDtoEmail.getRegEmail());
					}else {
						json.put("userName", userDtoEmail.getUserName());
					}
				}
			}
		}
		this.writeJson(json.toString(), response);
	}
	
	/**
	 * 使用cookie自动登录
	 * @param request
	 * @param response
	 * @param userUUID
	 */
	@ModelAttribute
	@RequestMapping("/autoLogin")
	public void userAutoLogin(HttpServletRequest request,HttpServletResponse response, String userUUID) {
		JSONObject json = new JSONObject();
		json.put("message", "");
		
		UserDto userDto = userService.getUserDtosByUserUUID(userUUID);
		if(userDto == null) {
			json.put("message", "用户ID错误！");
		}else {
			String loginTime = this.getStringDate(new Date());
			userService.modUserLoginTimeByUserUUID(loginTime, userDto.getUserUUID());
			json.put("userUUID", userDto.getUserUUID());
			if(userDto.getUserName() == null) {
				json.put("userName", userDto.getRegEmail());
			}else {
				if(userDto.getUserName().equals("")) {
					json.put("userName", userDto.getRegEmail());
				}else {
					json.put("userName", userDto.getUserName());
				}
			}
		}
		
		this.writeJson(json.toString(), response);
	}
	
	/**
	 * 重置密码
	 * @param request
	 * @param response
	 * @param userName
	 * @param newPassword
	 */
	@ModelAttribute
	@RequestMapping("/resetPassword")
	public void resetPassword(HttpServletRequest request,HttpServletResponse response, String regEmail, String newPassword) {
		JSONObject json = new JSONObject();
		json.put("message", "");
		
		UserDto userDto = userService.getUserDtosByRegEmail(regEmail);
		String newMd5Password = this.md5(newPassword);
		if(userDto != null) {
			userService.modUserPasswordByUserUUID(newMd5Password, userDto.getUserUUID());
		}else {
			json.put("message", "请输入正确的邮箱地址！");
		}
		
		this.writeJson(json.toString(), response);
	}
	
//	@ModelAttribute
//	@RequestMapping(value="/loginOut")
//	public void loginOut(HttpServletRequest request,HttpServletResponse response) {
//		JSONObject json = new JSONObject();
//		json.put("message", "");
//		this.writeJson(json.toString(), response);
//	}
	
	/**
	 * 用户注册
	 * @param request
	 * @param response
	 * @param userName
	 * @param password
	 * @param regSex
	 * @param regAge
	 * @param regEmail
	 */
	@ModelAttribute
	@RequestMapping("/register")
	public void userRegister(HttpServletRequest request,HttpServletResponse response, String password, String regEmail) {
		JSONObject json = new JSONObject();
		json.put("message", "");
		
		UserDto judgeUser = userService.getUserDtosByRegEmail(regEmail);
		if(judgeUser != null) {
			json.put("message", "邮箱地址已存在！");
		}else {
			
			//将用户信息存入数据库
			UserDto userDto = new UserDto();
			String userUUID = this.getUUID();
			String md5Password = this.md5(password);
			String regTime=this.getStringDate(new Date());
			
			userDto.setUserUUID(userUUID);
			userDto.setUserName("");
			userDto.setPassword(md5Password);
			userDto.setRegSex("保密");
			userDto.setRegAge(0);
			userDto.setRegEmail(regEmail);
			userDto.setRegTime(regTime);
			userDto.setAdmin(regEmail.equals("dxj1718874198@gmail.com") ? "1" : "0");
			
			userService.addUser(userDto);
		}
		
		this.writeJson(json.toString(), response);
	}
	
	/**
	 * 删除用户以及用户数据
	 * @param request
	 * @param response
	 * @param userName
	 * @throws Exception
	 */
	@ModelAttribute
	@RequestMapping("/delUser")
	public void delUser(HttpServletRequest request,HttpServletResponse response, String userName) throws Exception {
		JSONObject json = new JSONObject();
		json.put("message", "");

		
//		String realPath = request.getServletContext().getRealPath("");
//		String fileSystem = realPath + "fileSystem";

		//初始化相对路径
		String userUUID = userService.getUserUUIDByUserName(userName);
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
		
//		File file = new File(rootPath);
//		if (file.exists()) {
////			String cmd = "rm -r " + rootPath;
////			Process process = Runtime.getRuntime().exec(cmd);
////			process.waitFor();
//			
//			FileUtils.deleteQuietly(new File(rootPath));
//		}
		
		//删除用户数据
		List<PathsDto> pathsDtosList = pathsService.getReNameOrDeleteOrMoveFilesByUserUUIDAndFileNameANDDepth(userUUID, "/_%", 1);
		for(int i = 0; i < pathsDtosList.size(); i++) {
			if(pathsDtosList.get(i).getFilename().indexOf("/") == -1) {
				//删除阿里OSS文件
				String objectName = rootPath + pathsDtosList.get(i).getPath();
				ossClient.deleteObject(bucketName, objectName);
			}
		}
		ossClient.shutdown();
		
		//删除数据库用户信息
		pathsService.delPathsByUserUUID(userUUID);
		recycleService.delPathsByUserUUID(userUUID);
		userService.delUserByUserUUID(userUUID);
		
		//获取用户列表
		List<UserDto> userDtosList = userService.getUserDtos();
		json.put("usersList", userDtosList);
		
		this.writeJson(json.toString(), response);
	}
	
	/**
	 * 获取用户信息
	 * @param request
	 * @param response
	 * @param userUUID
	 */
	@ModelAttribute
	@RequestMapping("/getUser")
	public void getUsersByUUID(HttpServletRequest request,HttpServletResponse response, String userUUID) {
		JSONObject json = new JSONObject();
		json.put("message", "");
		
		UserDto userDto = userService.getUserDtosByUserUUID(userUUID);
		if(userDto != null) {
			json.put("userUUID", userDto.getUserUUID());
			json.put("userName", userDto.getUserName());
			json.put("password", userDto.getPassword());
			json.put("regSex", userDto.getRegSex());
			json.put("regAge", userDto.getRegAge());
			json.put("regEmail", userDto.getRegEmail());
		}else {
			json.put("message", "无用户信息！");
		}
		
		this.writeJson(json.toString(), response);
	}
	
	/**
	 * 获取用户列表，用户使用空间
	 * @param request
	 * @param response
	 */
	@ModelAttribute
	@RequestMapping("/getUsers")
	public void getUsers(HttpServletRequest request,HttpServletResponse response) {
		JSONObject json = new JSONObject();
		json.put("message", "");
		
		//获取用户列表
		List<UserDto> userDtosList = userService.getUserDtos();
		json.put("usersList", userDtosList);
		
		//获取用户使用空间
		String[] userSpace = new String[userDtosList.size()];
		for(int i = 0; i < userDtosList.size(); i++) {
			double totalSizes = 0;
			List<PathsDto> pathsDtosListBySize = pathsService.getFilesByUserUUIDAndSize(userDtosList.get(i).getUserUUID());
			for(int j = 0; j < pathsDtosListBySize.size(); j++) {
				double fileSize = Double.parseDouble(pathsDtosListBySize.get(j).getSize().replace("MB", ""));
				totalSizes += fileSize;
			}
			userSpace[i] = String.valueOf(totalSizes);
		}
		json.put("userSpace", userSpace);
		
		this.writeJson(json.toString(), response);
	}
	
	/**
	 * 修改用户信息包括密码
	 * @param request
	 * @param response
	 * @param userName
	 * @param password
	 * @param regSex
	 * @param regAge
	 * @param regEmail
	 * @param userUUID
	 */
	
	@ModelAttribute
	@RequestMapping("/operateUser")
	public void operateUser(HttpServletRequest request,HttpServletResponse response, String userName, String password, String regSex, String regAge, String regEmail, String userUUID) {
		JSONObject json = new JSONObject();
		json.put("message", "");
		
		String md5Password = this.md5(password);
		userService.modUserAndPasswordByUserUUID(userName, md5Password, regSex, Integer.parseInt(regAge), regEmail, userUUID);
		
		this.writeJson(json.toString(), response);
	}
	
	/**
	 * 修改用户信息
	 * @param request
	 * @param response
	 * @param userName
	 * @param password
	 * @param regSex
	 * @param regAge
	 * @param regEmail
	 * @param userUUID
	 */
	@ModelAttribute
	@RequestMapping("/modUser")
	public void modUser(HttpServletRequest request,HttpServletResponse response, String userName, String regSex, String regAge, String regEmail, String userUUID) {
		JSONObject json = new JSONObject();
		json.put("message", "");
		
		UserDto userDto1 = userService.getUserByUserName(userName);
		if(userDto1 == null) {
			UserDto userDto = userService.getUserDtosByUserUUID(userUUID);
			
			if(userDto.getUserName() != null && !userDto.getUserName().equals(userName)) {
				String oldRootPath = "fileSystem/" + ((userDto.getUserName() == null || userDto.getUserName().equals("")) ? userDto.getRegEmail() : userDto.getUserName());
				String newRootPath = "fileSystem/" + userName;
				
				//连接阿里OSS
				String serverUUID = "0e7be8dea2bf4f00a664cbd16366fbc0";
				ServerDto serverDto = serverService.getServerByServerUUID(serverUUID);
				String endpoint = serverDto.getEndPiontInternal();
				String accessKeyId = serverDto.getAccessKeyId();
				String accessKeySecret = serverDto.getSecretAccessKey();
				String bucketName = serverDto.getBucketName();
				OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId,accessKeySecret);
				
				//修改用户数据
				List<PathsDto> pathsDtosList = pathsService.getReNameOrDeleteOrMoveFilesByUserUUIDAndFileNameANDDepth(userUUID, "/_%", 1);
				for(int i = 0; i < pathsDtosList.size(); i++) {
					if(pathsDtosList.get(i).getFilename().indexOf("/") == -1) {
						//拷贝阿里OSS文件
						String sourceObjectName = oldRootPath + pathsDtosList.get(i).getPath();
						String destinationObjectName = newRootPath + pathsDtosList.get(i).getPath();
						ossClient.copyObject(bucketName, sourceObjectName, bucketName, destinationObjectName);
						//删除阿里OSS文件
						ossClient.deleteObject(bucketName, sourceObjectName);
					}
				}
				ossClient.shutdown();
			}
			
			userService.modUserByUserUUID(userName, regSex, Integer.parseInt(regAge), regEmail, userUUID);
			json.put("userName", userName);
		}else {
			if(userUUID.equals(userDto1.getUserUUID())) {
				userService.modUserByUserUUID(userName, regSex, Integer.parseInt(regAge), regEmail, userUUID);
				json.put("userName", userName);
			}else {
				json.put("message", "此用户名已使用！");
			}
		}
		
		this.writeJson(json.toString(), response);
	}
	
	/**
	 * 修改用户密码
	 * @param request
	 * @param response
	 * @param newPassword
	 * @param userUUID
	 */
	@ModelAttribute
	@RequestMapping("/modUserPassword")
	public void modUserPassword(HttpServletRequest request,HttpServletResponse response, String newPassword, String userUUID) {
		JSONObject json = new JSONObject();
		json.put("message", "");
		
		String newMd5Password = this.md5(newPassword);
		userService.modUserPasswordByUserUUID(newMd5Password, userUUID);
		
		this.writeJson(json.toString(), response);
	}
	
	/**
	 * 获取邮箱验证码
	 * @param request
	 * @param response
	 * @param userName
	 * @param email
	 * @throws Exception
	 */
	@ModelAttribute
	@RequestMapping("/getEmailValidCode")
	public void getEmailValidCode(HttpServletRequest request,HttpServletResponse response, String email) throws Exception {
		JSONObject json = new JSONObject();
		json.put("message", "");
		json.put("validCode", "");
		
		if(checkEmailMethod(email)) {
			//判断是否为新邮箱地址
			UserDto userDto = userService.getUserDtosByRegEmail(email);
			if(userDto == null) {
				
				//生成随机验证码
				String chars = "ABCDEFGHJKMNPQRSTWXYZabcdefhijkmnprstwxyz0123456789";
				char[] charArray = chars.toCharArray();
				String validCode = "";
				Random r = new Random();
				for (int i = 0; i < 6; i++) {
					int num = r.nextInt(charArray.length);
					validCode += charArray[num];
				}
				String md5ValidCode = this.md5(validCode);
				json.put("validCode", md5ValidCode);

			    String emailTitle = "验证您的 [袖珍网盘吧] 注册请求";
			    String emailContent = "要在 [袖珍网盘吧] 创建一个新账户，请在注册页面输入验证码：" + validCode;
			    sendEmail(new String[]{email}, emailTitle, emailContent);
				
			}else {
				json.put("message", "此邮箱地址已使用！");
			}
		}else {
			json.put("message", "请输入正确的邮箱地址！");
		}
		
		this.writeJson(json.toString(), response);
	}
	
	
	/**
	 * 获取重置密码的邮箱验证码
	 * @param request
	 * @param response
	 * @param userName
	 * @param email
	 * @throws Exception
	 */
	@ModelAttribute
	@RequestMapping("/getResetPasswordEmailValidCode")
	public void getResetPasswordEmailValidCode(HttpServletRequest request,HttpServletResponse response, String regEmail) throws Exception {
		JSONObject json = new JSONObject();
		json.put("message", "");
		json.put("validCode", "");
		
		UserDto userDto = userService.getUserDtosByRegEmail(regEmail);
		
		if(userDto == null) {
			json.put("message", "请输入正确的邮箱地址！");
		}else {
			String email = userDto.getRegEmail();
			
			//生成随机验证码
			String chars = "ABCDEFGHJKMNPQRSTWXYZabcdefhijkmnprstwxyz0123456789";
			char[] charArray = chars.toCharArray();
			String validCode = "";
			Random r = new Random();
			for (int i = 0; i < 6; i++) {
				int num = r.nextInt(charArray.length);
				validCode += charArray[num];
			}
			String md5ValidCode = this.md5(validCode);
			json.put("validCode", md5ValidCode);
			

		    String emailTitle = "验证您的 [袖珍网盘吧] 重置密码请求";
		    String emailContent = "要在 [袖珍网盘吧] 重置用户密码，请在重置密码页面输入验证码：" + validCode;
			sendEmail(new String[]{email}, emailTitle, emailContent);
		}
		
		this.writeJson(json.toString(), response);
	}
	
	@ModelAttribute
	@RequestMapping("/sendAdminEmail")
	public void sendAdminEmail(HttpServletRequest request,HttpServletResponse response, String emailTitle, String emailContent, String userUUID) throws AddressException, UnsupportedEncodingException, MessagingException {
		JSONObject json = new JSONObject();
		json.put("message", "");
		
		if(userUUID.equals("all")) {
			List<UserDto> userDtosList = userService.getUserDtos();
			String[] email = new String[userDtosList.size() - 1];
			for(int i = 0, j = 0; i < userDtosList.size(); i++) {
				if(!userDtosList.get(i).getUserName().equals("admin")) {
					email[j++] = userDtosList.get(i).getRegEmail();
					
					String emailUUID = this.getUUID();
					String sendUserUUID = "8acca9a4eabe4fc7987abf4666bde368";
					String sendTime = this.getStringDate(new Date());
					EmailDto emailDto = new EmailDto();
					emailDto.setEmailUUID(emailUUID);
					emailDto.setSendUserUUID(sendUserUUID);
					emailDto.setReceiveUserUUID(userDtosList.get(i).getUserUUID());
					emailDto.setEmailTitle(emailTitle);
					emailDto.setEmailContent(emailContent);
					emailDto.setSendTime(sendTime);
					emailService.addEmailDto(emailDto);
				}
			}
			sendEmail(email, emailTitle, emailContent);
		}else {
			UserDto userDto = userService.getUserDtosByUserUUID(userUUID);
			String email = userDto.getRegEmail();

			String emailUUID = this.getUUID();
			String sendUserUUID = "8acca9a4eabe4fc7987abf4666bde368";
			String sendTime = this.getStringDate(new Date());
			EmailDto emailDto = new EmailDto();
			emailDto.setEmailUUID(emailUUID);
			emailDto.setSendUserUUID(sendUserUUID);
			emailDto.setReceiveUserUUID(userUUID);
			emailDto.setEmailTitle(emailTitle);
			emailDto.setEmailContent(emailContent);
			emailDto.setSendTime(sendTime);
			emailService.addEmailDto(emailDto);
			
			sendEmail(new String[]{email}, emailTitle, emailContent);
		}
		
		this.writeJson(json.toString(), response);
	}
	
	@ModelAttribute
	@RequestMapping("/sendUserFeedback")
	public void sendUserFeedback(HttpServletRequest request,HttpServletResponse response, String userUUID, String emailContent) throws AddressException, UnsupportedEncodingException, MessagingException {
		JSONObject json = new JSONObject();
		json.put("message", "");
		
		UserDto userDto = userService.getUserDtosByUserUUID(userUUID);
		String email = "dxj1718874198@gmail.com";
		String emailTitle = "此为 [袖珍网盘吧] \"" + userDto.getUserName() + "\"用户的反馈信息";
		
		String emailUUID = this.getUUID();
		String receiveUserUUID = "8acca9a4eabe4fc7987abf4666bde368";
		String sendTime = this.getStringDate(new Date());
		EmailDto emailDto = new EmailDto();
		emailDto.setEmailUUID(emailUUID);
		emailDto.setSendUserUUID(userDto.getUserUUID());
		emailDto.setReceiveUserUUID(receiveUserUUID);
		emailDto.setEmailTitle(emailTitle);
		emailDto.setEmailContent(emailContent);
		emailDto.setSendTime(sendTime);
		emailService.addEmailDto(emailDto);
		
		sendEmail(new String[]{email}, emailTitle, emailContent);
		
		this.writeJson(json.toString(), response);
	}
	
	@ModelAttribute
	@RequestMapping("/getEmailDtosList")
	public void getEmailDtosList(HttpServletRequest request,HttpServletResponse response) {
		JSONObject json = new JSONObject();
		json.put("message", "");
		
		List<EmailDto> emailDtosList = emailService.getEmailDtos();
		List<UserDto> userDtosList = userService.getUserDtos();
		for(int i = 0; i < emailDtosList.size(); i++) {
			for(int j = 0; j < userDtosList.size(); j++) {
				if(emailDtosList.get(i).getSendUserUUID().equals(userDtosList.get(j).getUserUUID())) {
					emailDtosList.get(i).setSendUserName(userDtosList.get(j).getUserName());
				}
				
				if(emailDtosList.get(i).getReceiveUserUUID().equals(userDtosList.get(j).getUserUUID())) {
					emailDtosList.get(i).setReceiveUserName(userDtosList.get(j).getUserName());
				}
			}
		}
		
		json.put("emailDtosList", emailDtosList);
		
		this.writeJson(json.toString(), response);
	}
	
	@ModelAttribute
	@RequestMapping("/delEmailDto")
	public void delEmailDto(HttpServletRequest request,HttpServletResponse response, String emailUUID) {
		JSONObject json = new JSONObject();
		json.put("message", "");
		
		emailService.delEmailDtoByEmailUUID(emailUUID);
		
		List<EmailDto> emailDtosList = emailService.getEmailDtos();
		List<UserDto> userDtosList = userService.getUserDtos();
		for(int i = 0; i < emailDtosList.size(); i++) {
			for(int j = 0; j < userDtosList.size(); j++) {
				if(emailDtosList.get(i).getSendUserUUID().equals(userDtosList.get(j).getUserUUID())) {
					emailDtosList.get(i).setSendUserName(userDtosList.get(j).getUserName());
				}
				
				if(emailDtosList.get(i).getReceiveUserUUID().equals(userDtosList.get(j).getUserUUID())) {
					emailDtosList.get(i).setReceiveUserName(userDtosList.get(j).getUserName());
				}
			}
		}
		
		json.put("emailDtosList", emailDtosList);
		
		this.writeJson(json.toString(), response);
	}
	
	/**
	 * 发送电子邮件
	 */
	public void sendEmail(String[] email, String emailTitle, String emailContent) throws AddressException, MessagingException, UnsupportedEncodingException {
		//初始化邮箱服务器信息
	    String HOST = "smtp.163.com";
	    final String FROM = "15218480260@163.com";
	    String USER = "15218480260@163.com";
	    final String PWD = "KTIGDMCJLAYKVGCB";
	    String SUBJECT = emailTitle;
	    String context = emailContent;
	    String[] TOS = email;
	    
	    //设置邮箱服务器属性
	    Properties props = new Properties();
        props.put("mail.smtp.host", HOST);
       //SSLSocketFactory类的端口
        props.put("mail.smtp.socketFactory.port", "465");
        //SSLSocketFactory类
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        
        //加载邮箱信息

        Session session = Session.getDefaultInstance(props);
        session.setDebug(true);
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(FROM, "袖珍网盘吧", "UTF-8"));
        InternetAddress[] sendTo = new InternetAddress[TOS.length];
        for (int i = 0; i < TOS.length; i++) { 
        	sendTo[i] = new InternetAddress(TOS[i]); 
        }
        message.addRecipients(Message.RecipientType.TO, sendTo);
        message.addRecipients(MimeMessage.RecipientType.CC, InternetAddress.parse(FROM));
        message.setSubject(SUBJECT);
        
        //加载发送内容
        Multipart multipart = new MimeMultipart();
        BodyPart contentPart = new MimeBodyPart();
        contentPart.setText(context);
        multipart.addBodyPart(contentPart);
        message.setContent(multipart);
        message.saveChanges();

        // 发送邮件
        Transport transport = session.getTransport("smtp");
        transport.connect(HOST, USER, PWD);
        transport.sendMessage(message, message.getAllRecipients());
        transport.close();
	}
	
	/**
	 * 验证邮箱服务器
	 * @param email
	 * @return
	 * @throws Exception
	 */
	public boolean checkEmailMethod(String email) throws Exception {
		
        String hostName = email.split("@")[1];
     // 查找DNS缓存服务器上为MX类型的缓存域名信息
        Lookup lookup = new Lookup(hostName, Type.MX);
        lookup.run();
        if (lookup.getResult() == Lookup.SUCCESSFUL) {//查找失败
//        	System.out.println("true---------------------------");
            return true;
        }
//        System.out.println("false+++++++++++++++++++++++++++++++++");
        return false;
            
	}

}
