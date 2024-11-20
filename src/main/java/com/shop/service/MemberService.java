package com.shop.service;

import java.util.UUID;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shop.entity.Member;
import com.shop.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {
	private final MemberRepository memberRepository;
	private final EmailService emailService;

	public Member saveMember(Member member) {
		validateDuplicateMember(member);
		return memberRepository.save(member);
	}

	private void validateDuplicateMember(Member member) {
		Member findMember = memberRepository.findByEmail(member.getEmail());
		if (findMember != null) {
			throw new IllegalStateException("이미 가입된 회원입니다."); // 이미 가입된 회원의 경우 예외를 발생시킨다.
		}
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		Member member = memberRepository.findByEmail(email);
		if (member == null) {
			throw new UsernameNotFoundException(email);
		}
		return User.builder().username(member.getEmail()).password(member.getPassword())
				.roles(member.getRole().toString()).build();
	}

	public boolean existsByEmail(String email) {
		return memberRepository.existsByEmail(email);
	}

	
	
	//findpw
	@Transactional
	public void sendTemporaryPassword(String email) {
		// 이메일로 가입된 회원을 찾기
		Member member = memberRepository.findByEmail(email);
		if (member == null) {
			throw new IllegalArgumentException("해당 이메일로 가입된 사용자가 없습니다.");
		}

		// 임시 비밀번호 생성
		String temporaryPassword = generateTemporaryPassword();

		// 비밀번호를 임시 비밀번호로 변경하고 암호화
		member.setPassword(encodePassword(temporaryPassword));
		memberRepository.save(member);

		// 이메일로 임시 비밀번호 발송
		emailService.sendEmail(email, "임시 비밀번호 안내", "임시 비밀번호는 다음과 같습니다: " + temporaryPassword + "\n비밀번호를 변경해주세요.");
	}

	private String generateTemporaryPassword() {
		// UUID를 사용하여 8자리 임시 비밀번호 생성
		return UUID.randomUUID().toString().substring(0, 8);
	}

	private String encodePassword(String rawPassword) {
		// BCrypt로 비밀번호 암호화
		return new BCryptPasswordEncoder().encode(rawPassword);
	}
}
