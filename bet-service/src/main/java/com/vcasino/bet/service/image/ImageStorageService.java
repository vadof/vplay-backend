package com.vcasino.bet.service.image;

import com.vcasino.bet.dto.LoadedImage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageStorageService {
    List<String> upload(String folder, List<MultipartFile> files);
    LoadedImage load(String folder, String filename);
    boolean existsByKey(String key);
    List<String> getKeysInFolder(String folder);
}
