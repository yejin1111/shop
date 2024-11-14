package com.shop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/thymeleaf")
public class ThymeleafExController {

	@GetMapping(value = "/ex05")
	public String thymeleafExample05(){
	return "thymeleafEx/thymeleafEx05";
	}
	
	@GetMapping(value = "/ex06")
	public String thymeleafExample06(String param1, String param2, Model model){ model.addAttribute("param1", param1);
	model.addAttribute("param2", param2); return "thymeleafEx/thymeleafEx06";
	}
	
	
	@GetMapping(value = "/ex07")
	public String thymeleafExample07(){ return "thymeleafEx/thymeleafEx07";
	}
}