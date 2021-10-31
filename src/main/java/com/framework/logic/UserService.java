package com.framework.logic;

import com.framework.boundaries.UserBoundary;

public interface UserService {
	public UserBoundary register(UserBoundary user);

	public UserBoundary login(String userEmail, String password);

	public UserBoundary login2FA(String oneTimeKey);

	public UserBoundary updateUser(UserBoundary update);
	
	public UserBoundary resetPassword(String userEmail,String oneTimeKey);
	
	public UserBoundary deleteAccount(String oneTimeKey);
}
