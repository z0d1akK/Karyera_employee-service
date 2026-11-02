package com.zodiakk.employeeservice.common.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KafkaEventConstants {

    public static final String AGGREGATE_TYPE_EMPLOYEE_ACCOUNT = "EMPLOYEE_ACCOUNT";

    public static final String AGGREGATE_TYPE_EMPLOYEE = "EMPLOYEE";

    public static final String USER_CREATED = "USER_CREATED";

    public static final String USER_DELETED = "USER_DELETED";

    public static final String EMPLOYEE_ACCOUNT_UNLINKED = "EMPLOYEE_ACCOUNT_UNLINKED";

    public static final String EMPLOYEE_SKILL_UPDATED = "EmployeeSkillUpdated";

    public static final String EMPLOYEE_POSITION_CHANGED = "EmployeePositionChanged";

    public static final int EVENT_VERSION_V1 = 1;

    public static final String DLT_SUFFIX = ".DLT";

    public static final String SKILL_CHANGE_ADDED = "ADDED";

    public static final String SKILL_CHANGE_UPDATED = "UPDATED";

    public static final String SKILL_CHANGE_REMOVED = "REMOVED";
}