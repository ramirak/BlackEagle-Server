package com.framework.security.sessions;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class SessionAttributes {
	public String retrieveAuthenticatedUsername() {
		return SecurityContextHolder.getContext().getAuthentication().getName();
	}

	public String retrieveIpAddress() {
		ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		HttpServletRequest request = attr.getRequest();
		return request.getRemoteAddr();
	}

}
