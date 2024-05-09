package vsu.cs.is.infsysserver.security.util;

import org.springframework.security.core.userdetails.UserDetails;
import vsu.cs.is.infsysserver.security.entity.UserDetailsImpl;
import vsu.cs.is.infsysserver.security.entity.temp.User;

public final class UserMapper {

    private UserMapper() {}

    public static UserDetails mapUserToUserDetails(User user) {
        return UserDetailsImpl.builder()
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
