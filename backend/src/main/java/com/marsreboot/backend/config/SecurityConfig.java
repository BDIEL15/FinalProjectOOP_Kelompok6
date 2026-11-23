package com.marsreboot.backend.config;

import com.marsreboot.backend.api.ApiErrorWriter;
import com.marsreboot.backend.user.UserEntity;
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

/** Mengatur kata sandi, sesi login, CSRF, dan akses endpoint. */
@Configuration
public class SecurityConfig {

    /** Mencari akun di database saat pemain mencoba login. */
    @Bean
    UserDetailsService userDetailsService(UserRepository users) {
        return username -> {
            // Login gagal kalau nama pengguna tidak ada.
            UserEntity user = users.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Ubah data akun ke format yang dipakai Spring Security.
            return User.withUsername(user.getUsername())
                    .password(user.getPasswordHash())
                    .roles("PLAYER")
                    .build();
        };
    }

    /** Mengubah kata sandi menjadi hash dan memeriksanya saat login. */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /** Menghubungkan data akun dan pemeriksa kata sandi untuk proses login. */
    @Bean
    AuthenticationProvider authenticationProvider(UserDetailsService users, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(users);
        provider.setPasswordEncoder(encoder);
        return provider;
    }

    /** Menyediakan proses pemeriksaan login untuk AuthController. */
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /** Menyimpan sesi login pemain. */
    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    /** Menyimpan token CSRF untuk melindungi permintaan yang mengubah data. */
    @Bean
    CsrfTokenRepository csrfTokenRepository() {
        return new HttpSessionCsrfTokenRepository();
    }

    /** Membatasi akses endpoint dan memakai login berbasis sesi. */
    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityContextRepository contexts,
            CsrfTokenRepository csrf
    ) throws Exception {
        http
            // Gunakan sesi yang disimpan server.
            .securityContext(c -> c.securityContextRepository(contexts))

            // Minta token CSRF untuk permintaan yang mengubah data.
            .csrf(c -> c.csrfTokenRepository(csrf))

            // Login dan logout API ditangani AuthController.
            .formLogin(c -> c.disable())
            .httpBasic(c -> c.disable())
            .logout(c -> c.disable())
            .requestCache(c -> c.disable())

            // Pendaftaran dan login terbuka; endpoint API lain perlu sesi login.
            .authorizeHttpRequests(a -> a
                .requestMatchers("/api/csrf", "/api/auth/register", "/api/auth/login", "/api/scores/leaderboard").permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().denyAll()
            )

            // Kirim respons JSON untuk permintaan yang tidak punya akses.
            .exceptionHandling(e -> e
                .authenticationEntryPoint((request, response, exception) -> {
                    ApiErrorWriter.write(
                        response, HttpStatus.UNAUTHORIZED,
                        "UNAUTHENTICATED", "Masuk untuk mengakses layanan ini"
                    );
                })
                .accessDeniedHandler((request, response, exception) -> {
                    ApiErrorWriter.write(
                        response, HttpStatus.FORBIDDEN,
                        "FORBIDDEN", "Permintaan tidak diizinkan"
                    );
                })
            );

        return http.build();
    }
}
