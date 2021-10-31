package com.framework.logic;

import java.util.List;
import com.framework.boundaries.UserBoundary;

public interface AdminService {

	public UserBoundary getSpecificUser(String email);

	public List<UserBoundary> getAllUsers(int page, int size);

	public UserBoundary resetPassword(String userEmail);

	public UserBoundary deleteAccount(String userEmail);
}
