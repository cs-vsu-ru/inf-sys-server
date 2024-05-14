package vsu.cs.is.infsysserver.security.util;

import org.springframework.security.core.userdetails.UserDetails;
import vsu.cs.is.infsysserver.security.entity.UserDetailsImpl;
import vsu.cs.is.infsysserver.user.adapter.jpa.entity.User;

public final class UserMapper {

    private UserMapper() {}

    public static UserDetails mapUserToUserDetails(User user) {
        return UserDetailsImpl.builder()
                .email(user.getEmail())
                .password(user.getPassword())
                .role(user.getRole())
                .build();
    }
}
