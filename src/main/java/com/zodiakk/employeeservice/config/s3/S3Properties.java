package com.zodiakk.employeeservice.config.s3;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.s3")
public class S3Properties {

    private String bucket;

    private String region = "us-east-1";

    private String endpoint;

    private String accessKey;

    private String secretKey;

    private long maxFileSize = 5_242_880L;

    private long presignTtlSeconds = 900L;
}
