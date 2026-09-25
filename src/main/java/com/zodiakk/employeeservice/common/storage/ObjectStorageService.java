package com.zodiakk.employeeservice.common.storage;

import java.io.InputStream;

public interface ObjectStorageService {

    void upload(String objectKey, InputStream inputStream, String contentType, long contentLength);

    void delete(String objectKey);

    String createPresignedGetUrl(String objectKey);
}
