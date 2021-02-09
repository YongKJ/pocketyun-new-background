package com.yongkj.pocketyun_new.service;

import java.util.List;

import com.yongkj.pocketyun_new.dto.EmailDto;

public interface EmailService {
	
	List<EmailDto> getEmailDtos();
	
	void addEmailDto(EmailDto emailDto);
	
	void delEmailDtoByEmailUUID(String emailUUID);

}
