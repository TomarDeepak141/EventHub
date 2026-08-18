package eventHub.deepak.service.interfaces;

import eventHub.deepak.dto.request.LoginRequest;
import eventHub.deepak.dto.response.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
}
