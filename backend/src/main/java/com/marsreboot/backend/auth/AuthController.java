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

@RestController
@RequestMapping("/api")
public class AuthController {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository contexts;
    private final CsrfTokenRepository csrfTokens;

    public AuthController(UserRepository users, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
                          SecurityContextRepository contexts, CsrfTokenRepository csrfTokens) {
        this.users = users; this.passwordEncoder = passwordEncoder; this.authenticationManager = authenticationManager;
        this.contexts = contexts; this.csrfTokens = csrfTokens;
    }

    public record Credentials(@NotBlank @Size(max = 80) String username,
                              @NotBlank @Size(max = 72) String password) {}
    public record UserView(Long id, String username) {}
    public record AuthResponse(UserView user) {}
    public record CsrfView(String headerName, String token) {}

    @GetMapping("/csrf")
    public CsrfView csrf(CsrfToken token) { return new CsrfView(token.getHeaderName(), token.getToken()); }

    @PostMapping("/auth/register")
    public UserView register(@Valid @RequestBody Credentials credentials) {
        String username = normalize(credentials.username());
        int passwordBytes = credentials.password().getBytes(StandardCharsets.UTF_8).length;
        if (!username.matches("[a-z0-9_.-]{3,32}") || passwordBytes < 8 || passwordBytes > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid username or password format");
        }
        if (users.existsByUsername(username)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already registered");
        return view(users.save(new UserEntity(username, passwordEncoder.encode(credentials.password()))));
    }

    @PostMapping("/auth/login")
    public AuthResponse login(@Valid @RequestBody Credentials credentials, HttpServletRequest request, HttpServletResponse response) {
        String username = normalize(credentials.username());
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(username, credentials.password()));
        } catch (AuthenticationException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }
        new ChangeSessionIdAuthenticationStrategy().onAuthentication(authentication, request, response);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        contexts.saveContext(context, request, response);
        csrfTokens.saveToken(null, request, response);
        return new AuthResponse(view(users.findByUsername(username).orElseThrow()));
    }

    @GetMapping("/auth/me")
    public AuthResponse me(Authentication authentication) {
        return new AuthResponse(view(users.findByUsername(authentication.getName()).orElseThrow()));
    }

    @PostMapping("/auth/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();
        csrfTokens.saveToken(null, request, response);
    }

    private static String normalize(String username) { return username.trim().toLowerCase(Locale.ROOT); }
    private static UserView view(UserEntity user) { return new UserView(user.getId(), user.getUsername()); }
}
