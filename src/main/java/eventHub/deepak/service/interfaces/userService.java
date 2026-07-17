package eventHub.deepak.service.interfaces;

import eventHub.deepak.dto.request.userRequest;
import eventHub.deepak.dto.response.userResponse;
import jakarta.validation.Valid;

public interface userService {
    public userResponse register(@Valid userRequest request);
}
