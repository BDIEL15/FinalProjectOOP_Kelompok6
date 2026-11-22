package com.marsreboot.backend.auth;

import com.marsreboot.backend.user.UserEntity;
import com.marsreboot.backend.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

/** Menangani pendaftaran, login, dan logout pemain. */
@RestController
@RequestMapping("/api")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final CsrfTokenRepository csrfTokenRepository;

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository,
            CsrfTokenRepository csrfTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.csrfTokenRepository = csrfTokenRepository;
    }

    public static class Credentials {
        @NotBlank
        @Size(max = 80)
        private String username;

        @NotBlank
        @Size(max = 72)
        private String password;

        public Credentials() {
            // Constructor kosong dipakai Jackson saat membaca JSON.
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class UserView {
        private final Long id;
        private final String username;

        public UserView(Long id, String username) {
            this.id = id;
            this.username = username;
        }

        public Long getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }
    }

    public static class AuthResponse {
        private final UserView user;

        public AuthResponse(UserView user) {
            this.user = user;
        }

        public UserView getUser() {
            return user;
        }
    }

    public static class CsrfView {
        private final String headerName;
        private final String token;

        public CsrfView(String headerName, String token) {
            this.headerName = headerName;
            this.token = token;
        }

        public String getHeaderName() {
            return headerName;
        }

        public String getToken() {
            return token;
        }
    }

    /** Memberikan token untuk permintaan yang mengubah data. */
    @GetMapping("/csrf")
    public CsrfView csrf(CsrfToken token) {
        return new CsrfView(token.getHeaderName(), token.getToken());
    }

    /** Membuat akun baru setelah memeriksa data pendaftaran. */
    @PostMapping("/auth/register")
    public UserView register(@Valid @RequestBody Credentials credentials) {
        String username = normalize(credentials.getUsername());
        // BCrypt membatasi kata sandi sampai 72 byte.
        int passwordBytes = credentials.getPassword().getBytes(StandardCharsets.UTF_8).length;

        if (!username.matches("[a-z0-9_.-]{3,32}") || passwordBytes < 8 || passwordBytes > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Format nama pengguna atau kata sandi tidak sesuai");
        }
        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nama pengguna sudah terdaftar");
        }

        // Kata sandi disimpan sebagai hash, bukan teks asli.
        String passwordHash = passwordEncoder.encode(credentials.getPassword());
        UserEntity newUser = new UserEntity(username, passwordHash);
        UserEntity savedUser = userRepository.save(newUser);
        return toView(savedUser);
    }

    /** Memeriksa login dan membuat sesi jika datanya benar. */
    @PostMapping("/auth/login")
    public AuthResponse login(@Valid @RequestBody Credentials credentials,
                              HttpServletRequest request, HttpServletResponse response) {
        String username = normalize(credentials.getUsername());
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(username, credentials.getPassword()));
        } catch (AuthenticationException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nama pengguna atau kata sandi salah");
        }

        // Perbarui ID sesi setelah login.
        new ChangeSessionIdAuthenticationStrategy().onAuthentication(authentication, request, response);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
        // Token CSRF lama dihapus setelah login.
        csrfTokenRepository.saveToken(null, request, response);

        return new AuthResponse(toView(userRepository.findByUsername(username).orElseThrow()));
    }

    /** Mengembalikan akun yang sedang login. */
    @GetMapping("/auth/me")
    public AuthResponse me(Authentication authentication) {
        return new AuthResponse(toView(userRepository.findByUsername(authentication.getName()).orElseThrow()));
    }

    /** Mengakhiri sesi login pemain. */
    @PostMapping("/auth/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        csrfTokenRepository.saveToken(null, request, response);
    }

    // Samakan format nama pengguna sebelum disimpan atau dicari.
    private static String normalize(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }

    private static UserView toView(UserEntity user) {
        return new UserView(user.getId(), user.getUsername());
    }
}
