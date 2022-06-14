package com.framework.security.configurations;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.framework.constants.PasswordsDefaults;
import com.framework.constants.UserRole;
import com.framework.security.services.BruteForceProtection;
import com.framework.security.services.ResetPasswordCachingService;
import com.framework.security.sessions.SessionAttributes;

@Component
public class ForgotPasswordAuthenticationProvider implements AuthenticationProvider {

	private UserDetailsService userService;
	private SessionAttributes session;
	private ResetPasswordCachingService rpCachingService;
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
	public void setRpCachingService(ResetPasswordCachingService rpCachingService) {
		this.rpCachingService = rpCachingService;
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
			// Count failed attempts even if no user was found in the DB
			bfp.bfpCheck(request.getRemoteAddr(), username, false);
			return null;
		}

		// Only users of type PLAYER are allowed to reset their own passwords..
		if (!session.hasRole(UserRole.PLAYER.name(), user.getAuthorities()))
			return null;

		try {
			if (rpCachingService.hasKey(username) && rpCachingService.getOTK(username).equals(password)) {
				bfp.bfpCheck(request.getRemoteAddr(), username, true);
				// User authenticated with one time key via email and allowed to reset his own
				// password
				ArrayList<GrantedAuthority> grantedAuthorities = new ArrayList<>();
				// Grant current user access to reset password page only..
				grantedAuthorities.add(new SimpleGrantedAuthority(PasswordsDefaults.RESET_PASSWORD_TOKEN));
				return new UsernamePasswordAuthenticationToken(username, password, grantedAuthorities);
			}
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		// If we arrived here it means all providers failed authentication, count it as
		// failed login attempt in the BFP service..
		bfp.bfpCheck(request.getRemoteAddr(), username, false);
		throw new BadCredentialsException("Failed to authenticate");
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
	}
}
