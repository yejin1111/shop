package com.shop.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.shop.entity.Member;
import com.shop.service.MemberService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MyPageController {

	private final MemberService memberService;

	// GET 요청 시 마이페이지를 보여줍니다. 로그인한 사용자의 정보를 모델에 담아 전달.
	@GetMapping(value = "/mypage")
	public String myPage(Model model, Principal principal) {
		// 로그인한 사용자의 이메일을 가져옵니다.
		String email = principal.getName(); // Principal 객체에서 로그인한 사용자 이름(이메일)을 가져옵니다.

		// MemberService를 통해 이메일로 사용자를 찾습니다.
		Member member = memberService.findByEmail(email);

		// Member 객체를 모델에 추가하여 뷰로 전달
		model.addAttribute("member", member);

		return "/mypage/mypage"; // 마이페이지 뷰를 반환
	}

	// POST 요청 시 사용자 정보를 수정하는 로직을 처리합니다.
	@PostMapping(value = "/mypage")
	public String updateMember(@ModelAttribute("member") Member member) {
		// 사용자 정보를 업데이트하는 서비스 호출
		memberService.update(member);

		return "redirect:/mypage"; // 수정된 정보를 반영하여 다시 마이페이지로 리다이렉트
	}
}
