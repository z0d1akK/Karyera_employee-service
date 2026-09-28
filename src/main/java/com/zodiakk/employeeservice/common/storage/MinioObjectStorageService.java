package com.zodiakk.employeeservice.common.storage;

import com.zodiakk.employeeservice.config.minio.MinioProperties;
import com.zodiakk.employeeservice.employee.exception.profilephoto.ObjectStorageUploadFailedException;
import com.zodiakk.employeeservice.employee.exception.profilephoto.ObjectStotageDeleteFailedException;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class MinioObjectStorageService implements ObjectStorageService {

    private final MinioClient minioClient;

    private final MinioProperties minioProperties;

    @Override
    public void upload(
            String objectKey,
            InputStream inputStream,
            String contentType,
            long contentLength
    ) {
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
            throw new ObjectStotageDeleteFailedException(e);
        }
    }

    @Override
    public String createPresignedGetUrl(String objectKey) {
        try {
            return minioClient.getPresignedObjectUrl(
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
}