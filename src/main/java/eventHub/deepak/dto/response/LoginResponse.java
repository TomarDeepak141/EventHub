package eventHub.deepak.dto.response;

import eventHub.deepak.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LoginResponse {
    private long userId;
    private String email;
    private Role role;
    private String jwt;
    private LocalDateTime date;
}
