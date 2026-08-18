package eventHub.deepak.service.impl;

import eventHub.deepak.Repository.UserRepository;
import eventHub.deepak.dto.request.LoginRequest;
import eventHub.deepak.dto.response.LoginResponse;
import eventHub.deepak.entity.User;
import eventHub.deepak.service.interfaces.AuthService;
import eventHub.deepak.service.interfaces.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import eventHub.deepak.globalExceptionHandler.InvalidCredentialsException;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new InvalidCredentialsException(
                                "Invalid email or password"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new InvalidCredentialsException(
                    "Invalid email or password");
        }

        String token = jwtService.generateToken(user);

        return new LoginResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                token,
                LocalDateTime.now()
        );
    }
}
