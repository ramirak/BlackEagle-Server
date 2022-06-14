package com.framework.logic;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.framework.boundaries.PasswordBoundary;
import com.framework.boundaries.UserBoundary;
import com.framework.boundaries.UserLoginDetails;

public interface UserService extends UserDetailsService {

	public UserBoundary register(UserBoundary user);

	public UserBoundary updateUser(UserBoundary update);

	public void sendOtkViaEmail(String userEmail);

	public void resetPassword(PasswordBoundary passDetails);

	public UserBoundary deleteAccount(UserLoginDetails loginDetails);

	public UserBoundary getUserDetails();

	public void sessionCheck();
}
