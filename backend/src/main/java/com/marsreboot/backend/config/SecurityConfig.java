package com.marsreboot.backend.config;

import com.marsreboot.backend.user.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

@Configuration
public class SecurityConfig {
    @Bean
    UserDetailsService userDetailsService(UserRepository users) {
        return username -> users.findByUsername(username)
                .map(user -> User.withUsername(user.getUsername()).password(user.getPasswordHash()).roles("PLAYER").build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }

    @Bean
    AuthenticationProvider authenticationProvider(UserDetailsService users, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(users);
        provider.setPasswordEncoder(encoder);
        return provider;
    }

    @Bean AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean SecurityContextRepository securityContextRepository() { return new HttpSessionSecurityContextRepository(); }
    @Bean CsrfTokenRepository csrfTokenRepository() { return new HttpSessionCsrfTokenRepository(); }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityContextRepository contexts,
                                             CsrfTokenRepository csrf) throws Exception {
        http.securityContext(c -> c.securityContextRepository(contexts))
                .csrf(c -> c.csrfTokenRepository(csrf))
                .formLogin(c -> c.disable())
                .httpBasic(c -> c.disable())
                .logout(c -> c.disable())
                .requestCache(c -> c.disable())
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/api/csrf", "/api/auth/register", "/api/auth/login").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().denyAll())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((request, response, exception) ->
                                com.marsreboot.backend.api.ApiErrorWriter.write(response, HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Masuk untuk mengakses layanan ini"))
                        .accessDeniedHandler((request, response, exception) ->
                                com.marsreboot.backend.api.ApiErrorWriter.write(response, HttpStatus.FORBIDDEN, "FORBIDDEN", "Permintaan tidak diizinkan")));
        return http.build();
    }
}
