package com.zodiakk.employeeservice.common.exception;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorMessages {

    public static final String DEPARTMENT_NOT_FOUND = "Department not found with id: %s";

    public static final String DEPARTMENT_ALREADY_EXISTS = "Department already exists with name: %s";

    public static final String DEPARTMENT_IN_USE = "Department cannot be deleted because it is referenced by employees: %s";

    public static final String POSITION_NOT_FOUND = "Position not found with id: %s";

    public static final String POSITION_ALREADY_EXISTS = "Position already exists with name: %s";

    public static final String POSITION_IN_USE = "Position cannot be deleted because it is referenced by employees: %s";

    public static final String GRADE_NOT_FOUND = "Grade not found with id: %s";

    public static final String GRADE_ALREADY_EXISTS = "Grade already exists";

    public static final String GRADE_IN_USE = "Grade cannot be deleted because it is referenced by employees: %s";

    public static final String SPECIALIZATION_NOT_FOUND = "Specialization not found with id: %s";

    public static final String SPECIALIZATION_ALREADY_EXISTS = "Specialization already exists with name: %s";

    public static final String SPECIALIZATION_IN_USE = "Specialization cannot be deleted because it is referenced by employees: %s";

    public static final String SKILL_NOT_FOUND = "Skill not found with id: %s";

    public static final String SKILL_ALREADY_EXISTS = "Skill already exists with name: %s";

    public static final String SKILL_IN_USE = "Skill cannot be deleted because it is referenced by employees: %s";

    public static final String SKILL_INACTIVE = "Skill is inactive and cannot be assigned: %s";

    public static final String RESPONSIBILITY_TYPE_NOT_FOUND = "Responsibility type not found with id: %s";

    public static final String RESPONSIBILITY_TYPE_ALREADY_EXISTS = "Responsibility type already exists with code: %s";

    public static final String RESPONSIBILITY_TYPE_IN_USE = "Responsibility type cannot be deleted because it is referenced by employee responsibilities: %s";

    public static final String RESPONSIBILITY_TYPE_INACTIVE = "Responsibility type is inactive and cannot be assigned: %s";

    public static final String EMPLOYEE_NOT_FOUND = "Employee not found with id: %s";

    public static final String EMPLOYEE_NOT_FOUND_BY_USER_ID = "Employee not found with user id: %s";

    public static final String EMPLOYEE_ALREADY_EXISTS = "Employee already exists with user id: %s";

    public static final String EMPLOYEE_TERMINATED = "Operation is not allowed for terminated employee: %s";

    public static final String EMPLOYEE_NOT_TERMINATED = "Employee must be terminated before deletion: %s";

    public static final String EMPLOYEE_CANNOT_BE_CREATED_TERMINATED = "Employee cannot be created with TERMINATED status";

    public static final String INVALID_EMPLOYEE_DATES = "Birth date must be before hire date, and hire date must not be in the future";

    public static final String CATALOG_INACTIVE = "%s is inactive and cannot be assigned: %s";

    public static final String EDUCATION_NOT_FOUND = "Education not found with id: %s";

    public static final String EXPERIENCE_NOT_FOUND = "Experience not found with id: %s";

    public static final String INVALID_DATE_RANGE = "End date must be greater than or equal to start date";

    public static final String EMPLOYEE_SKILL_NOT_FOUND = "Employee skill not found with id: %s";

    public static final String EMPLOYEE_SKILL_ALREADY_EXISTS = "Employee already has skill with id: %s";

    public static final String EMPLOYEE_RESPONSIBILITY_NOT_FOUND = "Employee responsibility not found with id: %s";

    public static final String SELF_RESPONSIBILITY = "Employee cannot be responsible for themselves: %s";

    public static final String RESPONSIBILITY_ALREADY_ACTIVE = "Active responsibility already exists for this assignment";

    public static final String MULTIPLE_NOT_ALLOWED = "Multiple active responsibilities of type '%s' are not allowed for responsible employee: %s";

    public static final String MAX_SUBORDINATES_EXCEEDED = "Responsible employee has reached max subordinates limit: %s";

    public static final String MILITARY_OBLIGATION_NOT_FOUND = "Military obligation not found for employee: %s";

    public static final String MILITARY_OBLIGATION_ALREADY_EXISTS = "Military obligation already exists for employee: %s";

    public static final String PROFILE_PHOTO_NOT_FOUND = "Profile photo not found for employee: %s";

    public static final String PROFILE_PHOTO_ALREADY_EXISTS = "Profile photo already exists for employee: %s";

    public static final String INVALID_CONTENT_TYPE = "Unsupported content type: %s. Allowed: image/jpeg, image/png, image/webp";

    public static final String FILE_TOO_LARGE = "File size exceeds maximum allowed size of %s bytes";

    public static final String S3_UPLOAD_FAILED = "Failed to upload file to object storage";

    public static final String S3_DELETE_FAILED = "Failed to delete file from object storage";

    public static final String RESOURCE_DOES_NOT_BELONG_TO_EMPLOYEE = "Resource does not belong to employee: %s";

    public static final String INTERNAL_SERVER_ERROR = "An unexpected error occurred. Please try again later.";

    public static final String ACCESS_DENIED = "Access denied";

    public static final String UNAUTHORIZED = "Unauthorized";
}
