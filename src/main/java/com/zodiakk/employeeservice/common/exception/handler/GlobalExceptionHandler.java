package com.zodiakk.employeeservice.common.exception.handler;

import com.zodiakk.employeeservice.common.dto.field.ValidationErrorField;
import com.zodiakk.employeeservice.common.dto.response.ApiErrorResponse;
import com.zodiakk.employeeservice.common.dto.response.ValidationErrorResponse;
import com.zodiakk.employeeservice.common.exception.BusinessException;
import com.zodiakk.employeeservice.common.exception.ErrorMessages;
import com.zodiakk.employeeservice.common.exception.ResourceNotFoundException;
import com.zodiakk.employeeservice.employee.exception.department.DepartmentAlreadyExistsException;
import com.zodiakk.employeeservice.employee.exception.department.DepartmentInUseException;
import com.zodiakk.employeeservice.employee.exception.employee.EmployeeAlreadyExistsException;
import com.zodiakk.employeeservice.employee.exception.employeeresponsibility.MaxSubordinatesExceededException;
import com.zodiakk.employeeservice.employee.exception.employeeresponsibility.ResponsibilityAlreadyActiveException;
import com.zodiakk.employeeservice.employee.exception.employeeskill.EmployeeSkillAlreadyExistsException;
import com.zodiakk.employeeservice.employee.exception.grade.GradeAlreadyExistsException;
import com.zodiakk.employeeservice.employee.exception.grade.GradeInUseException;
import com.zodiakk.employeeservice.employee.exception.military.MilitaryObligationAlreadyExistsException;
import com.zodiakk.employeeservice.employee.exception.position.PositionAlreadyExistsException;
import com.zodiakk.employeeservice.employee.exception.position.PositionInUseException;
import com.zodiakk.employeeservice.employee.exception.profilephoto.EmptyFileException;
import com.zodiakk.employeeservice.employee.exception.profilephoto.FileTooLargeException;
import com.zodiakk.employeeservice.employee.exception.profilephoto.InvalidContentTypeException;
import com.zodiakk.employeeservice.employee.exception.profilephoto.ObjectStorageDeleteFailedException;
import com.zodiakk.employeeservice.employee.exception.profilephoto.ObjectStorageUploadFailedException;
import com.zodiakk.employeeservice.employee.exception.responsibilitytype.ResponsibilityTypeAlreadyExistsException;
import com.zodiakk.employeeservice.employee.exception.responsibilitytype.ResponsibilityTypeInUseException;
import com.zodiakk.employeeservice.employee.exception.skill.SkillAlreadyExistsException;
import com.zodiakk.employeeservice.employee.exception.skill.SkillInUseException;
import com.zodiakk.employeeservice.employee.exception.specialization.SpecializationAlreadyExistsException;
import com.zodiakk.employeeservice.employee.exception.specialization.SpecializationInUseException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({
            DepartmentAlreadyExistsException.class,
            DepartmentInUseException.class,
            PositionAlreadyExistsException.class,
            PositionInUseException.class,
            GradeAlreadyExistsException.class,
            GradeInUseException.class,
            SpecializationAlreadyExistsException.class,
            SpecializationInUseException.class,
            SkillAlreadyExistsException.class,
            SkillInUseException.class,
            ResponsibilityTypeAlreadyExistsException.class,
            ResponsibilityTypeInUseException.class,
            EmployeeAlreadyExistsException.class,
            EmployeeSkillAlreadyExistsException.class,
            MilitaryObligationAlreadyExistsException.class,
            ResponsibilityAlreadyActiveException.class,
            MaxSubordinatesExceededException.class
    })
    public ResponseEntity<ApiErrorResponse> handleConflict(BusinessException ex) {
        return error(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(FileTooLargeException.class)
    public ResponseEntity<ApiErrorResponse> handleFileTooLarge(FileTooLargeException ex) {
        return error(HttpStatus.PAYLOAD_TOO_LARGE, ex.getMessage());
    }

    @ExceptionHandler(InvalidContentTypeException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidContentType(InvalidContentTypeException ex) {
        return error(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getMessage());
    }

    @ExceptionHandler(EmptyFileException.class)
    public ResponseEntity<ApiErrorResponse> handleEmptyFile(EmptyFileException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        return error(HttpStatus.PAYLOAD_TOO_LARGE, ErrorMessages.UPLOAD_SIZE_EXCEEDED);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiErrorResponse> handleMultipart(MultipartException ex) {
        return error(HttpStatus.BAD_REQUEST, ErrorMessages.INVALID_REQUEST_BODY);
    }

    @ExceptionHandler({
            ObjectStorageUploadFailedException.class,
            ObjectStorageDeleteFailedException.class
    })
    public ResponseEntity<ApiErrorResponse> handleObjectStorageFailure(BusinessException ex) {
        log.warn("Object storage operation failed", ex);

        return error(HttpStatus.BAD_GATEWAY, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return error(HttpStatus.FORBIDDEN, ErrorMessages.ACCESS_DENIED);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusiness(BusinessException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        return fieldValidationError(ex.getBindingResult().getFieldErrors());
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ValidationErrorResponse> handleBinding(BindException ex) {
        return fieldValidationError(ex.getBindingResult().getFieldErrors());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        List<ValidationErrorField> errors = ex.getConstraintViolations()
                .stream()
                .map(violation -> ValidationErrorField.builder()
                        .field(violation.getPropertyPath().toString())
                        .message(violation.getMessage())
                        .build())
                .toList();

        return validationError(errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleNotReadable(HttpMessageNotReadableException ex) {
        return error(HttpStatus.BAD_REQUEST, ErrorMessages.INVALID_REQUEST_BODY);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFound(NoResourceFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ErrorMessages.RESOURCE_NOT_FOUND);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return error(HttpStatus.METHOD_NOT_ALLOWED, ErrorMessages.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        return error(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ErrorMessages.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingPart(MissingServletRequestPartException ex) {
        return error(HttpStatus.BAD_REQUEST, ErrorMessages.MISSING_PARAMETER.formatted(ex.getRequestPartName()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return error(
                HttpStatus.BAD_REQUEST,
                ErrorMessages.INVALID_PARAMETER.formatted(ex.getName())
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex) {
        return error(
                HttpStatus.BAD_REQUEST,
                ErrorMessages.MISSING_PARAMETER.formatted(ex.getParameterName())
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Database integrity constraint violation", ex);

        return error(HttpStatus.CONFLICT, ErrorMessages.DATA_INTEGRITY_VIOLATION);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleInternal(Exception ex) {
        log.error("Unhandled exception", ex);

        return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessages.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiErrorResponse> error(HttpStatus status, String message) {
        ApiErrorResponse response = ApiErrorResponse.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(status).body(response);
    }

    private ResponseEntity<ValidationErrorResponse> fieldValidationError(List<FieldError> fieldErrors) {
        List<ValidationErrorField> errors = fieldErrors.stream()
                .map(this::mapFieldError)
                .toList();

        return validationError(errors);
    }

    private ResponseEntity<ValidationErrorResponse> validationError(List<ValidationErrorField> errors) {
        ValidationErrorResponse response = ValidationErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    private ValidationErrorField mapFieldError(FieldError error) {
        return ValidationErrorField.builder()
                .field(error.getField())
                .message(error.getDefaultMessage())
                .build();
    }
}
