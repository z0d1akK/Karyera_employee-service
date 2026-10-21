package com.zodiakk.employeeservice.employee;

import com.zodiakk.employeeservice.common.AbstractIntegrationTest;
import com.zodiakk.employeeservice.employee.entity.Employee;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Employee profile photo")
class EmployeeProfilePhotoIntegrationTest extends AbstractIntegrationTest {

    private static final byte[] PNG_BYTES = "fake-png-content".getBytes(StandardCharsets.UTF_8);

    private static final byte[] JPEG_BYTES = "fake-jpeg-content".getBytes(StandardCharsets.UTF_8);

    @Nested
    @DisplayName("upload")
    class Upload {

        @Test
        @DisplayName("uploads a photo, replaces it and deletes the previous object")
        void shouldUploadAndReplacePhoto() throws Exception {
            Employee employee = newEmployee();
            String url = url(employee);

            mockMvc.perform(asHr(multipart(url)
                            .file(new MockMultipartFile(
                                    "file", "avatar.png", "image/png", PNG_BYTES
                            ))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.employeeId").value(employee.getId().toString()))
                    .andExpect(jsonPath("$.fileName").value("avatar.png"))
                    .andExpect(jsonPath("$.contentType").value("image/png"))
                    .andExpect(jsonPath("$.fileSize").value(PNG_BYTES.length))
                    .andExpect(jsonPath("$.url").value(org.hamcrest.Matchers.nullValue()));

            assertThat(employeeProfilePhotoRepository.findByEmployeeId(employee.getId())).isPresent();

            mockMvc.perform(asHr(multipart(url)
                            .file(new MockMultipartFile(
                                    "file", "avatar.jpg", "image/jpeg", JPEG_BYTES
                            ))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fileName").value("avatar.jpg"))
                    .andExpect(jsonPath("$.contentType").value("image/jpeg"));

            ArgumentCaptor<String> uploadKeys = ArgumentCaptor.forClass(String.class);
            verify(objectStorageService, times(2)).upload(
                    uploadKeys.capture(),
                    any(InputStream.class),
                    anyString(),
                    anyLong()
            );

            String firstKey = uploadKeys.getAllValues().get(0);
            String secondKey = uploadKeys.getAllValues().get(1);

            assertThat(firstKey)
                    .startsWith("employees/" + employee.getId() + "/profile/")
                    .endsWith(".png");
            assertThat(secondKey)
                    .startsWith("employees/" + employee.getId() + "/profile/")
                    .endsWith(".jpg");
            verify(objectStorageService).delete(firstKey);
        }

        @Test
        @DisplayName("lets an employee upload his own photo")
        void shouldLetEmployeeUploadOwnPhoto() throws Exception {
            Employee employee = newEmployee();

            mockMvc.perform(asEmployee(multipart(url(employee))
                            .file(new MockMultipartFile(
                                    "file", "avatar.png", "image/png", PNG_BYTES
                            )), employee.getUserId()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("forbids another employee to upload the photo")
        void shouldForbidForeignUpload() throws Exception {
            Employee employee = newEmployee();

            mockMvc.perform(asEmployee(multipart(url(employee))
                            .file(new MockMultipartFile(
                                    "file", "avatar.png", "image/png", PNG_BYTES
                            )), UUID.randomUUID()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("rejects an empty file")
        void shouldRejectEmptyFile() throws Exception {
            Employee employee = newEmployee();

            mockMvc.perform(asHr(multipart(url(employee))
                            .file(new MockMultipartFile(
                                    "file", "empty.png", "image/png", new byte[0]
                            ))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("rejects an unsupported content type")
        void shouldRejectUnsupportedContentType() throws Exception {
            Employee employee = newEmployee();

            mockMvc.perform(asHr(multipart(url(employee))
                            .file(new MockMultipartFile(
                                    "file", "document.txt", "text/plain", PNG_BYTES
                            ))))
                    .andExpect(status().isUnsupportedMediaType());
        }

        @Test
        @DisplayName("rejects a file larger than the configured limit")
        void shouldRejectOversizedFile() throws Exception {
            Employee employee = newEmployee();
            byte[] tooLarge = new byte[5 * 1024 * 1024 + 1];

            mockMvc.perform(asHr(multipart(url(employee))
                            .file(new MockMultipartFile(
                                    "file", "huge.png", "image/png", tooLarge
                            ))))
                    .andExpect(status().isPayloadTooLarge());
        }

        @Test
        @DisplayName("returns 404 for an unknown employee")
        void shouldReturnNotFoundForUnknownEmployee() throws Exception {
            mockMvc.perform(asHr(multipart(url(UUID.randomUUID()))
                            .file(new MockMultipartFile(
                                    "file", "avatar.png", "image/png", PNG_BYTES
                            ))))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("read and delete")
    class ReadAndDelete {

        @Test
        @DisplayName("returns the uploaded photo and deletes it")
        void shouldGetAndDeletePhoto() throws Exception {
            Employee employee = newEmployee();

            mockMvc.perform(asHr(multipart(url(employee))
                            .file(new MockMultipartFile(
                                    "file", "avatar.png", "image/png", PNG_BYTES
                            ))))
                    .andExpect(status().isOk());

            mockMvc.perform(asHr(get(url(employee))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fileName").value("avatar.png"));

            mockMvc.perform(asHr(delete(url(employee))))
                    .andExpect(status().isNoContent());

            assertThat(employeeProfilePhotoRepository.findByEmployeeId(employee.getId())).isEmpty();
            verify(objectStorageService).delete(anyString());
        }

        @Test
        @DisplayName("returns 404 when the photo is missing")
        void shouldReturnNotFoundWithoutPhoto() throws Exception {
            Employee employee = newEmployee();

            mockMvc.perform(asHr(get(url(employee))))
                    .andExpect(status().isNotFound());
            mockMvc.perform(asHr(delete(url(employee))))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("allows the owner to read the photo")
        void shouldAllowOwnerToReadPhoto() throws Exception {
            Employee employee = newEmployee();

            mockMvc.perform(asHr(multipart(url(employee))
                            .file(new MockMultipartFile(
                                    "file", "avatar.png", "image/png", PNG_BYTES
                            ))))
                    .andExpect(status().isOk());

            mockMvc.perform(asEmployee(get(url(employee)), employee.getUserId()))
                    .andExpect(status().isOk());
        }
    }

    private Employee newEmployee() {
        return persistEmployee(
                persistPosition("Developer-" + UUID.randomUUID()),
                persistGrade("Lead-" + UUID.randomUUID(), nextGradeLevel())
        );
    }

    private String url(Employee employee) {
        return "/api/employees/" + employee.getId() + "/profile-photo";
    }

    private String url(UUID employeeId) {
        return "/api/employees/" + employeeId + "/profile-photo";
    }
}
