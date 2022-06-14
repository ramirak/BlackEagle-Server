package com.framework.security.configurations;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.filter.OncePerRequestFilter;
import com.framework.data.UserEntity;
import com.framework.data.dao.UserDao;

public class SessionValidationFilter extends OncePerRequestFilter {

	private UserDao userDao;

	public SessionValidationFilter(UserDao userDao) {
		super();
		this.userDao = userDao;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null) {
			// Find the corresponding user in the database
			Optional<UserEntity> existingEntity = userDao.findById(auth.getName());

			if (existingEntity.isEmpty())
				// Session timeout for removed or non-active accounts
				new SecurityContextLogoutHandler().logout(request, response, auth);
		}
		doFilter(request, response, filterChain);
	}
}
