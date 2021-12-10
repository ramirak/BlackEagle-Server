package com.framework.security.configurations;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.framework.boundaries.UserBoundary;

public class CustomUsernamePasswordAuthFilter extends UsernamePasswordAuthenticationFilter {
	private AuthenticationManager authManager;

	public CustomUsernamePasswordAuthFilter(AuthenticationManager authManager) {
		super();
		this.authManager = authManager;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		try {
			// Get username & password from request (JSON) any way you like
			UserBoundary authRequest = new ObjectMapper().readValue(request.getInputStream(), UserBoundary.class);
			// TODO :test for null values
			
			
			Authentication auth = new UsernamePasswordAuthenticationToken(authRequest.getUserId().getUID(),
					authRequest.getUserId().getPasswordBoundary().getPassword());
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
		  // Create a new session and add the security context.
	    HttpSession session = request.getSession(true);
	    session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);
		
	}
}