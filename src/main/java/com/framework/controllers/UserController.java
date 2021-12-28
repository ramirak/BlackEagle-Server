package com.framework.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.framework.boundaries.UserBoundary;
import com.framework.boundaries.UserLoginDetails;
import com.framework.logic.UserService;

@RestController
public class UserController {
	private UserService userService;

	@Autowired
	public void setUserService(UserService userService) {
		this.userService = userService;
	}
	
	@RequestMapping(path = "/login", 
			method = RequestMethod.POST, 
			produces = MediaType.APPLICATION_JSON_VALUE, 
			consumes = MediaType.APPLICATION_JSON_VALUE)
	public String login(@RequestBody UserLoginDetails userDetails) {
		return "/login";
	}
	
	
	@RequestMapping(path = "/users/register", 
			method = RequestMethod.POST, 
			produces = MediaType.APPLICATION_JSON_VALUE, 
			consumes = MediaType.APPLICATION_JSON_VALUE)
	public UserBoundary register(@RequestBody UserBoundary newDetails) {
		return userService.register(newDetails);
	}

	@RequestMapping(path = "/users/update", 
			method = RequestMethod.PUT, 
			produces = MediaType.APPLICATION_JSON_VALUE, 
			consumes = MediaType.APPLICATION_JSON_VALUE)
	public UserBoundary update(@RequestBody UserBoundary newDetails) { 		
		return userService.updateUser(newDetails);
	}
	
	

	@RequestMapping(path = "/users/login/2FA", 
			method = RequestMethod.POST, 
			produces = MediaType.APPLICATION_JSON_VALUE, 
			consumes = MediaType.APPLICATION_JSON_VALUE)
	public UserBoundary login2FA(@RequestBody String oneTimeKey) {
		return userService.login2FA(oneTimeKey);
	}
	

	@RequestMapping(path = "/users/reset/{userEmail}/{oneTimeKey}", 
			method = RequestMethod.GET, 
			produces = MediaType.APPLICATION_JSON_VALUE)
	public UserBoundary resetPassword(@PathVariable("userEmail") String email, @PathVariable("oneTimeKey") String oneTimeKey) {
		return userService.resetPassword(email,oneTimeKey);
	}
	
	@RequestMapping(path = "/users/delete", 
			method = RequestMethod.DELETE, 
			produces = MediaType.APPLICATION_JSON_VALUE, 
			consumes = MediaType.APPLICATION_JSON_VALUE)
	public UserBoundary deleteAccount(@RequestBody String oneTimeKey) {
		return userService.deleteAccount(oneTimeKey);
	}
	
	
	
	
	
	
}


