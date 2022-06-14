package com.framework.security.configurations;

import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.framework.constants.PasswordsDefaults;
import com.framework.exceptions.SessionExpiredException;
import com.framework.security.services.BruteForceProtection;
import com.framework.security.services.SecondFactorCachingService;
import com.framework.security.sessions.SessionAttributes;

@Component
public class SecondAuthenticationProvider implements AuthenticationProvider {

	private UserDetailsService userService;
	private SessionAttributes session;
	private SecondFactorCachingService otp;
	private BruteForceProtection bfp;

	@Autowired
	public void setUserService(UserDetailsService userService) {
		this.userService = userService;
	}

	@Autowired
	public void setSession(SessionAttributes session) {
		this.session = session;
	}

	@Autowired
	public void setOtp(SecondFactorCachingService otp) {
		this.otp = otp;
	}

	@Autowired
	public void setBfp(BruteForceProtection bfp) {
		this.bfp = bfp;
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String username = authentication.getName();
		String password = (String) authentication.getCredentials();
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
				.getRequest();

		UserDetails user;
		try {
			user = userService.loadUserByUsername(username);
		} catch (UsernameNotFoundException e1) {
			// Do not reveal if user was not found, show bad credentials..
			throw new BadCredentialsException("Failed to authenticate");
		}

		if (session.getAuthenticationDetails() != null
				&& session.hasRole(PasswordsDefaults.TEMP_TOKEN, session.retrieveAuthorities())) {
			try {
				if (otp.hasKey(username)) {
					if (otp.getOTP(username).equals(password)) {
						bfp.bfpCheck(request.getRemoteAddr(), username, true);
						// Fully authenticated, set User roles accordingly..
						otp.removeOTP(username);
						return new UsernamePasswordAuthenticationToken(username, password, user.getAuthorities());
					}
				} else {
					// User has PreAuth Token but the OTP has already been expired
					SecurityContextHolder.getContext().setAuthentication(null);
					throw new SessionExpiredException("Otp has expired");
				}
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		throw new BadCredentialsException("Failed to authenticate");
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
	}
}
