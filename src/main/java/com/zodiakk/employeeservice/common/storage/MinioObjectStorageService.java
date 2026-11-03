package com.zodiakk.employeeservice.common.storage;

import com.zodiakk.employeeservice.config.minio.MinioProperties;
import com.zodiakk.employeeservice.employee.exception.profilephoto.ObjectStorageDeleteFailedException;
import com.zodiakk.employeeservice.employee.exception.profilephoto.ObjectStorageUploadFailedException;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class MinioObjectStorageService implements ObjectStorageService {

    private static final String DEFAULT_REGION = "us-east-1";

    private final MinioClient minioClient;

    private final MinioClient presignClient;

    private final MinioProperties minioProperties;

    public MinioObjectStorageService(MinioClient minioClient, MinioProperties minioProperties) {
        this.minioClient = minioClient;
        this.minioProperties = minioProperties;
        this.presignClient = buildPresignClient(minioClient, minioProperties);
    }

    @Override
    public void upload(String objectKey, InputStream inputStream, String contentType, long contentLength) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(objectKey)
                            .stream(inputStream, contentLength, -1)
                            .contentType(contentType)
                            .build()
            );
        } catch (Exception e) {
            throw new ObjectStorageUploadFailedException(e);
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            throw new ObjectStorageDeleteFailedException(e);
        }
    }

    @Override
    public String createPresignedGetUrl(String objectKey) {
        try {
            return presignClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(minioProperties.getBucket())
                            .object(objectKey)
                            .expiry((int) minioProperties.getPresignTtlSeconds())
                            .build()
            );
        } catch (Exception e) {
            throw new ObjectStorageUploadFailedException(e);
        }
    }

    private MinioClient buildPresignClient(MinioClient defaultClient, MinioProperties properties) {
        String publicEndpoint = properties.getPublicEndpoint();

        if (publicEndpoint == null
                || publicEndpoint.isBlank()
                || publicEndpoint.equals(properties.getEndpoint())) {
            return defaultClient;
        }

        return MinioClient.builder()
                .endpoint(publicEndpoint)
                .region(DEFAULT_REGION)
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
    }
}
