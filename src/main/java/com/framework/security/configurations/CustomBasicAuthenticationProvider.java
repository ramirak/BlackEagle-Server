package com.framework.security.configurations;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.framework.constants.PasswordsDefaults;
import com.framework.data.PasswordEntity;
import com.framework.data.dao.PasswordDao;
import com.framework.exceptions.NotFoundException;
import com.framework.security.services.OTPService;
import com.framework.security.sessions.SessionAttributes;

@Component
public class CustomBasicAuthenticationProvider implements AuthenticationProvider {

	private UserDetailsService userService;
	private PasswordDao passwordDao;
	private PasswordEncoder passwordEncoder;
	private SessionAttributes session;
	private OTPService otp;
	private final String tempToken = PasswordsDefaults.TEMP_TOKEN;

	@Autowired
	public void setUserService(UserDetailsService userService) {
		this.userService = userService;
	}

	@Autowired
	public void setPasswordDao(PasswordDao passwordDao) {
		this.passwordDao = passwordDao;
	}

	@Autowired
	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	@Autowired
	public void setSession(SessionAttributes session) {
		this.session = session;
	}

	@Autowired
	public void setOtp(OTPService otp) {
		this.otp = otp;
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String username = authentication.getName();
		String password = (String) authentication.getCredentials();
		UserDetails user = userService.loadUserByUsername(username);
		boolean multipleAuthentication;
		
		if (session.hasRole("DEVICE", user.getAuthorities())) // Devices should login without 2fa ..
			multipleAuthentication = false;
		else 
			multipleAuthentication = PasswordsDefaults.FORCE_SECOND_AUTHENTICATION;
		
		if (multipleAuthentication) {
			// Already authenticated first stage, now check 2fa..
			if (session.getAuthenticationDetails() != null
					&& session.hasRole(tempToken, session.retrieveAuthorities())) {
				try {
					if (Integer.parseInt(password) == otp.getOTP(username)) {
						// Fully authenticated, set User roles accordingly..
						return new UsernamePasswordAuthenticationToken(username, password, user.getAuthorities());
					} else {
						throw new BadCredentialsException("Incorrect one time key");
					}
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		}

		// First stage of the authentication
		Optional<PasswordEntity> existingPass = passwordDao.findByActiveAndPassOwnerUid(true, authentication.getName());
		if (!existingPass.isPresent())
			throw new NotFoundException("Unable to find current active password");
		PasswordEntity pe = existingPass.get();
		if (passwordEncoder.matches(password, pe.getPassword())) {
			if (multipleAuthentication) {
				try {
					System.out.println("---------------- Current Key : " + otp.getOTP(username) + " ----------------");
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ArrayList<GrantedAuthority> grantedAuthorities = new ArrayList<>();
				// Grant current user access to second factor authentication only..
				grantedAuthorities.add(new SimpleGrantedAuthority(tempToken));
				return new UsernamePasswordAuthenticationToken(username, password, grantedAuthorities);
			} else
				return new UsernamePasswordAuthenticationToken(username, password, user.getAuthorities());
		}
		throw new BadCredentialsException("Incorrect username or password");
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
	}
}
