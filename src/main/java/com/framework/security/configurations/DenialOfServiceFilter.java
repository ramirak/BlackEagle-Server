package com.framework.security.configurations;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

import com.framework.security.services.DenialOfServiceProtection;

public class DenialOfServiceFilter extends OncePerRequestFilter {

	private DenialOfServiceProtection ddosProtectionService;

	public DenialOfServiceFilter(DenialOfServiceProtection ddosProtectionService) {
		this.ddosProtectionService = ddosProtectionService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		ddosProtectionService.consumeBucket();
		filterChain.doFilter(request, response);
	}
}
