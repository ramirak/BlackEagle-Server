package com.framework.logic;

import com.framework.boundaries.UserBoundary;

public interface AdminService {
	public UserBoundary designateUser(UserBoundary user);
	
	public UserBoundary getSpecificUser(String email);

	public UserBoundary resetPassword(String userEmail);

	public UserBoundary deleteAccount(String userEmail);
}
