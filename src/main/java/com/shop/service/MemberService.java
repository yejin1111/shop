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
	private final EmailService emailService; // 이메일 서비스 주입

	// 회원 저장
	public Member saveMember(Member member) {
		validateDuplicateMember(member);
		return memberRepository.save(member);
	}

	// 중복 회원 검증
	private void validateDuplicateMember(Member member) {
		Member findMember = memberRepository.findByEmail(member.getEmail());
		if (findMember != null) {
			throw new IllegalStateException("이미 가입된 회원입니다."); // 중복된 회원 예외 처리
		}
	}

	// UserDetailsService 구현
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		Member member = memberRepository.findByEmail(email);
		if (member == null) {
			throw new UsernameNotFoundException(email);
		}

		return User.builder().username(member.getEmail()).password(member.getPassword())
				.roles(member.getRole().toString()).build();
	}

	// 이메일 존재 여부 확인
	public boolean existsByEmail(String email) {
		return memberRepository.existsByEmail(email);
	}

	// 임시 비밀번호 발송
	@Transactional
	public void sendTemporaryPassword(String email) {
		// 이메일로 가입된 회원 확인
		Member member = memberRepository.findByEmail(email);
		if (member == null) {
			throw new IllegalArgumentException("해당 이메일로 가입된 사용자가 없습니다.");
		}

		// 임시 비밀번호 생성 및 설정
		String temporaryPassword = generateTemporaryPassword();
		member.setPassword(encodePassword(temporaryPassword));
		memberRepository.save(member);

		// 이메일 발송
		emailService.sendEmail(email, "임시 비밀번호 안내", "임시 비밀번호는 다음과 같습니다: " + temporaryPassword + "\n비밀번호를 변경해주세요.");
	}

	// 임시 비밀번호 생성
	private String generateTemporaryPassword() {
		return UUID.randomUUID().toString().substring(0, 8);
	}

	// 비밀번호 암호화
	private String encodePassword(String rawPassword) {
		return new BCryptPasswordEncoder().encode(rawPassword);
	}

	public Member findByEmail(String email) {
		return memberRepository.findByEmail(email);
	}

	public void update(Member member) {
		memberRepository.save(member); // 사용자 정보 업데이트
	}

}
