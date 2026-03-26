package vsu.cs.is.infsysserver.security.entity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TwoFactorRequiredResponse {

    private boolean requiresTwoFactor;
    private String email;
}