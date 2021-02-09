package com.yongkj.pocketyun_new.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yongkj.pocketyun_new.dto.UserDto;
import com.yongkj.pocketyun_new.mapper.PbUserMapper;


@Service("userService")
public class UserServiceImpl implements UserService {
	
	@Autowired
	private PbUserMapper pbUserMapper;
	
	
	public void addUser(UserDto userDto) {
		pbUserMapper.addUser(userDto);
	}
	
	
	public void delUserByUserUUID(String userUUID) {
		pbUserMapper.delUserByUserUUID(userUUID);
	}
	
	
	public List<UserDto> getUserDtos() {
		return pbUserMapper.getUserDtos();
	}
	
	
	public void modUserLoginTimeByUserUUID(String loginTime, String userUUID) {
		pbUserMapper.modUserLoginTimeByUserUUID(loginTime, userUUID);
	}
	
	
	public void modUserByUserUUID(String userName, String regSex, int regAge, String regEmail, String userUUID) {
		pbUserMapper.modUserByUserUUID(userName, regSex, regAge, regEmail, userUUID);
	}
	
	
	public UserDto getUserByUserNameAndPassword(String userName, String password) {
		return pbUserMapper.getUserByUserNameAndPassword(userName, password);
	}
	
	
	public UserDto getUserByUserName(String userName) {
		return pbUserMapper.getUserByUserName(userName);
	}
	
	
	public String getUserNameByUserUUID(String userUUID) {
		return pbUserMapper.getUserNameByUserUUID(userUUID);
	}
	
	
	public UserDto getUserDtosByUserUUID(String userUUID) {
		return pbUserMapper.getUserDtosByUserUUID(userUUID);
	}
	
	
	public String getUserUUIDByUserName(String userName) {
		return pbUserMapper.getUserUUIDByUserName(userName);
	}

	public UserDto getUserDtosByRegEmail(String regEmail) {
		return pbUserMapper.getUserDtosByRegEmail(regEmail);
	}


	public void modUserPasswordByUserUUID(String password, String userUUID) {
		pbUserMapper.modUserPasswordByUserUUID(password, userUUID);
	}


	public void modUserAndPasswordByUserUUID(String userName, String password, String regSex, int regAge,
			String regEmail, String userUUID) {
		pbUserMapper.modUserAndPasswordByUserUUID(userName, password, regSex, regAge, regEmail, userUUID);
	}


	public UserDto getUserByRegEmailAndPassword(String regEmail, String password) {
		return pbUserMapper.getUserByRegEmailAndPassword(regEmail, password);
	}

}
