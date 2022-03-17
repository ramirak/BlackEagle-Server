package com.framework.security.configurations;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.framework.boundaries.UserLoginDetails;
import com.framework.constants.EventType;
import com.framework.logic.jpa.EventServiceJpa;

public class CustomUsernamePasswordAuthFilter extends UsernamePasswordAuthenticationFilter  {
	private AuthenticationManager authManager;
	private EventServiceJpa eventJpa;

	public CustomUsernamePasswordAuthFilter(AuthenticationManager authManager, EventServiceJpa eventJpa) {
		super();
		this.authManager = authManager;
		this.eventJpa = eventJpa;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		try {
			// Get username & password from request (JSON) any way you like
			UserLoginDetails authRequest = new ObjectMapper().readValue(request.getInputStream(),
					UserLoginDetails.class);
			// TODO :test for null values
			Authentication auth = new UsernamePasswordAuthenticationToken(authRequest.getUid(),
					authRequest.getPassword());
			return authManager.authenticate(auth);
		} catch (Exception exp) {
			throw new RuntimeException(exp);
		}
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {
		SecurityContext securityContext = SecurityContextHolder.getContext();
		securityContext.setAuthentication(authResult);
		if (authResult.getAuthorities().contains(new SimpleGrantedAuthority("PLAYER"))) // Log only events for basic users
			eventJpa.createEvent(authResult.getName(), EventType.NEW_LOGIN);
		/*
		 * // Create a new session and add the security context. HttpSession session =
		 * request.getSession(true); session.setAttribute("SPRING_SECURITY_CONTEXT",
		 * securityContext);
		 */
	}
	
}