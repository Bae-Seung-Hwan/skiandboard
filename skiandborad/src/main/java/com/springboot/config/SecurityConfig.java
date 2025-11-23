package com.springboot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    //http.csrf(csrf -> csrf.disable());

    http.authorizeHttpRequests(auth -> auth
        // 관리자 페이지: ROLE_ADMIN만 접근
        .requestMatchers("/admin/**").hasRole("ADMIN")

        // 나머지는 기존 규칙 유지
        .requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/images/**").permitAll()
        .requestMatchers("/resorts/**").permitAll()
        .requestMatchers("/api/**").permitAll()
        .requestMatchers("/board/**").permitAll()
        .anyRequest().permitAll()
    );

    http.formLogin(login -> login
        .loginPage("/login")
        .loginProcessingUrl("/login")
        .defaultSuccessUrl("/board", true)
        .failureUrl("/login?error")
        .permitAll()
    );

    http.logout(l -> l.logoutUrl("/logout").logoutSuccessUrl("/board"));

    return http.build();
  }
}
