package eventHub.deepak.mapper;

import eventHub.deepak.dto.request.userRequest;
import eventHub.deepak.dto.response.userResponse;
import eventHub.deepak.entity.user;
import org.springframework.stereotype.Component;

@Component
public class userMapper {
    public user toEntity(userRequest request){
            user user = new user();

            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setPassword(
                    request.getPassword()
            );
            return user;
        }
    public userResponse toResponse(user user){
        userResponse response = new userResponse();


        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setName(user.getName());
        return response;
    }

}

