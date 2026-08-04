package com.portfolio.project.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	private final GatewayAuthenticationFilter gatewayAuthenticationFilter;

	public SecurityConfig(GatewayAuthenticationFilter gatewayAuthenticationFilter) {
		this.gatewayAuthenticationFilter = gatewayAuthenticationFilter;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.cors(Customizer.withDefaults()).csrf(csrf -> csrf.disable())
				.exceptionHandling(exception -> exception
						.authenticationEntryPoint((request, response, authException) -> response.sendError(401)))
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(HttpMethod.POST, "/api/v1/projects**").permitAll() // for time being
						.requestMatchers(HttpMethod.GET, "/api/v1/projects/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/contact/**").permitAll()
						.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
						
						.anyRequest().authenticated())
				.addFilterBefore(gatewayAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}
