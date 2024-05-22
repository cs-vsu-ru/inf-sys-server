package vsu.cs.is.infsysserver.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application")
public record ApplicationProperties(
        String serverUrl,
        String baseFilesFolder
) {
}
