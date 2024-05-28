package vsu.cs.is.infsysserver.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "application")
public record ApplicationProperties(
        UploadProperties upload,
        Map<String, ServiceProperties> services
) {
}
