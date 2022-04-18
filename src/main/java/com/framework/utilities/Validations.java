package com.framework.utilities;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.framework.exceptions.BadRequestException;
import com.framework.exceptions.UnauthorizedRequest;

@Service
public class Validations {

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

	public void assertAuthorizedOperation(String userRole) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(userRole)))
			throw new UnauthorizedRequest("User does not own the required privileges");
	}
}