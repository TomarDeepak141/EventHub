package eventHub.deepak.service.interfaces;

import eventHub.deepak.dto.request.UserRequest;
import eventHub.deepak.dto.response.UserResponse;
import jakarta.validation.Valid;
import eventHub.deepak.dto.request.ProfileRequest;
import eventHub.deepak.dto.request.UserAdminRequest;

import java.util.List;

public interface UserService {
    public UserResponse register(@Valid UserRequest request);

    UserResponse profile(String email);

    UserResponse updateProfile(String email, ProfileRequest request);

    UserResponse updateAccount(Long id, UserAdminRequest request, String actorEmail);

    List<UserResponse> listUsers();
}
