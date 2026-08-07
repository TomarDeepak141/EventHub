package eventHub.deepak.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;


@NoArgsConstructor
@Getter
@Setter
@Data
public class UserRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank
    @Size(min=8,message = "Password must be at least 8 characters")
    private String password;

}
