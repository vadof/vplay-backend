package com.vcasino.bet.service.image;

import com.vcasino.bet.dto.LoadedImage;
import com.vcasino.bet.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@Profile("dev")
public class LocalImageStorageService implements ImageStorageService {

    private final Path rootLocation = Paths.get("dev/uploads").toAbsolutePath().normalize();
    private final Set<String> folders = Set.of("participants", "tournaments");

    @Override
    public List<String> upload(String folder, List<MultipartFile> files) {
        if (!folders.contains(folder)) {
            throw new AppException("Invalid folder", HttpStatus.BAD_REQUEST);
        }

        List<String> keys = new ArrayList<>();
        for (MultipartFile file : files) {
            String filename = UUID.randomUUID() + "-" + file.getOriginalFilename();

            Path targetDir = rootLocation.resolve(folder).normalize();

            try {
                Files.createDirectories(targetDir);
                Path destination = targetDir.resolve(filename);

                Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
                keys.add(folder + "/" + filename);
            } catch (IOException e) {
                log.error("Error uploading file: {}/{}", folder, file, e);
            }
        }

        return keys;
    }

    @Override
    public LoadedImage load(String folder, String filename) {
        Path targetPath = rootLocation.resolve(folder).resolve(filename).normalize();

        if (!targetPath.startsWith(rootLocation)) {
            throw new AppException(null, HttpStatus.FORBIDDEN);
        }

        if (!Files.exists(targetPath) || !Files.isRegularFile(targetPath)) {
            throw new AppException(null, HttpStatus.NOT_FOUND);
        }

        try {
            byte[] data = Files.readAllBytes(targetPath);
            String contentType = Files.probeContentType(targetPath);

            return new LoadedImage(data, contentType);
        } catch (IOException e) {
            throw new AppException(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @Override
    public boolean existsByKey(String key) {
        Path filePath = rootLocation.resolve(key).normalize();
        return Files.exists(filePath) && Files.isRegularFile(filePath);
    }

    @Override
    public List<String> getKeysInFolder(String folder) {
        Path folderPath = rootLocation.resolve(folder).normalize();

        if (!Files.exists(folderPath) || !Files.isDirectory(folderPath)) {
            return Collections.emptyList();
        }

        try (Stream<Path> paths = Files.list(folderPath)) {
            return paths
                    .filter(Files::isRegularFile)
                    .map(path -> folder + "/" + path.getFileName().toString())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new AppException("Failed to list files", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
