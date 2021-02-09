package com.yongkj.pocketyun_new.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.yongkj.pocketyun_new.dto.EmailDto;

@Mapper
public interface PbEmailMapper {
	
	@Select("SELECT * FROM py_email")
	List<EmailDto> getEmailDtos();
	
	@Insert("INSERT INTO py_email (emailUUID, sendUserUUID, receiveUserUUID, emailTitle, emailContent, sendTime) VALUES (#{emailUUID}, #{sendUserUUID}, #{receiveUserUUID}, #{emailTitle}, #{emailContent}, #{sendTime})")
	void addEmailDto(EmailDto emailDto);
	
	@Delete("DELETE FROM py_email WHERE emailUUID = #{emailUUID}")
	void delEmailDtoByEmailUUID(@Param("emailUUID") String emailUUID);

}
