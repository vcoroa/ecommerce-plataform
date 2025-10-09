package br.com.vcoroa.ecommerce.platform.application.service;

import br.com.vcoroa.ecommerce.platform.application.dto.request.LoginRequest;
import br.com.vcoroa.ecommerce.platform.application.dto.request.RegisterRequest;
import br.com.vcoroa.ecommerce.platform.application.dto.response.AuthResponse;
import br.com.vcoroa.ecommerce.platform.application.mapper.UserMapper;
import br.com.vcoroa.ecommerce.platform.domain.entity.User;
import br.com.vcoroa.ecommerce.platform.domain.exception.DuplicateResourceException;
import br.com.vcoroa.ecommerce.platform.domain.exception.ResourceNotFoundException;
import br.com.vcoroa.ecommerce.platform.domain.exception.UnauthorizedException;
import br.com.vcoroa.ecommerce.platform.infrastructure.repository.UserRepository;
import br.com.vcoroa.ecommerce.platform.presentation.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;

    @Transactional
    public void register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }

        User user = userMapper.toEntity(request, passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        try {

            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
                )
            );

            String token = jwtTokenProvider.generateToken((UserDetails) authentication.getPrincipal());

            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            return new AuthResponse(token, user.getUsername(), user.getRole());
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid username or password");
        }
    }
}
