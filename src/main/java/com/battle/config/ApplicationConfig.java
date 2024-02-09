package com.battle.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.battle.repositories.UserRepository;
import com.battle.serviceImpl.UserDetailsServiceImpl;

@Configuration
public class ApplicationConfig {

//	@Autowired
//	  UserDetailsServiceImpl userDetailsService;
	@Autowired
	private UserRepository userRepository;
	 @Bean
	    public UserDetailsService userDetailsServices() {
	        return new UserDetailsServiceImpl();
	    }
//	@Bean
//	public UserDetailsService userDetailsService() {
//		 return username -> userRepository.findByEmail(username)
//				 .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//				 }
//	
	
	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(userDetailsServices());
		authProvider.setPasswordEncoder(PasswordEncoder());
		
		return authProvider;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{
		return config.getAuthenticationManager();
	}

	@Bean
	public PasswordEncoder PasswordEncoder() {

		return new BCryptPasswordEncoder();
	}
}