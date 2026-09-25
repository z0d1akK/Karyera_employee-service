package com.zodiakk.employeeservice.employee.service.impl;

import com.zodiakk.employeeservice.common.storage.ObjectStorageService;
import com.zodiakk.employeeservice.config.s3.S3Properties;
import com.zodiakk.employeeservice.employee.dto.response.EmployeeProfilePhotoResponseDto;
import com.zodiakk.employeeservice.employee.entity.Employee;
import com.zodiakk.employeeservice.employee.entity.EmployeeProfilePhoto;
import com.zodiakk.employeeservice.employee.exception.employee.EmployeeNotFoundException;
import com.zodiakk.employeeservice.employee.exception.profilephoto.FileTooLargeException;
import com.zodiakk.employeeservice.employee.exception.profilephoto.InvalidContentTypeException;
import com.zodiakk.employeeservice.employee.exception.profilephoto.ProfilePhotoNotFoundException;
import com.zodiakk.employeeservice.employee.exception.profilephoto.S3UploadFailedException;
import com.zodiakk.employeeservice.employee.mapper.EmployeeProfilePhotoMapper;
import com.zodiakk.employeeservice.employee.repository.EmployeeProfilePhotoRepository;
import com.zodiakk.employeeservice.employee.repository.EmployeeRepository;
import com.zodiakk.employeeservice.employee.service.EmployeeProfilePhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeProfilePhotoServiceImpl implements EmployeeProfilePhotoService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final EmployeeProfilePhotoRepository employeeProfilePhotoRepository;

    private final EmployeeRepository employeeRepository;

    private final ObjectStorageService objectStorageService;

    private final EmployeeProfilePhotoMapper employeeProfilePhotoMapper;

    private final S3Properties s3Properties;

    @Override
    @Transactional
    public EmployeeProfilePhotoResponseDto upload(UUID employeeId, MultipartFile file) {
        Employee employee = findEmployee(employeeId);
        validateFile(file);

        String contentType = file.getContentType();
        String extension = resolveExtension(contentType, file.getOriginalFilename());
        String objectKey = "employees/%s/profile/%s.%s".formatted(employeeId, UUID.randomUUID(), extension);

        try {
            objectStorageService.upload(objectKey, file.getInputStream(), contentType, file.getSize());
        } catch (IOException e) {
            throw new S3UploadFailedException(e);
        }

        EmployeeProfilePhoto photo = employeeProfilePhotoRepository.findByEmployeeId(employeeId)
                .orElse(null);

        if (photo != null) {
            String oldKey = photo.getObjectKey();
            photo.setObjectKey(objectKey);
            photo.setFileName(file.getOriginalFilename());
            photo.setContentType(contentType);
            photo.setFileSize(file.getSize());
            EmployeeProfilePhoto saved = employeeProfilePhotoRepository.save(photo);
            if (oldKey != null && !oldKey.equals(objectKey)) {
                objectStorageService.delete(oldKey);
            }
            return toResponseWithUrl(saved);
        }

        EmployeeProfilePhoto newPhoto = EmployeeProfilePhoto.builder()
                .employee(employee)
                .objectKey(objectKey)
                .fileName(file.getOriginalFilename())
                .contentType(contentType)
                .fileSize(file.getSize())
                .build();

        employee.setProfilePhoto(newPhoto);
        return toResponseWithUrl(employeeProfilePhotoRepository.save(newPhoto));
    }

    @Override
    public EmployeeProfilePhotoResponseDto getByEmployeeId(UUID employeeId) {
        findEmployee(employeeId);
        EmployeeProfilePhoto photo = employeeProfilePhotoRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ProfilePhotoNotFoundException(employeeId));
        return toResponseWithUrl(photo);
    }

    @Override
    @Transactional
    public void delete(UUID employeeId) {
        Employee employee = findEmployee(employeeId);
        EmployeeProfilePhoto photo = employeeProfilePhotoRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ProfilePhotoNotFoundException(employeeId));

        String objectKey = photo.getObjectKey();
        employee.setProfilePhoto(null);
        employeeProfilePhotoRepository.delete(photo);
        objectStorageService.delete(objectKey);
    }

    private EmployeeProfilePhotoResponseDto toResponseWithUrl(EmployeeProfilePhoto photo) {
        EmployeeProfilePhotoResponseDto response = employeeProfilePhotoMapper.toResponseDto(photo);
        response.setUrl(objectStorageService.createPresignedGetUrl(photo.getObjectKey()));
        return response;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidContentTypeException("empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new InvalidContentTypeException(contentType);
        }

        if (file.getSize() > s3Properties.getMaxFileSize()) {
            throw new FileTooLargeException(s3Properties.getMaxFileSize());
        }
    }

    private String resolveExtension(String contentType, String originalFilename) {
        if (contentType != null) {
            return switch (contentType.toLowerCase(Locale.ROOT)) {
                case "image/jpeg" -> "jpg";
                case "image/png" -> "png";
                case "image/webp" -> "webp";
                default -> "bin";
            };
        }

        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
        }

        return "bin";
    }

    private Employee findEmployee(UUID employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));
    }
}
