package com.zodiakk.employeeservice.common.storage;

import com.zodiakk.employeeservice.common.exception.ErrorMessages;
import com.zodiakk.employeeservice.config.minio.MinioProperties;
import com.zodiakk.employeeservice.employee.exception.profilephoto.ObjectStorageDeleteFailedException;
import com.zodiakk.employeeservice.employee.exception.profilephoto.ObjectStorageUploadFailedException;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MinioObjectStorageServiceTest {

    private static final String BUCKET = "employee-avatars";

    private static final String OBJECT_KEY = "photos/employee-1.png";

    private static final String CONTENT_TYPE = "image/png";

    private static final long CONTENT_LENGTH = 5L;

    private static final long PRESIGN_TTL_SECONDS = 600L;

    private static final String PRESIGNED_URL =
            "https://minio.example.com/employee-avatars/photos/employee-1.png?signature=abc";

    @Mock
    private MinioClient minioClient;

    private MinioObjectStorageService service;

    @BeforeEach
    void setUp() {
        MinioProperties properties = new MinioProperties();
        properties.setBucket(BUCKET);
        properties.setPresignTtlSeconds(PRESIGN_TTL_SECONDS);
        service = new MinioObjectStorageService(minioClient, properties);
    }

    @Nested
    @DisplayName("upload")
    class Upload {

        @Test
        @DisplayName("uploads the stream with the configured bucket, object, content type and size")
        void shouldUploadObject() throws Exception {
            InputStream inputStream = new ByteArrayInputStream(new byte[]{1, 2, 3, 4, 5});

            service.upload(OBJECT_KEY, inputStream, CONTENT_TYPE, CONTENT_LENGTH);

            ArgumentCaptor<PutObjectArgs> captor = ArgumentCaptor.forClass(PutObjectArgs.class);
            verify(minioClient).putObject(captor.capture());

            PutObjectArgs args = captor.getValue();

            assertThat(args.bucket()).isEqualTo(BUCKET);
            assertThat(args.object()).isEqualTo(OBJECT_KEY);
            assertThat(args.contentType()).isEqualTo(CONTENT_TYPE);
            assertThat(args.objectSize()).isEqualTo(CONTENT_LENGTH);
            assertThat(args.stream()).isNotNull();
        }

        @Test
        @DisplayName("wraps upload failures in ObjectStorageUploadFailedException")
        void shouldWrapUploadFailure() throws Exception {
            IOException failure = new IOException("minio is down");
            doThrow(failure).when(minioClient).putObject(any(PutObjectArgs.class));
            InputStream inputStream = new ByteArrayInputStream(new byte[]{1});

            assertThatThrownBy(() ->
                    service.upload(OBJECT_KEY, inputStream, CONTENT_TYPE, CONTENT_LENGTH))
                    .isInstanceOf(ObjectStorageUploadFailedException.class)
                    .hasMessage(ErrorMessages.OBJECT_STORAGE_UPLOAD_FAILED)
                    .hasCause(failure);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("removes the object from the configured bucket")
        void shouldDeleteObject() throws Exception {
            service.delete(OBJECT_KEY);

            ArgumentCaptor<RemoveObjectArgs> captor =
                    ArgumentCaptor.forClass(RemoveObjectArgs.class);
            verify(minioClient).removeObject(captor.capture());

            RemoveObjectArgs args = captor.getValue();

            assertThat(args.bucket()).isEqualTo(BUCKET);
            assertThat(args.object()).isEqualTo(OBJECT_KEY);
        }

        @Test
        @DisplayName("wraps delete failures in ObjectStorageDeleteFailedException")
        void shouldWrapDeleteFailure() throws Exception {
            IOException failure = new IOException("minio is down");
            doThrow(failure).when(minioClient).removeObject(any(RemoveObjectArgs.class));

            assertThatThrownBy(() -> service.delete(OBJECT_KEY))
                    .isInstanceOf(ObjectStorageDeleteFailedException.class)
                    .hasMessage(ErrorMessages.OBJECT_STORAGE_DELETE_FAILED)
                    .hasCause(failure);
        }
    }

    @Nested
    @DisplayName("createPresignedGetUrl")
    class CreatePresignedGetUrl {

        @Test
        @DisplayName("creates a GET URL with the configured bucket, object and expiry")
        void shouldCreatePresignedUrl() throws Exception {
            when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                    .thenReturn(PRESIGNED_URL);

            String url = service.createPresignedGetUrl(OBJECT_KEY);

            assertThat(url).isEqualTo(PRESIGNED_URL);

            ArgumentCaptor<GetPresignedObjectUrlArgs> captor =
                    ArgumentCaptor.forClass(GetPresignedObjectUrlArgs.class);
            verify(minioClient).getPresignedObjectUrl(captor.capture());

            GetPresignedObjectUrlArgs args = captor.getValue();

            assertThat(args.method()).isEqualTo(Method.GET);
            assertThat(args.bucket()).isEqualTo(BUCKET);
            assertThat(args.object()).isEqualTo(OBJECT_KEY);
            assertThat(args.expiry()).isEqualTo((int) PRESIGN_TTL_SECONDS);
        }

        @Test
        @DisplayName("signs a public URL locally when the public endpoint is unreachable")
        void shouldPresignAgainstUnreachablePublicEndpoint() {
            MinioProperties properties = new MinioProperties();
            properties.setEndpoint("http://minio:9000");
            properties.setPublicEndpoint("http://127.0.0.1:9");
            properties.setAccessKey("minioadmin");
            properties.setSecretKey("minioadmin");
            properties.setBucket(BUCKET);
            properties.setPresignTtlSeconds(PRESIGN_TTL_SECONDS);

            MinioObjectStorageService publicService = new MinioObjectStorageService(minioClient, properties);

            String url = publicService.createPresignedGetUrl(OBJECT_KEY);

            assertThat(url).startsWith("http://127.0.0.1:9/" + BUCKET + "/" + OBJECT_KEY);
        }

        @Test
        @DisplayName("wraps presigned URL failures in ObjectStorageUploadFailedException")
        void shouldWrapPresignedUrlFailure() throws Exception {
            IOException failure = new IOException("minio is down");
            doThrow(failure).when(minioClient)
                    .getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));

            assertThatThrownBy(() -> service.createPresignedGetUrl(OBJECT_KEY))
                    .isInstanceOf(ObjectStorageUploadFailedException.class)
                    .hasMessage(ErrorMessages.OBJECT_STORAGE_UPLOAD_FAILED)
                    .hasCause(failure);
        }
    }
}
