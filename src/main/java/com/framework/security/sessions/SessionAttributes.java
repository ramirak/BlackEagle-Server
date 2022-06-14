package com.framework.security.sessions;

import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class SessionAttributes {

	public String retrieveAuthenticatedUsername() {
		return SecurityContextHolder.getContext().getAuthentication().getName();
	}

	public Collection<? extends GrantedAuthority> retrieveAuthorities() {
		return SecurityContextHolder.getContext().getAuthentication().getAuthorities();
	}

	public boolean hasRole(String role, Collection<? extends GrantedAuthority> authorities) {
		for (GrantedAuthority authority : authorities) {
			if (authority.getAuthority().contains(role)) {
				return true;
			}
		}
		return false;
	}

	public Authentication getAuthenticationDetails() {
		return SecurityContextHolder.getContext().getAuthentication();
	}

	public String retrieveIpAddress() {
		ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		HttpServletRequest request = attr.getRequest();
		return request.getRemoteAddr();
	}
}
