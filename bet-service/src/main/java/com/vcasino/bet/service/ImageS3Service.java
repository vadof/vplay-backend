package com.vcasino.bet.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.vcasino.bet.config.ApplicationConfig;
import com.vcasino.bet.dto.LoadedImage;
import com.vcasino.bet.exception.AppException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@Profile("prod")
public class ImageS3Service implements ImageStorageService {

    private final AmazonS3 amazonS3;
    private final String bucketName;
    private final Set<String> folders = Set.of("participants", "tournaments");

    public ImageS3Service(AmazonS3 amazonS3, ApplicationConfig applicationConfig) {
        this.amazonS3 = amazonS3;
        this.bucketName = applicationConfig.getS3().getBucket();
    }

    @PostConstruct
    public void init() {
        if (!amazonS3.doesBucketExistV2(bucketName)) {
            amazonS3.createBucket(bucketName);
            log.info("Created S3 bucket: {}", bucketName);
        }
    }

    @Override
    public List<String> upload(String folder, List<MultipartFile> files) {
        if (!folders.contains(folder)) {
            throw new AppException("Invalid folder", HttpStatus.BAD_REQUEST);
        }

        List<String> keys = new ArrayList<>();
        for (MultipartFile file : files) {
            String filename = UUID.randomUUID() + "-" + file.getOriginalFilename();
            String key = folder + "/" + filename;

            try {
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(file.getSize());
                metadata.setContentType(file.getContentType());

                amazonS3.putObject(bucketName, key, file.getInputStream(), metadata);
                log.info("{} uploaded", key);
            } catch (IOException e) {
                log.error("Failed to upload file to S3: {}", key, e);
            }

            keys.add(key);
        }

        return keys;
    }

    @Override
    public LoadedImage load(String folder, String filename) {
        String key = folder + "/" + filename;

        try (S3Object object = amazonS3.getObject(bucketName, key);
             S3ObjectInputStream inputStream = object.getObjectContent()) {

            byte[] data = inputStream.readAllBytes();
            String contentType = object.getObjectMetadata().getContentType();

            return new LoadedImage(data, contentType);
        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() == 404) {
                throw new AppException("File not found", HttpStatus.NOT_FOUND);
            }
            throw new AppException("S3 error", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            throw new AppException("Failed to read file from S3", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public boolean existsByKey(String key) {
        return amazonS3.doesObjectExist(bucketName, key);
    }

    public List<String> getKeysInFolder(String folder) {
        if (!folders.contains(folder)) {
            throw new AppException("Invalid folder", HttpStatus.BAD_REQUEST);
        }

        List<String> keys = new ArrayList<>();

        ListObjectsV2Request listObjects = new ListObjectsV2Request()
                .withBucketName(bucketName)
                .withPrefix(folder + "/")
                .withDelimiter("/");

        ListObjectsV2Result objectListing = amazonS3.listObjectsV2(listObjects);

        for (S3ObjectSummary summary : objectListing.getObjectSummaries()) {
            keys.add(summary.getKey());
        }

        return keys;
    }

}
