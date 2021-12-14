package com.framework.security.configurations;

import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.framework.data.PasswordEntity;
import com.framework.data.dao.PasswordDao;
import com.framework.exceptions.NotFoundException;
import com.framework.logic.UserService;
import com.framework.logic.jpa.UserServiceJpa;

@Component
public class CustomBasicAuthenticationProvider implements AuthenticationProvider {

	private UserDetailsService userService;
	private PasswordDao passwordDao;
	private PasswordEncoder passwordEncoder;

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

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String username = authentication.getName();
	    String password = (String) authentication.getCredentials();
	    UserDetails user = userService.loadUserByUsername(username);
	    
	    Optional<PasswordEntity> existingPass = passwordDao.findByActiveAndPassOwnerUid(true, authentication.getName());
		if (!existingPass.isPresent())
			throw new NotFoundException("Unable to find current active password");
		
		PasswordEntity pe = existingPass.get();
	    System.out.println("@@@@@@@@@@@@@@@@@ testing from custom auth provider @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
	    if (passwordEncoder.matches(password, pe.getPassword())) {
	        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
	        return new UsernamePasswordAuthenticationToken(username, password, authorities);
	    }
	    throw new BadCredentialsException("Incorrect username or password");
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
	}
}
