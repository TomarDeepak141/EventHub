package eventHub.deepak.controller;

import eventHub.deepak.dto.request.UserRequest;
import eventHub.deepak.dto.response.UserResponse;
import eventHub.deepak.service.interfaces.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.core.Authentication;
import eventHub.deepak.dto.request.ProfileRequest;
import eventHub.deepak.dto.request.UserAdminRequest;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody UserRequest request
    ) {

        UserResponse response = userService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/me")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public UserResponse me(Authentication authentication) {
        return userService.profile(authentication.getName());
    }

    @PutMapping("/me")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public UserResponse updateMe(Authentication authentication, @Valid @RequestBody ProfileRequest request) {
        return userService.updateProfile(authentication.getName(), request);
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public List<UserResponse> users() {
        return userService.listUsers();
    }

    @PutMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public UserResponse updateAccount(@PathVariable Long id, @Valid @RequestBody UserAdminRequest request) {
        return userService.updateAccount(id, request);
    }

}
