package com.zodiakk.employeeservice.common.validation;

public final class ValidationMessages {

    private ValidationMessages() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final String FIELD_REQUIRED = "Поле обязательно для заполнения";
    public static final String ID_REQUIRED = "Идентификатор обязателен для заполнения";
    public static final String UUID_VALID = "Идентификатор должен быть корректным UUID";

    public static final String PAGE_MIN_SIZE = "Размер страницы должен быть больше нуля";
    public static final String PAGE_MAX_SIZE = "Размер страницы не может превышать {value}";
    public static final String PAGE_NUMBER_MIN = "Номер страницы не может быть отрицательным";

    public static final String NAME_REQUIRED = "Имя обязательно для заполнения";
    public static final String NAME_SIZE = "Имя должно содержать от {min} до {max} символов";

    public static final String DESCRIPTION_SIZE = "Описание должно содержать не более {max} символов";

    public static final String FIRST_NAME_REQUIRED = "Имя обязательно для заполнения";
    public static final String FIRST_NAME_SIZE = "Имя должно содержать от {min} до {max} символов";

    public static final String LAST_NAME_REQUIRED = "Фамилия обязательна для заполнения";
    public static final String LAST_NAME_SIZE = "Фамилия должна содержать от {min} до {max} символов";

    public static final String MIDDLE_NAME_SIZE = "Отчество должно содержать не более {max} символов";

    public static final String USER_ID_REQUIRED = "Идентификатор пользователя обязателен для заполнения";

    public static final String BIRTH_DATE_PAST = "Дата рождения должна быть в прошлом";

    public static final String HIRE_DATE_REQUIRED = "Дата приёма на работу обязательна для заполнения";

    public static final String POSITION_REQUIRED = "Должность обязательна для заполнения";

    public static final String GRADE_REQUIRED = "Грейд обязателен для заполнения";

    public static final String EMPLOYMENT_STATUS_REQUIRED = "Статус занятости обязателен для заполнения";

    public static final String MAX_SUBORDINATES_MIN = "Максимальное количество подчинённых не может быть отрицательным";
    public static final String MAX_SUBORDINATES_MAX = "Максимальное количество подчинённых не может превышать {value}";

    public static final String SKILL_REQUIRED = "Навык обязателен для заполнения";
    public static final String SKILL_LEVEL_REQUIRED = "Уровень навыка обязателен для заполнения";
    public static final String SKILL_LEVEL_RANGE = "Уровень навыка должен быть от {min} до {max}";

    public static final String YEARS_OF_EXPERIENCE_MIN = "Опыт работы не может быть отрицательным";
    public static final String YEARS_OF_EXPERIENCE_MAX = "Опыт работы не может превышать {value}";

    public static final String COMPANY_REQUIRED = "Компания обязательна для заполнения";
    public static final String COMPANY_SIZE = "Название компании должно содержать от {min} до {max} символов";

    public static final String EXPERIENCE_POSITION_REQUIRED = "Должность обязательна для заполнения";
    public static final String EXPERIENCE_POSITION_SIZE = "Должность должна содержать от {min} до {max} символов";
    public static final String EXPERIENCE_DESCRIPTION_SIZE = "Описание должно содержать не более {max} символов";

    public static final String START_DATE_REQUIRED = "Дата начала обязательна для заполнения";
    public static final String START_DATE_VALID = "Дата начала должна быть не раньше сегодняшнего дня";

    public static final String EDUCATION_LEVEL_REQUIRED = "Уровень образования обязателен для заполнения";

    public static final String INSTITUTION_REQUIRED = "Название учебного заведения обязательно для заполнения";
    public static final String INSTITUTION_SIZE = "Название учебного заведения должно содержать от {min} до {max} символов";

    public static final String FIELD_OF_STUDY_SIZE = "Направление обучения должно содержать не более {max} символов";

    public static final String GRADE_LEVEL_MIN = "Уровень класса должен быть больше нуля";
    public static final String GRADE_LEVEL_MAX = "Уровень класса не может превышать {value}";

    public static final String DEGREE_NAME_SIZE = "Название степени должно содержать не более {max} символов";

    public static final String MILITARY_STATUS_REQUIRED = "Воинский статус обязателен для заполнения";
    public static final String MILITARY_RANK_SIZE = "Воинское звание должно содержать не более {max} символов";

    public static final String CATEGORY_SIZE = "Категория должна содержать не более {max} символов";

    public static final String DOCUMENT_NUMBER_SIZE = "Номер документа должен содержать не более {max} символов";

    public static final String RESPONSIBLE_EMPLOYEE_REQUIRED = "Ответственный сотрудник обязателен для заполнения";

    public static final String RESPONSIBILITY_TYPE_REQUIRED = "Тип ответственности обязателен для заполнения";

    public static final String CODE_REQUIRED = "Код обязателен для заполнения";
    public static final String CODE_SIZE = "Код должен содержать от {min} до {max} символов";

    public static final String MULTIPLE_ALLOWED_REQUIRED = "Флаг множественности обязателен для заполнения";

    public static final String ACTIVE_REQUIRED = "Флаг активности обязателен для заполнения";

    public static final String FILE_NAME_REQUIRED = "Имя файла обязательно для заполнения";
    public static final String FILE_NAME_SIZE = "Имя файла должно содержать не более {max} символов";

    public static final String CONTENT_TYPE_REQUIRED = "Тип содержимого обязателен для заполнения";
    public static final String CONTENT_TYPE_SIZE = "Тип содержимого должен содержать не более {max} символов";

    public static final String FILE_SIZE_REQUIRED = "Размер файла обязателен для заполнения";
    public static final String FILE_SIZE_MIN = "Размер файла должен быть больше нуля";

    public static final String OBJECT_KEY_REQUIRED = "Ключ объекта обязателен для заполнения";
    public static final String OBJECT_KEY_SIZE = "Ключ объекта должен содержать не более {max} символов";
}