package com.framework.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.framework.boundaries.PasswordBoundary;
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

	@RequestMapping(path = "/users/register", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public UserBoundary register(@RequestBody UserBoundary newDetails) {
		return userService.register(newDetails);
	}

	@RequestMapping(path = "/users/update", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public UserBoundary update(@RequestBody UserBoundary newDetails) {
		return userService.updateUser(newDetails);
	}

	@RequestMapping(path = "/users/sendOTK/{userEmail}", method = RequestMethod.GET)
	public void sendOTK(@PathVariable("userEmail") String email) {
		userService.sendOtkViaEmail(email);
	}

	@RequestMapping(path = "/users/resetPassword", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public void resetPassword(@RequestBody PasswordBoundary passDetails) {
		userService.resetPassword(passDetails);
	}

	@RequestMapping(path = "/users/delete", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public UserBoundary deleteAccount(@RequestBody UserLoginDetails loginDetails) {
		return userService.deleteAccount(loginDetails);
	}

	@RequestMapping(path = "/users/getAccount", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public UserBoundary getUserDetails() {
		return userService.getUserDetails();
	}

	@RequestMapping(path = "/users/sessionCheck", method = RequestMethod.GET)
	public void sessionCheck() {
		userService.sessionCheck();
	}
}
