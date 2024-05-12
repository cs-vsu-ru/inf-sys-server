package vsu.cs.is.infsysserver.security.entity.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {
    @JsonProperty("jwtToken")
    private String accessToken;
//    @JsonProperty("refresh_token")
//    private String refreshToken;
    private String mainRole;
}
