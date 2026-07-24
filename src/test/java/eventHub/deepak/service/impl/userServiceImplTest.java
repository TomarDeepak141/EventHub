package eventHub.deepak.service.impl;

import eventHub.deepak.dto.request.userRequest;
import eventHub.deepak.dto.response.userResponse;
import eventHub.deepak.globalExceptionHandler.EmailAlreadyExistsException;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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
        userResponse actual = userService.register(request);
        assertEquals(response.getId(), actual.getId());
        assertEquals(response.getName(), actual.getName());
        assertEquals(response.getEmail(), actual.getEmail());
        verify(userRepository).existsByEmail(request.getEmail());

        verify(userMapper).toEntity(request);

        verify(passwordEncoder).encode(request.getPassword());

        verify(userRepository).save(user);

        verify(userMapper).toResponse(user);
    }
    void shouldThrowExceptionWhenEmailAlreadyExists(){
        when(userRepository.existsByEmail(anyString()))
                .thenReturn(true);
        EmailAlreadyExistsException exception =
                assertThrows(
                        EmailAlreadyExistsException.class,
                        () -> userService.register(request)
                );
        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository).existsByEmail(request.getEmail());
        verify(userMapper, never()).toEntity(any(userRequest.class));

        verify(passwordEncoder, never()).encode(anyString());

        verify(userRepository, never()).save(any(user.class));

        verify(userMapper, never()).toResponse(any(user.class));
    }
}
