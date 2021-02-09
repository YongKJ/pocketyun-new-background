package com.yongkj.pocketyun_new.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yongkj.pocketyun_new.dto.EmailDto;
import com.yongkj.pocketyun_new.mapper.PbEmailMapper;

@Service("emailService")
public class EmailServiceImpl implements EmailService {
	
	@Autowired
	private PbEmailMapper pbEmailMapper;

	public List<EmailDto> getEmailDtos() {
		return pbEmailMapper.getEmailDtos();
	}

	public void addEmailDto(EmailDto emailDto) {
		pbEmailMapper.addEmailDto(emailDto);
	}

	public void delEmailDtoByEmailUUID(String emailUUID) {
		pbEmailMapper.delEmailDtoByEmailUUID(emailUUID);
	}

}
