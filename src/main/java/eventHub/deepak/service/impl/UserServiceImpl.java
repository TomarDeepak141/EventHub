package eventHub.deepak.service.impl;

import eventHub.deepak.repository.UserRepository;
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
import java.util.Locale;
import java.util.List;

import eventHub.deepak.dto.request.ProfileRequest;
import eventHub.deepak.dto.request.UserAdminRequest;

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
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyExistsException();
        }
        User user = userMapper.toEntity(request);
        user.setEmail(email);

        user.setPassword(
                passwordEncoder
                        .encode(request.getPassword())
        );
        user.setRole(Role.CUSTOMER);

        user.setIsActive(true);

        user.setCreatedAt(LocalDateTime.now());

        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    @Override
    public UserResponse profile(String email) {
        return userRepository.findByEmailIgnoreCase(email).map(userMapper::toResponse).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Override
    public UserResponse updateProfile(String email, ProfileRequest request) {
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setName(request.name().trim());
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public UserResponse updateAccount(Long id, UserAdminRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        user.setRole(request.role());
        user.setIsActive(request.active());
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream().map(userMapper::toResponse).toList();
    }
}

