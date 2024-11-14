package com.shop.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberFormDto {

	@NotBlank(message = "이름은 필수 입력 값입니다.")
	private String name;

	@NotEmpty(message = "이메일은 필수 입력값입니다.")
	private String email;

	@NotEmpty(message = "비밀번호는 필수 입력 값입니다.")
	@Length(min = 8, max = 16, message = "비밀번호는 8자 이상 16자 이하로 입력합니다.")
	private String password;

	@NotEmpty(message = "주소는 필수 입력 값입니다.")
	private String address;

@NotEmpty (message = "비밀번호를 다시 확인하여주세요.")
private String confirmPassword;
}