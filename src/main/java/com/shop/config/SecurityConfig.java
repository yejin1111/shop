package com.shop.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.shop.service.MemberService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    MemberService memberService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
        .csrf().disable()  // 개발 중에만 CSRF 보호를 비활성화 (후에 재활성화 필요)
        .formLogin(formLogin -> formLogin
            .loginPage("/members/login")            // 로그인 페이지 url을 설정
            .defaultSuccessUrl("/")                 // 로그인 성공 시 이동할 url
            .usernameParameter("email")             // 로그인 시 사용할 파라미터 이름으로 email을 지정
            .failureUrl("/members/login/error"))    // 로그인 실패 시 이동할 url을 설정

        .logout(logout -> logout
            .logoutRequestMatcher(new AntPathRequestMatcher("/members/logout"))    // 로그아웃 url을 설정
            .logoutSuccessUrl("/"))                            // 로그아웃 성공 시 이동할 url을 설정

        .authorizeHttpRequests(request -> request
            .requestMatchers("/", "/members/**", "/item/**", "/images/**").permitAll()   // 이메일 찾기 경로를 인증 없이 허용
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated())

        .exceptionHandling(handling -> handling    // 인증되지 않은 사용자가 리소스에 접근할 때 수행되는 핸들러 등록
            .authenticationEntryPoint(new CustomAuthenticationEntryPoint()));
        
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(memberService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/css/**", "/js/**", "/img/**");
    }
}
