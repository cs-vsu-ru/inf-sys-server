package vsu.cs.is.infsysserver.security.entity.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BindRequiredResponse {
    private boolean bindRequired;
}
