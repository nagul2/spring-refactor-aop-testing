package org.example.expert.domain.auth.service;

import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.common.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;


    @Test
    void 회원가입_성공시_JWT토큰을_생성한다() {
        // given
        SignupRequest request = new SignupRequest("e1@email.com", "qwer1234!@#$", "user");

        given(passwordEncoder.encode(request.getPassword())).willReturn("encodePw");
        given(userRepository.existsByEmail(request.getEmail())).willReturn(false);

        User savedUser = User.builder()
                .id(1L)
                .email(request.getEmail())
                .password("encodePw")
                .userRole(UserRole.USER)
                .build();

        given(userRepository.save(any(User.class))).willReturn(savedUser);
        given(jwtUtil.createToken(savedUser.getId(), savedUser.getEmail(), savedUser.getUserRole()))
                .willReturn("mocked-jwt-token");

        // when
        SignupResponse result = authService.signup(request);


        // then
        assertNotNull(result.getBearerToken());
        assertEquals("mocked-jwt-token", result.getBearerToken());
        verify(userRepository).existsByEmail(request.getEmail());
        verify(passwordEncoder).encode(request.getPassword());
        verify(userRepository).save(any(User.class));
        verify(jwtUtil).createToken(savedUser.getId(), savedUser.getEmail(), savedUser.getUserRole());
    }

    @Test
    void 로그인시_이메일이_없으면_예외가_발생한다() {
        // given
        SigninRequest request = new SigninRequest("e1@email.com", "password");
        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.empty());

        // when & then
        assertThrows(InvalidRequestException.class, () -> authService.signin(request));
        verify(userRepository).findByEmail(request.getEmail());
    }

    @Test
    void 로그인시_비밀번호가_다르면_예외가_발생한다() {
        // given
        SigninRequest request = new SigninRequest("e1@email.com", "password");
        User user = User.builder()
                .id(1L)
                .email(request.getEmail())
                .password("encodedPw")
                .userRole(UserRole.USER)
                .build();

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.getPassword(), user.getPassword())).willReturn(false);

        // when & then
        assertThrows(AuthException.class, () -> authService.signin(request));
        verify(userRepository).findByEmail(request.getEmail());
        verify(passwordEncoder).matches(request.getPassword(), user.getPassword());
    }
}