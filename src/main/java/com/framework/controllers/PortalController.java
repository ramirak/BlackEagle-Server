package com.framework.controllers;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public class PortalController {


	@RequestMapping(path = "/welcome", method = RequestMethod.POST)
	public String welcome() {
		return "/welcome";
	}
	
	@RequestMapping(path = "/login", method = RequestMethod.POST)
	public String login() {
		return "/login";
	}
	
	@RequestMapping(path = "/login2FA", method = RequestMethod.POST)
	public String login2FA() {
		return "/login2FA";
	}
	
	@RequestMapping(path = "/register", method = RequestMethod.POST)
	public String register() {
		return "/register";
	}
	
	@RequestMapping(path = "/ForgotMyPassword", method = RequestMethod.POST)
	public String forgotMyPassword() {
		return "/ForgotMyPassword";
	}
	
	@RequestMapping(path = "/enterOTP", method = RequestMethod.POST)
	public String enterOtp() {
		return "/enterOTP";
	}
	
	
	
}
