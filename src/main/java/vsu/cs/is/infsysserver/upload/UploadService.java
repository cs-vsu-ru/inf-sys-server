package vsu.cs.is.infsysserver.upload;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vsu.cs.is.infsysserver.configuration.properties.ApplicationProperties;
import vsu.cs.is.infsysserver.exception.GeneralException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.logging.Logger;

@RequiredArgsConstructor
@Service
public class UploadService {

    private static final Logger LOGGER = Logger.getLogger(UploadService.class.getName());

    private final ApplicationProperties applicationProperties;

    public String uploadFile(MultipartFile file) {
        UUID uuid = UUID.randomUUID();
        String filePath = applicationProperties.upload().baseFilesFolder()
                + uuid
                + file.getOriginalFilename();

        Path path = Path.of(filePath);
        try {
            byte[] bytes = file.getBytes();
            Files.write(path, bytes);
            return applicationProperties.upload().serverUrl() + path;
        } catch (IOException ex) {
            LOGGER.severe(ex.getLocalizedMessage());

            throw new GeneralException("Не удалось загрузить файл");
        }
    }
}
