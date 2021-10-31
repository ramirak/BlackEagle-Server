package com.framework.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import com.framework.boundaries.UserBoundary;
import com.framework.logic.AdminService;

public class AdminController {
	private AdminService adminService;

	@Autowired
	public void setAdminService(AdminService adminService) {
		this.adminService = adminService;
	}

	@RequestMapping(path = "/admins/get/{userEmail}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public UserBoundary getSpecificUser(@PathVariable("userEmail") String email) {
		return adminService.getSpecificUser(email);
	}

	@RequestMapping(path = "/admins/getAll", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public UserBoundary[] getAllUsers(
			@RequestParam(name = "page", required = false, defaultValue = "0") int page,
			@RequestParam(name = "size", required = false, defaultValue = "5") int size) {
		return adminService.getAllUsers(page, size).toArray(new UserBoundary[0]);
	}

	@RequestMapping(path = "/admins/reset/{userEmail}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public UserBoundary resetPassword(@PathVariable("userEmail") String email) {
		return adminService.resetPassword(email);
	}

	@RequestMapping(path = "/admins/delete", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public UserBoundary deleteAccount(@RequestBody String email) {
		return adminService.deleteAccount(email);
	}
}
