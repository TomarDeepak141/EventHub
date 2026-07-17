package eventHub.deepak.service.impl;

import eventHub.deepak.dto.request.userRequest;
import eventHub.deepak.dto.response.userResponse;
import eventHub.deepak.entity.user;
import eventHub.deepak.enums.role;
import eventHub.deepak.globalExceptionHandler.EmailAlreadyExistsException;
import eventHub.deepak.service.interfaces.userService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class userServiceImpl implements userService {
    private final eventHub.deepak.Repository.userRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final eventHub.deepak.mapper.userMapper userMapper;

    public userServiceImpl(eventHub.deepak.Repository.userRepository userRepository, PasswordEncoder passwordEncoder, eventHub.deepak.mapper.userMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    public userResponse register(userRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }
        user user = userMapper.toEntity(request);

        user.setPassword(
                passwordEncoder
                        .encode(request.getPassword())
        );
        user.setRole(role.USER);

        user.setIsActive(true);

        user.setCreatedAt(LocalDateTime.now());

        user.setUpdatedAt(LocalDateTime.now());

        eventHub.deepak.entity.user savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }
}

