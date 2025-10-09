package br.com.vcoroa.ecommerce.platform.presentation.controller;

import br.com.vcoroa.ecommerce.platform.application.dto.request.LoginRequest;
import br.com.vcoroa.ecommerce.platform.application.dto.request.RegisterRequest;
import br.com.vcoroa.ecommerce.platform.application.dto.response.AuthResponse;
import br.com.vcoroa.ecommerce.platform.application.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok("User registered successfully. Please login to get your token.");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
