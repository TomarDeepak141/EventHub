package eventHub.deepak.dto.response;


import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@Getter
@Setter
public class UserResponse {
    private Long id;
    private String name;
    private String email;
}
