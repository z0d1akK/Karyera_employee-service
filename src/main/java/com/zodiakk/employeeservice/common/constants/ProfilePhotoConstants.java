package com.zodiakk.employeeservice.common.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProfilePhotoConstants {

    public static final String CONTENT_TYPE_JPEG = "image/jpeg";

    public static final String CONTENT_TYPE_PNG = "image/png";

    public static final String CONTENT_TYPE_WEBP = "image/webp";

    public static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            CONTENT_TYPE_JPEG,
            CONTENT_TYPE_PNG,
            CONTENT_TYPE_WEBP
    );

    public static final String ALLOWED_CONTENT_TYPES_TEXT = String.join(", ", ALLOWED_CONTENT_TYPES);

    public static final String OBJECT_KEY_TEMPLATE = "employees/%s/profile/%s.%s";
}
