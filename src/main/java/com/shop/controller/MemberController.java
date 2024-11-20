package com.shop.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.shop.dto.MemberFormDto;
import com.shop.entity.Member;
import com.shop.service.MemberService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping("/members")
@Controller
@RequiredArgsConstructor
public class MemberController {
	private final MemberService memberService;
	private final PasswordEncoder passwordEncoder;

	@GetMapping(value = "/new")
	public String memberForm(Model model) {
		model.addAttribute("memberFormDto", new MemberFormDto());
		return "member/memberForm";
	}

	@PostMapping(value = "/new")
	public String newMember(@Valid MemberFormDto memberFormDto, BindingResult bindingResult, Model model) {

		if (bindingResult.hasErrors()) {
			return "member/memberForm";
		}

		try {
			Member member = Member.createMember(memberFormDto, passwordEncoder);
			memberService.saveMember(member);
		} catch (IllegalStateException e) {
			model.addAttribute("errorMessage", e.getMessage());
			return "member/memberForm";
		}
		return "redirect:/";

	}

	@GetMapping(value = "/login")
	public String loginMember() {
		return "/member/memberLoginForm";
	}

	@GetMapping(value = "/login/error")
	public String loginError(Model model) {
		model.addAttribute("loginErrorMsg", "아이디 또는 비밀번호를 확인해주세요");
		return "/member/memberLoginForm";
	}

	@GetMapping(value = "/email/error")
	public String emailError(Model model) {
		model.addAttribute("emailErrorMsg", "유효한 이메일을 입력해주세요");
		return "/member/findEmail";
	}

	@GetMapping("/findEmail")
	public String findEmail() {
		return "/member/findEmail"; // 이메일 찾기 폼으로 이동
	}

	@PostMapping("/findEmail")
	public String checkEmail(String email, Model model) {
		// 이메일 유효성 검사
		if (email == null || email.isEmpty()) {
			model.addAttribute("EmailErrorMsg", "이메일을 입력해주세요.");
			return "member/findEmail"; // 다시 이메일 입력 폼으로 이동
		}

		// 이메일 존재 여부 확인
		boolean exists = memberService.existsByEmail(email);
		if (exists) {
			model.addAttribute("EmailErrorMsg", "등록된 이메일입니다.");
		} else {
			model.addAttribute("EmailErrorMsg", "이메일을 다시 확인해주세요.");
		}

		return "member/findEmail"; // 이메일 확인 후 결과 화면으로 이동
	}

	// findPw
	@GetMapping("/findPw")
	public String findPw() {
		return "/member/findPw";
	}

	@PostMapping("/findPw")
	public String resetPassword(@RequestParam("email") String email, Model model) {
	    try {
	        // 이메일로 임시 비밀번호 전송
	        memberService.sendTemporaryPassword(email);
	        model.addAttribute("PwlSuccessMsg", "임시 비밀번호가 이메일로 전송되었습니다.");
	    } catch (IllegalArgumentException e) {
	        model.addAttribute("PwlErrorMsg", "해당 이메일로 가입된 사용자가 없습니다.");
	    }

	    return "member/findPw"; // 비밀번호 찾기 페이지로 다시 돌아가기
	}
}