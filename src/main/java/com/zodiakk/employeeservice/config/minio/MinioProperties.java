package com.zodiakk.employeeservice.config.minio;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.minio")
public class MinioProperties {

    private String endpoint;

    private String accessKey;

    private String secretKey;

    private String bucket;

    private long maxFileSize = 5_242_880L;

    private long presignTtlSeconds = 900L;
}