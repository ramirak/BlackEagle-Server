package com.framework.logic;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.framework.boundaries.UserBoundary;

public interface UserService extends UserDetailsService{
	public UserBoundary register(UserBoundary user);

	public UserBoundary updateUser(UserBoundary update);
	
	public UserBoundary resetPassword(String userEmail,String oneTimeKey);
	
	public UserBoundary deleteAccount(String oneTimeKey);
		
}
