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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.framework.logic.jpa.EventServiceJpa;


@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	private EventServiceJpa eventJpa;
	
	@Autowired
	public void setEventJpa(EventServiceJpa eventJpa) {
		this.eventJpa = eventJpa;
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.cors().and().csrf().disable()
				.addFilter(new CustomUsernamePasswordAuthFilter(authenticationManager(),eventJpa))
				.authorizeRequests()
				.antMatchers( 
						"/login", 
						"/users/register"
				)
				.permitAll()
				.antMatchers(
						"/users/update", 
						"/device/add" , 
						"/device/delete", 
						"/data/add",
						"/data/update"
				)
				.hasAnyAuthority("PLAYER", "ADMIN")
				.antMatchers(
						"/data/add",
						"/data/getAll"
				)
				.hasAnyAuthority("DEVICE")
				.antMatchers(
						"/admins/designate",
						"/admins/get",
						"/admins/getAll",
						"/admins/reset",
						"/admins/delete"				
				)
				.hasAnyAuthority("ADMIN")
				.antMatchers(
						"/encrypted"
				)
				.denyAll()
				.and()
				.formLogin(form -> form.loginPage("/login")
						.defaultSuccessUrl("/home")
						.failureUrl("/login?error=true")).logout();
	}

	@Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:19006/"));
        configuration.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
	
	@Override
	public void configure(WebSecurity web) {
		web.ignoring().antMatchers("/resources/**", "/static/**");
	}

	@Bean
	public CustomBasicAuthenticationProvider authProvider() {
		CustomBasicAuthenticationProvider authProvider = new CustomBasicAuthenticationProvider();
		authProvider.setPasswordEncoder(passwordEncoder());
		return authProvider;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new Pbkdf2PasswordEncoder();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(authProvider());
	}
}
