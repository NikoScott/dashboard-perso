package com.crm.freelance.controller;

import com.crm.freelance.dto.LoginRequest;
import com.crm.freelance.dto.LoginResponse;
import com.crm.freelance.dto.RegisterRequest;
import com.crm.freelance.model.AppUser;
import com.crm.freelance.repository.AppUserRepository;
import com.crm.freelance.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    /** true en dev/premier démarrage ; passer à false en prod une fois votre compte créé. */
    @Value("${crm.inscription.ouverte:true}")
    private boolean inscriptionOuverte;

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest req) {
        if (!inscriptionOuverte) {
            throw new IllegalArgumentException("Les inscriptions sont fermées sur cette instance.");
        }
        if (userRepository.findByUsername(req.username()).isPresent()) {
            throw new IllegalArgumentException("Nom d'utilisateur déjà pris");
        }
        AppUser user = AppUser.builder()
                .username(req.username())
                .password(passwordEncoder.encode(req.password()))
                .build();
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new LoginResponse(jwtUtil.generer(user.getUsername()), user.getUsername()));
    }

    /** Permet au frontend de savoir si l'onglet "Inscription" doit être affiché. */
    @GetMapping("/status")
    public java.util.Map<String, Boolean> status() {
        return java.util.Map.of("inscriptionOuverte", inscriptionOuverte);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );
        return new LoginResponse(jwtUtil.generer(req.username()), req.username());
    }
}
