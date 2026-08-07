package eventHub.deepak.dto.response;

import eventHub.deepak.Repository.UserRepository;
import eventHub.deepak.dto.request.LoginRequest;
import eventHub.deepak.entity.User;

public class LoginResponse {
    private final LoginRequest request;
    private final UserRepository userRepository;

    public LoginResponse(LoginRequest request, UserRepository userRepository) {
        this.request = request;
        this.userRepository = userRepository;
    }
    User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() ->
                    new InvalidCredentialsException("Invalid email or password"));

if (!passwordEncoder.matches(
        request.getPassword(),
        user.getPassword())) {

        throw new InvalidCredentialsException("Invalid email or password");
    }
}
