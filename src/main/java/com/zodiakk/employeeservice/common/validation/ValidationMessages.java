package com.zodiakk.employeeservice.common.validation;

public final class ValidationMessages {

    private ValidationMessages() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final String FIELD_REQUIRED = "Field is required";

    public static final String ID_REQUIRED = "Identifier is required";

    public static final String UUID_VALID = "Identifier must be a valid UUID";

    public static final String NAME_REQUIRED = "Name is required";
    public static final String NAME_SIZE = "Name must be between {min} and {max} characters";

    public static final String DESCRIPTION_SIZE = "Description must be less than {max} characters";

    public static final String FIRST_NAME_REQUIRED = "First name is required";
    public static final String FIRST_NAME_SIZE = "First name must be between {min} and {max} characters";

    public static final String LAST_NAME_REQUIRED = "Last name is required";
    public static final String LAST_NAME_SIZE = "Last name must be between {min} and {max} characters";

    public static final String MIDDLE_NAME_SIZE = "Middle name must be less than {max} characters";

    public static final String USER_ID_REQUIRED = "User identifier is required";

    public static final String BIRTH_DATE_PAST = "Birth date must be in the past";

    public static final String HIRE_DATE_REQUIRED = "Hire date is required";

    public static final String POSITION_REQUIRED = "Position is required";

    public static final String GRADE_REQUIRED = "Grade is required";

    public static final String EMPLOYMENT_STATUS_REQUIRED = "Employment status is required";

    public static final String MAX_SUBORDINATES_MIN = "Maximum number of subordinates cannot be negative";

    public static final String EMPLOYEE_REQUIRED = "Employee is required";

    public static final String SKILL_REQUIRED = "Skill is required";

    public static final String SKILL_LEVEL_REQUIRED = "Skill level is required";

    public static final String SKILL_LEVEL_RANGE = "Skill level must be between {min} and {max}";

    public static final String YEARS_OF_EXPERIENCE_MIN = "Years of experience cannot be negative";

    public static final String ASSESSMENT_DATE_PAST = "Assessment date cannot be in the future";

    public static final String COMPANY_REQUIRED = "Company is required";

    public static final String COMPANY_SIZE = "Company must be between {min} and {max} characters";

    public static final String EXPERIENCE_POSITION_REQUIRED = "Position is required";

    public static final String EXPERIENCE_POSITION_SIZE = "Position must be between {min} and {max} characters";

    public static final String EXPERIENCE_DESCRIPTION_SIZE = "Description must be less than {max} characters";

    public static final String START_DATE_REQUIRED = "Start date is required";

    public static final String EDUCATION_LEVEL_REQUIRED = "Education level is required";

    public static final String INSTITUTION_REQUIRED = "Institution name is required";

    public static final String INSTITUTION_SIZE = "Institution name must be between {min} and {max} characters";

    public static final String FIELD_OF_STUDY_SIZE = "Field of study must be less than {max} characters";

    public static final String DEGREE_NAME_SIZE = "Degree name must be less than {max} characters";

    public static final String MILITARY_STATUS_REQUIRED = "Military status is required";

    public static final String MILITARY_RANK_SIZE = "Military rank must be less than {max} characters";

    public static final String CATEGORY_SIZE = "Category must be less than {max} characters";

    public static final String DOCUMENT_NUMBER_SIZE = "Document number must be less than {max} characters";

    public static final String RESPONSIBLE_EMPLOYEE_REQUIRED = "Responsible employee is required";

    public static final String RESPONSIBILITY_TYPE_REQUIRED = "Responsibility type is required";

    public static final String CODE_REQUIRED = "Code is required";

    public static final String CODE_SIZE = "Code must be between {min} and {max} characters";

    public static final String MULTIPLE_ALLOWED_REQUIRED = "Multiple allowed flag is required";

    public static final String ACTIVE_REQUIRED = "Active flag is required";

    public static final String FILE_NAME_REQUIRED = "File name is required";

    public static final String FILE_NAME_SIZE = "File name must be less than {max} characters";

    public static final String CONTENT_TYPE_REQUIRED = "Content type is required";

    public static final String CONTENT_TYPE_SIZE = "Content type must be less than {max} characters";

    public static final String FILE_SIZE_REQUIRED = "File size is required";

    public static final String FILE_SIZE_MIN = "File size must be greater than zero";

    public static final String OBJECT_KEY_REQUIRED = "Object key is required";

    public static final String OBJECT_KEY_SIZE = "Object key must be less than {max} characters";
}