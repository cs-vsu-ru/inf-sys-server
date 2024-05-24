package vsu.cs.is.infsysserver.security.util;

import org.springframework.security.core.userdetails.UserDetails;
import vsu.cs.is.infsysserver.employee.adapter.rest.dto.request.EmployeeCreateRequest;
import vsu.cs.is.infsysserver.security.entity.UserDetailsImpl;
import vsu.cs.is.infsysserver.user.adapter.jpa.entity.User;

public final class UserMapper {

    private UserMapper() {}

    public static UserDetails mapUserToUserDetails(User user) {
        return UserDetailsImpl.builder()
                .login(user.getLogin())
                .password(user.getPassword())
                .role(user.getRole())
                .build();
    }

    public static User mapEmployeeCreateRequestToUser(EmployeeCreateRequest request) {
        return User.builder()
                .email(request.email())
                .login(request.login())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .role(request.mainRole())
                .build();
    }
}
