package com.framework.security.configurations;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.framework.constants.PasswordsDefaults;
import com.framework.constants.UserRole;
import com.framework.data.dao.UserDao;
import com.framework.logic.jpa.EventServiceJpa;
import com.framework.security.services.DenialOfServiceProtection;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	private EventServiceJpa eventJpa;
	private UserDao userDao;
	private DenialOfServiceProtection ddosProtectionService;
	private final String 
					PLAYER = UserRole.PLAYER.name(),
					ADMIN = UserRole.ADMIN.name(),
					DEVICE = UserRole.DEVICE.name();
	@Autowired
	public void setEventJpa(EventServiceJpa eventJpa) {
		this.eventJpa = eventJpa;
	}

	@Autowired
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}
	
	@Autowired
	public void setDdosProtectionService(DenialOfServiceProtection ddosProtectionService) {
		this.ddosProtectionService = ddosProtectionService;
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {	
		http.cors().and().csrf().disable()
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.ALWAYS).and()
				.addFilter(new PasswordAuthenticationFilter(authenticationManager(), eventJpa))
				.addFilterAfter(new DenialOfServiceFilter(ddosProtectionService), PasswordAuthenticationFilter.class)
				.addFilterAfter(new SessionValidationFilter(userDao), DenialOfServiceFilter.class)
				.authorizeRequests()
				/**
				 * ------------------------ RULES SUMMARY ------------------------
				 * Users with role [PRE_AUTH] can not access [Admin/User/Device/Data] API
				 * Users with role [RES_AUTH] can change his own password
				 * Users with role [Player, Device] can not access [Admin] API
				 * Users with role [Player, Admin] can not upload any file
				 * Users with role [Device] can not access [User/Device] API
				 * Users with role [Device] can not alter or remove data 
				 * Users with role [Admin] can not access [Data] API 
				 * ---------------------------------------------------------------
				 */
				.antMatchers( 
						"/login", 
						"/users/register",
						"/users/sendOTK/**",
						"/users/sessionCheck"
				)
				.permitAll()
				.antMatchers(
						"/users/resetPassword"
				).hasAnyAuthority(PasswordsDefaults.RESET_PASSWORD_TOKEN)
				.antMatchers(
						"/users/update",
						"/users/reset/**",
						"/users/delete/**",
						"/users/getAccount"
				)
				.hasAnyAuthority(PLAYER, ADMIN)
				.antMatchers(
						"/device/**",
						"/data/update/**",
						"/data/delete/**",
						"/data/deleteAll/**",
						"/events/getAll/**"
				)
				.hasAnyAuthority(PLAYER)
				.antMatchers(
						"/data/add/**",
						"/data/get/**",
						"/data/getAll/**"
				)
				.hasAnyAuthority(PLAYER, DEVICE)
				.antMatchers(
						"/data/upload"
				)
				.hasAnyAuthority(DEVICE)
				.antMatchers(
						"/admins/**"			
				)
				.hasAnyAuthority(ADMIN)
				.antMatchers(
						"/**"			
				).denyAll() // Deny all access if not matching one of the above rules..
				.and()
				.logout()
				.and()
				.headers()
		        .xssProtection()
		        .and()
		        .contentSecurityPolicy("script-src 'self'");;
	}

	@Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedHeaders(Arrays.asList("Content-Type","Accept","credentials"));
        configuration.setAllowedOrigins(Arrays.asList("https://localhost:19006/"));
        configuration.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
	
	@Override
	public void configure(WebSecurity web) {
		web.ignoring().antMatchers("/resources/**", "/static/**");
	}
	
	@Bean
	public FirstAuthenticationProvider fAuthProvider() {
		FirstAuthenticationProvider authProvider = new FirstAuthenticationProvider();
		authProvider.setPasswordEncoder(passwordEncoder());
		return authProvider;
	}
	
	@Bean
	public SecondAuthenticationProvider sAuthProvider() {
		SecondAuthenticationProvider authProvider = new SecondAuthenticationProvider();
		return authProvider;
	}
	
	@Bean
	public ForgotPasswordAuthenticationProvider foAuthProvider() {
		ForgotPasswordAuthenticationProvider authProvider = new ForgotPasswordAuthenticationProvider();
		return authProvider;
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new Pbkdf2PasswordEncoder();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(fAuthProvider());
		auth.authenticationProvider(sAuthProvider());
		auth.authenticationProvider(foAuthProvider());
	}
}
