package com.devashish.lending.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
            		.requestMatchers("/login.html", "/offline.html", "/css/**", "/js/login.js",
                            "/manifest.webmanifest", "/icons/**", "/sw.js").permitAll()
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login.html")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/index.html", true)
                .failureUrl("/login.html?error")
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login.html"))
            // API calls from the pages get a clean 401, not a redirect to HTML
            .exceptionHandling(ex -> ex.defaultAuthenticationEntryPointFor(
                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                request -> request.getRequestURI().startsWith("/api/")));
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService users(PasswordEncoder encoder, @Value("${app.login.password}") String password) {
        return new InMemoryUserDetailsManager(
            User.withUsername("owner").password(encoder.encode(password)).roles("OWNER").build());
    }
}