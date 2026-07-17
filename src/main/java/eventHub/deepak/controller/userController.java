package eventHub.deepak.controller;

import eventHub.deepak.dto.request.userRequest;
import eventHub.deepak.dto.response.userResponse;
import eventHub.deepak.service.interfaces.userService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class userController {
    private final userService userService;

    public userController(userService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<userResponse> register(
            @Valid @RequestBody userRequest request
    ) {

        userResponse response = userService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

}
