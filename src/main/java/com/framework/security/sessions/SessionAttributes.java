package com.framework.security.sessions;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SessionAttributes {
	public String retrieveAuthenticatedUsername() {
		return SecurityContextHolder.getContext().getAuthentication().getName();
	}
}
