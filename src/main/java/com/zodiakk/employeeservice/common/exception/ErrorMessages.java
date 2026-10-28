package com.zodiakk.employeeservice.common.exception;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorMessages {

    public static final String DEPARTMENT_NOT_FOUND = "Отдел не найден с идентификатором: %s";

    public static final String DEPARTMENT_ALREADY_EXISTS = "Отдел уже существует с названием: %s";

    public static final String DEPARTMENT_IN_USE = "Отдел нельзя удалить, так как он используется сотрудниками: %s";

    public static final String POSITION_NOT_FOUND = "Должность не найдена с идентификатором: %s";

    public static final String POSITION_ALREADY_EXISTS = "Должность уже существует с названием: %s";

    public static final String POSITION_IN_USE = "Должность нельзя удалить, так как она используется сотрудниками: %s";

    public static final String GRADE_NOT_FOUND = "Грейд не найден с идентификатором: %s";

    public static final String GRADE_ALREADY_EXISTS = "Грейд уже существует";

    public static final String GRADE_IN_USE = "Грейд нельзя удалить, так как он используется сотрудниками: %s";

    public static final String SPECIALIZATION_NOT_FOUND = "Специализация не найдена с идентификатором: %s";

    public static final String SPECIALIZATION_ALREADY_EXISTS = "Специализация уже существует с названием: %s";

    public static final String SPECIALIZATION_IN_USE = "Специализацию нельзя удалить, так как она используется сотрудниками: %s";

    public static final String SKILL_NOT_FOUND = "Навык не найден с идентификатором: %s";

    public static final String SKILL_ALREADY_EXISTS = "Навык уже существует с названием: %s";

    public static final String SKILL_IN_USE = "Навык нельзя удалить, так как он используется сотрудниками: %s";

    public static final String RESPONSIBILITY_TYPE_NOT_FOUND = "Тип ответственности не найден с идентификатором: %s";

    public static final String RESPONSIBILITY_TYPE_ALREADY_EXISTS = "Тип ответственности уже существует с кодом: %s";

    public static final String RESPONSIBILITY_TYPE_IN_USE = "Тип ответственности нельзя удалить, так как он используется в обязанностях сотрудников: %s";

    public static final String EMPLOYEE_NOT_FOUND = "Сотрудник не найден с идентификатором: %s";

    public static final String EMPLOYEE_NOT_FOUND_BY_USER_ID = "Сотрудник не найден с идентификатором пользователя: %s";

    public static final String EMPLOYEE_ALREADY_EXISTS = "Сотрудник уже существует с идентификатором пользователя: %s";

    public static final String EMPLOYEE_ALREADY_EXISTS_BY_EMAIL = "Сотрудник уже существует с корпоративной почтой: %s";

    public static final String EMPLOYEE_TERMINATED = "Операция недоступна для уволенного сотрудника: %s";

    public static final String EMPLOYEE_NOT_TERMINATED = "Сотрудник должен быть уволен перед удалением: %s";

    public static final String EMPLOYEE_CANNOT_BE_CREATED_TERMINATED = "Нельзя создать сотрудника со статусом TERMINATED";

    public static final String INVALID_EMPLOYEE_DATES = "Дата рождения должна быть раньше даты приёма на работу, а дата приёма не может быть в будущем";

    public static final String CATALOG_INACTIVE = "Неактивный справочник %s нельзя назначить: %s";

    public static final String EDUCATION_NOT_FOUND = "Образование не найдено с идентификатором: %s";

    public static final String EXPERIENCE_NOT_FOUND = "Опыт работы не найден с идентификатором: %s";

    public static final String INVALID_DATE_RANGE = "Дата окончания должна быть не раньше даты начала";

    public static final String EMPLOYEE_SKILL_NOT_FOUND = "Навык сотрудника не найден с идентификатором: %s";

    public static final String EMPLOYEE_SKILL_ALREADY_EXISTS = "У сотрудника уже есть навык с идентификатором: %s";

    public static final String EMPLOYEE_RESPONSIBILITY_NOT_FOUND = "Обязанность сотрудника не найдена с идентификатором: %s";

    public static final String SELF_RESPONSIBILITY = "Сотрудник не может быть ответственным за самого себя: %s";

    public static final String RESPONSIBILITY_ALREADY_ACTIVE = "Для этого назначения уже существует активная обязанность";

    public static final String MULTIPLE_NOT_ALLOWED = "Несколько активных обязанностей типа '%s' недопустимы для ответственного сотрудника: %s";

    public static final String MAX_SUBORDINATES_EXCEEDED = "Ответственный сотрудник достиг максимального количества подчинённых: %s";

    public static final String MILITARY_OBLIGATION_NOT_FOUND = "Воинская обязанность не найдена для сотрудника: %s";

    public static final String MILITARY_OBLIGATION_ALREADY_EXISTS = "Воинская обязанность уже существует для сотрудника: %s";

    public static final String PROFILE_PHOTO_NOT_FOUND = "Фотография профиля не найдена для сотрудника: %s";

    public static final String EMPTY_FILE = "Файл не передан или пуст";

    public static final String INVALID_CONTENT_TYPE = "Неподдерживаемый тип содержимого: %s. Допустимые: %s";

    public static final String FILE_TOO_LARGE = "Размер файла превышает максимально допустимый размер %s байт";

    public static final String UPLOAD_SIZE_EXCEEDED = "Размер загружаемого файла превышает допустимый";

    public static final String OBJECT_STORAGE_UPLOAD_FAILED = "Не удалось загрузить файл в объектное хранилище";

    public static final String OBJECT_STORAGE_DELETE_FAILED = "Не удалось удалить файл из объектного хранилища";

    public static final String RESOURCE_DOES_NOT_BELONG_TO_EMPLOYEE = "Ресурс не принадлежит сотруднику: %s";

    public static final String ACCOUNT_ALREADY_LINKED = "Сотрудник %s уже привязан к пользователю %s; запрошенный пользователь %s";

    public static final String UNSUPPORTED_EVENT_VERSION = "Неподдерживаемая версия события: %s";

    public static final String UNSUPPORTED_EVENT_TYPE = "Неподдерживаемый тип события: %s";

    public static final String OUTBOX_EVENT_SERIALIZATION_FAILED = "Не удалось сериализовать outbox-событие: %s";

    public static final String INTERNAL_SERVER_ERROR = "Произошла непредвиденная ошибка. Попробуйте позже.";

    public static final String DATA_INTEGRITY_VIOLATION = "Нарушение целостности данных";

    public static final String INVALID_REQUEST_BODY = "Некорректное тело запроса";

    public static final String RESOURCE_NOT_FOUND = "Запрошенный ресурс не найден";

    public static final String METHOD_NOT_ALLOWED = "Метод запроса не поддерживается";

    public static final String UNSUPPORTED_MEDIA_TYPE = "Неподдерживаемый тип содержимого запроса";

    public static final String INVALID_PARAMETER = "Некорректное значение параметра: %s";

    public static final String MISSING_PARAMETER = "Обязательный параметр отсутствует: %s";

    public static final String ACCESS_DENIED = "Доступ запрещён";

    public static final String UNAUTHORIZED = "Пользователь не авторизован";

    public static final String INVALID_INTERNAL_API_KEY = "Недействительный внутренний API-ключ";
}