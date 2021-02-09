package com.yongkj.pocketyun_new.service;

import java.util.List;

import com.yongkj.pocketyun_new.dto.UserDto;

public interface UserService {
	
void addUser(UserDto userDto);
	
	void delUserByUserUUID(String userUUID);
	
	List<UserDto> getUserDtos();
	
	void modUserByUserUUID(String userName, String regSex, int regAge, String regEmail, String userUUID);
	
	void modUserAndPasswordByUserUUID(String userName, String password, String regSex, int regAge, String regEmail, String userUUID);

	UserDto getUserByUserNameAndPassword(String userName, String password);
	
	String getUserNameByUserUUID(String userUUID);
	
	UserDto getUserDtosByUserUUID(String userUUID);
	
	UserDto getUserByUserName(String userName);
	
	String getUserUUIDByUserName(String userName);
	
	void modUserLoginTimeByUserUUID(String loginTime, String userUUID);
	
	UserDto getUserDtosByRegEmail(String regEmail);
	
	void modUserPasswordByUserUUID(String password, String userUUID);
	
	UserDto getUserByRegEmailAndPassword(String regEmail, String password);

}
