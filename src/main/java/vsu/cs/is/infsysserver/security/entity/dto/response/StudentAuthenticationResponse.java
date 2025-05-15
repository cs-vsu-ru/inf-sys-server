package vsu.cs.is.infsysserver.security.entity.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vsu.cs.is.infsysserver.student.adapter.rest.response.StudentResponse;
import vsu.cs.is.infsysserver.user.adapter.jpa.entity.User;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentAuthenticationResponse {
    @JsonProperty("jwtToken")
    private String accessToken;
    private StudentResponse user;
}
