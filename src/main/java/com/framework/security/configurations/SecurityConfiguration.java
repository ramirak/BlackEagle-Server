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

import com.framework.logic.jpa.EventServiceJpa;
import com.framework.security.services.DenialOfServiceProtection;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	private EventServiceJpa eventJpa;
	private DenialOfServiceProtection ddosProtectionService;

	@Autowired
	public void setEventJpa(EventServiceJpa eventJpa) {
		this.eventJpa = eventJpa;
	}

	@Autowired
	public void setDdosProtectionService(DenialOfServiceProtection ddosProtectionService) {
		this.ddosProtectionService = ddosProtectionService;
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.cors().and().csrf().disable().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.ALWAYS).and()
				.addFilter(new CustomUsernamePasswordAuthFilter(authenticationManager(),eventJpa))
				.addFilterAfter(new DenialOfServiceFilter(ddosProtectionService),CustomUsernamePasswordAuthFilter.class)
				.authorizeRequests()
				/**
				 * ------------------------ RULES SUMMARY ------------------------
				 * Users with role [PRE_AUTH] can not access [Admin/User/Device/Data] API
				 * Users with role [Player, Device] can not access [Admin] API
				 * Users with role [Device] can not access [User/Device] API
				 * Users with role [Device] can not alter or remove data 
				 * Users with role [Admin] can not access [Data] API 
				 * ---------------------------------------------------------------
				 */ 
				.antMatchers( 
						"/login", 
						"/users/register"
				)
				.permitAll()
				.antMatchers(
						"/users/**"
				)
				.hasAnyAuthority("PLAYER", "ADMIN")
				.antMatchers(
						"/device/**",
						"/data/update/**",
						"/data/delete/**",
						"/data/deleteAll/**"
				)
				.hasAnyAuthority("PLAYER")
				.antMatchers(
						"/data/add/**",
						"/data/get/**",
						"/data/getAll/**"
				)
				.hasAnyAuthority("PLAYER","DEVICE")
				.antMatchers(
						"/admins/**"			
				)
				.hasAnyAuthority("ADMIN")
				.antMatchers(
						"/**"			
				).denyAll() // Deny all access if not matching one of the above rules..
				.and()
				.formLogin(form -> form.loginPage("/login")
						.failureUrl("/login?error=true")).logout();
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
	public PasswordEncoder passwordEncoder() {
		return new Pbkdf2PasswordEncoder();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(fAuthProvider());
		auth.authenticationProvider(sAuthProvider());
	}
}
