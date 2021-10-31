package com.framework.utilities;

import org.springframework.stereotype.Service;

import com.framework.datatypes.UserRole;
import com.framework.exceptions.BadRequestException;
import com.framework.exceptions.UnauthorizedRequest;

@Service
public class Utils {
	
	public void assertValidRole(String role) {
		for (UserRole ur : UserRole.values()) {
			if (ur.name().equals(role))
				return;
		}
		throw new BadRequestException("Invalid role");
	}

	public void assertNull(Object obj) {
		if (obj == null)
			throw new BadRequestException("Null object");
	}

	public void assertEmptyString(String str) {
		if (str.equals(""))
			throw new BadRequestException("Empty string");
	}
	
	public void assertOwnership(String currentlyLoggedIn, String parentId) {
		if (!parentId.equals(currentlyLoggedIn))
			throw new UnauthorizedRequest("User does not own the requested data");	
	}

}