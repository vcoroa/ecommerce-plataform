package br.com.vcoroa.ecommerce.platform.application.service;

import br.com.vcoroa.ecommerce.platform.application.dto.request.LoginRequest;
import br.com.vcoroa.ecommerce.platform.application.dto.request.RegisterRequest;
import br.com.vcoroa.ecommerce.platform.application.dto.response.AuthResponse;
import br.com.vcoroa.ecommerce.platform.application.mapper.UserMapper;
import br.com.vcoroa.ecommerce.platform.domain.entity.User;
import br.com.vcoroa.ecommerce.platform.domain.enums.Role;
import br.com.vcoroa.ecommerce.platform.domain.exception.DuplicateResourceException;
import br.com.vcoroa.ecommerce.platform.domain.exception.UnauthorizedException;
import br.com.vcoroa.ecommerce.platform.infrastructure.repository.UserRepository;
import br.com.vcoroa.ecommerce.platform.presentation.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldEncodePasswordAndPersistUser() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("john");
        request.setEmail("john@example.com");
        request.setPassword("secret");
        request.setRole(Role.USER);

        User mappedUser = User.builder()
                .id(1)
                .uuid(UUID.randomUUID())
                .username(request.getUsername())
                .email(request.getEmail())
                .password("encoded")
                .role(Role.USER)
                .build();

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded");
        when(userMapper.toEntity(request, "encoded")).thenReturn(mappedUser);

        authService.register(request);

        verify(passwordEncoder).encode("secret");
        verify(userRepository).save(mappedUser);
    }

    @Test
    void register_shouldThrowWhenUsernameExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("john");
        request.setEmail("john@example.com");

        when(userRepository.existsByUsername("john")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Username already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_shouldThrowWhenEmailExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("john");
        request.setEmail("john@example.com");

        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_shouldAuthenticateAndReturnToken() {
        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("secret");

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("john")
                .password("encoded")
                .roles("USER")
                .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        User persisted = User.builder()
                .uuid(UUID.randomUUID())
                .username("john")
                .role(Role.USER)
                .build();

        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
        when(jwtTokenProvider.generateToken(userDetails)).thenReturn("token");
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(persisted));

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("token");
        assertThat(response.getUsername()).isEqualTo("john");
        assertThat(response.getRole()).isEqualTo(Role.USER);
        verify(jwtTokenProvider).generateToken(userDetails);
    }

    @Test
    void login_shouldThrowUnauthorizedWhenCredentialsInvalid() {
        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("bad");

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid username or password");
    }
}
