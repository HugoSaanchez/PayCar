
package com.example.demo.security;

import java.io.IOException;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.example.demo.apisecurity.JWTAuthorizationFilter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Configuration
@EnableWebSecurity
public class SecurityConfig {
	 @Order(1)
	 @Bean
	    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
	        http
	            .csrf().disable()
	            .addFilterBefore(new JWTAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class)
	            .authorizeHttpRequests()
	            .requestMatchers(new AntPathRequestMatcher("/api/login"), new AntPathRequestMatcher("/api/register")).permitAll()
	            .requestMatchers("/api/**").authenticated()
	            .and()
	            .formLogin().disable();
	        return http.build();
	    }
	
	@Order(2)
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests((requests) -> requests
				.requestMatchers("/", "/imgs/**", "/auth/**", "/webjars/**", "/css/**", "/files/**", "/error/**",
						"/logout/**", "/index/")
				.permitAll().requestMatchers("/admin/**").hasAuthority("ROL_ADMIN"))
				.formLogin((form) -> form
						//
						.loginPage("/auth/login").successHandler(new AuthenticationSuccessHandler() {
							@Override
							public void onAuthenticationSuccess(HttpServletRequest request,
									HttpServletResponse response, Authentication authentication)
									throws IOException, ServletException {
								Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
								if (roles.contains("ROL_ADMIN")) {
									response.sendRedirect("/admin/usuario");
									System.out.println("Roles del alumnosdas: " + roles);
								} 
							}
						}).permitAll())
				.logout((logout) -> logout.permitAll().logoutUrl("/logout").logoutSuccessUrl("/index/"));
		
		
		return http.build();
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
			throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}
	


}
