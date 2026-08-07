package eventHub.deepak.mapper;

import eventHub.deepak.dto.request.UserRequest;
import eventHub.deepak.dto.response.UserResponse;
import eventHub.deepak.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public User toEntity(UserRequest request){
            User user = new User();

            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setPassword(
                    request.getPassword()
            );
            return user;
        }
    public UserResponse toResponse(User user){
        UserResponse response = new UserResponse();


        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setName(user.getName());
        return response;
    }

}

