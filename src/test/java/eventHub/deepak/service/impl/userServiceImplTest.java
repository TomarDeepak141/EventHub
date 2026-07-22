package eventHub.deepak.service.impl;

import eventHub.deepak.dto.request.userRequest;
import eventHub.deepak.dto.response.userResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import eventHub.deepak.mapper.userMapper;
import eventHub.deepak.Repository.userRepository;
import eventHub.deepak.entity.user;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private userRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private userMapper userMapper;

    @InjectMocks
    private userServiceImpl userService;

    private userRequest request;
    private user user;
    private userResponse response;

    @BeforeEach
    void setUp() {
        request = new userRequest();
        request.setName("Deepak");
        request.setEmail("deepak@gmail.com");
        request.setPassword("password123");

        user = new user();
        user.setId(1L);
        user.setName("Deepak");
        user.setEmail("deepak@gmail.com");
        user.setPassword("password123");

        response = new userResponse();
        response.setId(1L);
        response.setName("Deepak");
        response.setEmail("deepak@gmail.com");
    }

    @Test
    void shouldRegisterSuccessfully(){
        when(userRepository.existsByEmail(anyString()))
                .thenReturn(false);
        when(userMapper.toEntity(request))
                .thenReturn(user);
        when(passwordEncoder.encode(anyString()))
                .thenReturn("encodedPassword");
        when(userMapper.toResponse(user))
                .thenReturn(response);
    }
}
