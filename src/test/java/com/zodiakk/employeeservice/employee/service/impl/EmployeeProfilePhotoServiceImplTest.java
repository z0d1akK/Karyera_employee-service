package com.zodiakk.employeeservice.employee.service.impl;

import com.zodiakk.employeeservice.common.storage.ObjectStorageService;
import com.zodiakk.employeeservice.config.minio.MinioProperties;
import com.zodiakk.employeeservice.employee.dto.response.EmployeeProfilePhotoResponseDto;
import com.zodiakk.employeeservice.employee.entity.Employee;
import com.zodiakk.employeeservice.employee.entity.EmployeeProfilePhoto;
import com.zodiakk.employeeservice.employee.entity.enums.EmploymentStatus;
import com.zodiakk.employeeservice.employee.exception.employee.EmployeeNotFoundException;
import com.zodiakk.employeeservice.employee.exception.profilephoto.EmptyFileException;
import com.zodiakk.employeeservice.employee.exception.profilephoto.FileTooLargeException;
import com.zodiakk.employeeservice.employee.exception.profilephoto.InvalidContentTypeException;
import com.zodiakk.employeeservice.employee.exception.profilephoto.ObjectStorageUploadFailedException;
import com.zodiakk.employeeservice.employee.exception.profilephoto.ProfilePhotoNotFoundException;
import com.zodiakk.employeeservice.employee.mapper.EmployeeProfilePhotoMapper;
import com.zodiakk.employeeservice.employee.repository.EmployeeProfilePhotoRepository;
import com.zodiakk.employeeservice.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeProfilePhotoServiceImplTest {

    private static final UUID EMPLOYEE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final UUID PHOTO_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private static final long MAX_FILE_SIZE = 5_242_880L;

    private static final String OLD_OBJECT_KEY = "employees/old/profile/old.jpg";

    private static final String PRESIGNED_URL = "http://minio/employees/photo";

    @Mock
    private EmployeeProfilePhotoRepository employeeProfilePhotoRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ObjectStorageService objectStorageService;

    @Mock
    private EmployeeProfilePhotoMapper employeeProfilePhotoMapper;

    @Mock
    private MinioProperties minioProperties;

    @InjectMocks
    private EmployeeProfilePhotoServiceImpl profilePhotoService;

    @BeforeEach
    void stubFileSizeLimit() {
        lenient().when(minioProperties.getMaxFileSize()).thenReturn(MAX_FILE_SIZE);
    }

    @Nested
    @DisplayName("upload")
    class Upload {

        @Test
        @DisplayName("throws when the employee does not exist")
        void shouldRejectMissingEmployee() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> profilePhotoService.upload(EMPLOYEE_ID, multipart()))
                    .isInstanceOf(EmployeeNotFoundException.class);

            verifyNoInteractions(objectStorageService);
        }

        @Test
        @DisplayName("throws when the file is null")
        void shouldRejectNullFile() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee()));

            assertThatThrownBy(() -> profilePhotoService.upload(EMPLOYEE_ID, null))
                    .isInstanceOf(EmptyFileException.class);
        }

        @Test
        @DisplayName("throws when the file is empty")
        void shouldRejectEmptyFile() {
            MultipartFile file = mock(MultipartFile.class);

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee()));
            when(file.isEmpty()).thenReturn(true);

            assertThatThrownBy(() -> profilePhotoService.upload(EMPLOYEE_ID, file))
                    .isInstanceOf(EmptyFileException.class);

            verifyNoInteractions(objectStorageService);
        }

        @Test
        @DisplayName("throws when the content type is missing")
        void shouldRejectMissingContentType() {
            MultipartFile file = mock(MultipartFile.class);

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee()));
            when(file.isEmpty()).thenReturn(false);
            when(file.getContentType()).thenReturn(null);

            assertThatThrownBy(() -> profilePhotoService.upload(EMPLOYEE_ID, file))
                    .isInstanceOf(InvalidContentTypeException.class);
        }

        @Test
        @DisplayName("throws when the content type is not an image")
        void shouldRejectUnsupportedContentType() {
            MultipartFile file = mock(MultipartFile.class);

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee()));
            when(file.isEmpty()).thenReturn(false);
            when(file.getContentType()).thenReturn("application/pdf");

            assertThatThrownBy(() -> profilePhotoService.upload(EMPLOYEE_ID, file))
                    .isInstanceOf(InvalidContentTypeException.class);

            verifyNoInteractions(objectStorageService);
        }

        @Test
        @DisplayName("throws when the file exceeds the configured limit")
        void shouldRejectOversizedFile() {
            MultipartFile file = mock(MultipartFile.class);

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee()));
            when(file.isEmpty()).thenReturn(false);
            when(file.getContentType()).thenReturn("image/jpeg");
            when(file.getSize()).thenReturn(MAX_FILE_SIZE + 1);
            when(minioProperties.getMaxFileSize()).thenReturn(MAX_FILE_SIZE);

            assertThatThrownBy(() -> profilePhotoService.upload(EMPLOYEE_ID, file))
                    .isInstanceOf(FileTooLargeException.class);

            verifyNoInteractions(objectStorageService);
        }

        @Test
        @DisplayName("stores a new photo, uploads the object and returns a presigned url")
        void shouldUploadNewPhoto() throws IOException {
            Employee employee = employee();
            MultipartFile file = jpegFile("avatar.jpg", 1024L);
            InputStream stream = new ByteArrayInputStream(new byte[]{1, 2, 3});
            EmployeeProfilePhotoResponseDto expected =
                    EmployeeProfilePhotoResponseDto.builder().id(PHOTO_ID).url(PRESIGNED_URL).build();

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
            when(file.getInputStream()).thenReturn(stream);
            when(employeeProfilePhotoRepository.findByEmployeeId(EMPLOYEE_ID)).thenReturn(Optional.empty());
            when(employeeProfilePhotoRepository.save(any(EmployeeProfilePhoto.class))).thenAnswer(invocation -> {
                EmployeeProfilePhoto photo = invocation.getArgument(0);
                photo.setId(PHOTO_ID);
                return photo;
            });
            when(employeeProfilePhotoMapper.toResponseDto(any(EmployeeProfilePhoto.class))).thenReturn(expected);
            when(objectStorageService.createPresignedGetUrl(anyString())).thenReturn(PRESIGNED_URL);

            EmployeeProfilePhotoResponseDto actual = profilePhotoService.upload(EMPLOYEE_ID, file);

            assertThat(actual).isSameAs(expected);

            ArgumentCaptor<String> objectKey = ArgumentCaptor.forClass(String.class);
            verify(objectStorageService).upload(
                    objectKey.capture(),
                    eq(stream),
                    eq("image/jpeg"),
                    eq(1024L)
            );
            assertThat(objectKey.getValue()).matches(
                    "employees/" + EMPLOYEE_ID + "/profile/[0-9a-f-]{36}\\.jpg"
            );

            ArgumentCaptor<EmployeeProfilePhoto> captor =
                    ArgumentCaptor.forClass(EmployeeProfilePhoto.class);
            verify(employeeProfilePhotoRepository).save(captor.capture());

            EmployeeProfilePhoto saved = captor.getValue();
            assertThat(saved.getEmployee()).isSameAs(employee);
            assertThat(saved.getObjectKey()).isEqualTo(objectKey.getValue());
            assertThat(saved.getFileName()).isEqualTo("avatar.jpg");
            assertThat(saved.getContentType()).isEqualTo("image/jpeg");
            assertThat(saved.getFileSize()).isEqualTo(1024L);
            assertThat(employee.getProfilePhoto()).isSameAs(saved);
            verify(objectStorageService, never()).delete(anyString());
        }

        @Test
        @DisplayName("maps png content type to the png extension")
        void shouldUsePngExtension() throws IOException {
            MultipartFile file = imageFile("image/png", "avatar.png", 10L);

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee()));
            when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{1}));
            when(employeeProfilePhotoRepository.findByEmployeeId(EMPLOYEE_ID)).thenReturn(Optional.empty());
            when(employeeProfilePhotoRepository.save(any(EmployeeProfilePhoto.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(employeeProfilePhotoMapper.toResponseDto(any(EmployeeProfilePhoto.class)))
                    .thenReturn(EmployeeProfilePhotoResponseDto.builder().build());
            when(objectStorageService.createPresignedGetUrl(anyString())).thenReturn(PRESIGNED_URL);

            profilePhotoService.upload(EMPLOYEE_ID, file);

            ArgumentCaptor<String> objectKey = ArgumentCaptor.forClass(String.class);
            verify(objectStorageService).upload(objectKey.capture(), any(), eq("image/png"), eq(10L));
            assertThat(objectKey.getValue()).endsWith(".png");
        }

        @Test
        @DisplayName("maps webp content type to the webp extension")
        void shouldUseWebpExtension() throws IOException {
            MultipartFile file = imageFile("image/webp", "avatar.webp", 20L);

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee()));
            when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{1}));
            when(employeeProfilePhotoRepository.findByEmployeeId(EMPLOYEE_ID)).thenReturn(Optional.empty());
            when(employeeProfilePhotoRepository.save(any(EmployeeProfilePhoto.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(employeeProfilePhotoMapper.toResponseDto(any(EmployeeProfilePhoto.class)))
                    .thenReturn(EmployeeProfilePhotoResponseDto.builder().build());
            when(objectStorageService.createPresignedGetUrl(anyString())).thenReturn(PRESIGNED_URL);

            profilePhotoService.upload(EMPLOYEE_ID, file);

            ArgumentCaptor<String> objectKey = ArgumentCaptor.forClass(String.class);
            verify(objectStorageService).upload(objectKey.capture(), any(), eq("image/webp"), eq(20L));
            assertThat(objectKey.getValue()).endsWith(".webp");
        }

        @Test
        @DisplayName("replaces an existing photo and removes the previous object")
        void shouldReplaceExistingPhoto() throws IOException {
            Employee employee = employee();
            EmployeeProfilePhoto existing = EmployeeProfilePhoto.builder()
                    .id(PHOTO_ID)
                    .employee(employee)
                    .objectKey(OLD_OBJECT_KEY)
                    .fileName("old.jpg")
                    .contentType("image/jpeg")
                    .fileSize(5L)
                    .build();
            MultipartFile file = jpegFile("new.jpg", 2048L);

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
            when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{1}));
            when(employeeProfilePhotoRepository.findByEmployeeId(EMPLOYEE_ID))
                    .thenReturn(Optional.of(existing));
            when(employeeProfilePhotoRepository.save(existing)).thenReturn(existing);
            when(employeeProfilePhotoMapper.toResponseDto(existing))
                    .thenReturn(EmployeeProfilePhotoResponseDto.builder().id(PHOTO_ID).build());
            when(objectStorageService.createPresignedGetUrl(anyString())).thenReturn(PRESIGNED_URL);

            profilePhotoService.upload(EMPLOYEE_ID, file);

            ArgumentCaptor<String> objectKey = ArgumentCaptor.forClass(String.class);
            verify(objectStorageService).upload(objectKey.capture(), any(), eq("image/jpeg"), eq(2048L));

            assertThat(existing.getObjectKey()).isEqualTo(objectKey.getValue());
            assertThat(existing.getObjectKey()).isNotEqualTo(OLD_OBJECT_KEY);
            assertThat(existing.getFileName()).isEqualTo("new.jpg");
            assertThat(existing.getFileSize()).isEqualTo(2048L);

            verify(objectStorageService).delete(OLD_OBJECT_KEY);
        }

        @Test
        @DisplayName("skips storage deletion when the previous photo has no object key")
        void shouldSkipDeleteWhenOldKeyIsMissing() throws IOException {
            Employee employee = employee();
            EmployeeProfilePhoto existing = EmployeeProfilePhoto.builder()
                    .id(PHOTO_ID)
                    .employee(employee)
                    .fileName("old.jpg")
                    .build();
            MultipartFile file = jpegFile("new.jpg", 2048L);

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
            when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{1}));
            when(employeeProfilePhotoRepository.findByEmployeeId(EMPLOYEE_ID))
                    .thenReturn(Optional.of(existing));
            when(employeeProfilePhotoRepository.save(existing)).thenReturn(existing);
            when(employeeProfilePhotoMapper.toResponseDto(existing))
                    .thenReturn(EmployeeProfilePhotoResponseDto.builder().id(PHOTO_ID).build());
            when(objectStorageService.createPresignedGetUrl(anyString())).thenReturn(PRESIGNED_URL);

            profilePhotoService.upload(EMPLOYEE_ID, file);

            verify(objectStorageService, never()).delete(anyString());
        }

        @Test
        @DisplayName("wraps IO failures while reading the uploaded file")
        void shouldWrapIoFailure() throws IOException {
            MultipartFile file = jpegFile("avatar.jpg", 1024L);

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee()));
            when(file.getInputStream()).thenThrow(new IOException("boom"));

            assertThatThrownBy(() -> profilePhotoService.upload(EMPLOYEE_ID, file))
                    .isInstanceOf(ObjectStorageUploadFailedException.class);

            verify(employeeProfilePhotoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getByEmployeeId")
    class GetByEmployeeId {

        @Test
        @DisplayName("throws when the employee does not exist")
        void shouldRejectMissingEmployee() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> profilePhotoService.getByEmployeeId(EMPLOYEE_ID))
                    .isInstanceOf(EmployeeNotFoundException.class);
        }

        @Test
        @DisplayName("throws when the photo does not exist")
        void shouldRejectMissingPhoto() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee()));
            when(employeeProfilePhotoRepository.findByEmployeeId(EMPLOYEE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> profilePhotoService.getByEmployeeId(EMPLOYEE_ID))
                    .isInstanceOf(ProfilePhotoNotFoundException.class);
        }

        @Test
        @DisplayName("returns the photo with a presigned url")
        void shouldReturnPhoto() {
            EmployeeProfilePhoto photo = EmployeeProfilePhoto.builder()
                    .id(PHOTO_ID)
                    .objectKey(OLD_OBJECT_KEY)
                    .build();
            EmployeeProfilePhotoResponseDto response =
                    EmployeeProfilePhotoResponseDto.builder().id(PHOTO_ID).build();

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee()));
            when(employeeProfilePhotoRepository.findByEmployeeId(EMPLOYEE_ID)).thenReturn(Optional.of(photo));
            when(employeeProfilePhotoMapper.toResponseDto(photo)).thenReturn(response);
            when(objectStorageService.createPresignedGetUrl(OLD_OBJECT_KEY)).thenReturn(PRESIGNED_URL);

            EmployeeProfilePhotoResponseDto actual = profilePhotoService.getByEmployeeId(EMPLOYEE_ID);

            assertThat(actual.getUrl()).isEqualTo(PRESIGNED_URL);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("throws when the employee does not exist")
        void shouldRejectMissingEmployee() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> profilePhotoService.delete(EMPLOYEE_ID))
                    .isInstanceOf(EmployeeNotFoundException.class);
        }

        @Test
        @DisplayName("throws when the photo does not exist")
        void shouldRejectMissingPhoto() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee()));
            when(employeeProfilePhotoRepository.findByEmployeeId(EMPLOYEE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> profilePhotoService.delete(EMPLOYEE_ID))
                    .isInstanceOf(ProfilePhotoNotFoundException.class);

            verifyNoInteractions(objectStorageService);
        }

        @Test
        @DisplayName("deletes the photo row, detaches it from the employee and removes the object")
        void shouldDeletePhoto() {
            Employee employee = employee();
            EmployeeProfilePhoto photo = EmployeeProfilePhoto.builder()
                    .id(PHOTO_ID)
                    .employee(employee)
                    .objectKey(OLD_OBJECT_KEY)
                    .build();
            employee.setProfilePhoto(photo);

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
            when(employeeProfilePhotoRepository.findByEmployeeId(EMPLOYEE_ID)).thenReturn(Optional.of(photo));

            assertThatCode(() -> profilePhotoService.delete(EMPLOYEE_ID)).doesNotThrowAnyException();

            assertThat(employee.getProfilePhoto()).isNull();
            verify(employeeProfilePhotoRepository).delete(photo);
            verify(objectStorageService).delete(OLD_OBJECT_KEY);
        }
    }

    private static Employee employee() {
        return Employee.builder()
                .id(EMPLOYEE_ID)
                .firstName("Ivan")
                .lastName("Petrov")
                .email("ivan.petrov@company.com")
                .hireDate(LocalDate.of(2024, 1, 1))
                .employmentStatus(EmploymentStatus.ACTIVE)
                .build();
    }

    private MultipartFile multipart() {
        return mock(MultipartFile.class);
    }

    private MultipartFile jpegFile(String fileName, long size) {
        return imageFile("image/jpeg", fileName, size);
    }

    private MultipartFile imageFile(String contentType, String fileName, long size) {
        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn(contentType);
        lenient().when(file.getOriginalFilename()).thenReturn(fileName);
        lenient().when(file.getSize()).thenReturn(size);

        return file;
    }
}
