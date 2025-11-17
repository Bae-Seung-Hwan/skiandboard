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

  // CustomUserDetailsService가 @Service로 등록되어 있으면
  // Spring Boot가 자동으로 AuthenticationManager에 연결합니다.
  // (따로 AuthenticationManager 빈을 만들 필요 X)

  @Bean
  SecurityFilterChain security(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(auth -> auth
        .requestMatchers("/", "/index", "/error",
            "/css/**", "/js/**", "/img/**", "/webjars/**", "/favicon.ico").permitAll()
        .requestMatchers("/api/resorts/**").permitAll()
        .requestMatchers("/login", "/signup").permitAll()
        // 게시판 쓰기/수정/삭제만 인증
        .requestMatchers("/board/new", "/board/*/edit", "/board/*/delete").authenticated()
        .requestMatchers(HttpMethod.POST,   "/board/**").authenticated()
        .requestMatchers(HttpMethod.PUT,    "/board/**").authenticated()
        .requestMatchers(HttpMethod.PATCH,  "/board/**").authenticated()
        .requestMatchers(HttpMethod.DELETE, "/board/**").authenticated()
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
