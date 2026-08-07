package eventHub.deepak.service.impl;

import eventHub.deepak.Repository.UserRepository;
import eventHub.deepak.dto.request.UserRequest;
import eventHub.deepak.dto.response.UserResponse;
import eventHub.deepak.entity.User;
import eventHub.deepak.enums.Role;
import eventHub.deepak.globalExceptionHandler.EmailAlreadyExistsException;
import eventHub.deepak.mapper.UserMapper;
import eventHub.deepak.service.interfaces.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    public UserResponse register(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }
        User user = userMapper.toEntity(request);

        user.setPassword(
                passwordEncoder
                        .encode(request.getPassword())
        );
        user.setRole(Role.USER);

        user.setIsActive(true);

        user.setCreatedAt(LocalDateTime.now());

        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }
}

