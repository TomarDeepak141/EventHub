package eventHub.deepak.service.interfaces;

import eventHub.deepak.dto.request.UserRequest;
import eventHub.deepak.dto.response.UserResponse;
import jakarta.validation.Valid;

public interface UserService {
    public UserResponse register(@Valid UserRequest request);
}
