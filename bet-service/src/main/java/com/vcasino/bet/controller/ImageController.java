package com.vcasino.bet.controller;

import com.vcasino.bet.dto.LoadedImage;
import com.vcasino.bet.service.ImageStorageService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bet/images")
@AllArgsConstructor
@Slf4j
public class ImageController {

    private final ImageStorageService storageService;

    @GetMapping("/{folder}/{filename}")
    public ResponseEntity<byte[]> load(@PathVariable(name = "folder") String folder,
                                       @PathVariable(name = "filename") String filename) {
        LoadedImage image = storageService.load(folder, filename);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.getContentType()))
                .body(image.getData());
    }
}
