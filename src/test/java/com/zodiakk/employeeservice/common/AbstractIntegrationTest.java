package com.zodiakk.employeeservice.common;

import com.zodiakk.employeeservice.TestcontainersConfiguration;
import com.zodiakk.employeeservice.common.cache.CacheNames;
import com.zodiakk.employeeservice.common.constants.Headers;
import com.zodiakk.employeeservice.common.storage.ObjectStorageService;
import com.zodiakk.employeeservice.common.util.EmailUtils;
import com.zodiakk.employeeservice.employee.entity.Department;
import com.zodiakk.employeeservice.employee.entity.Employee;
import com.zodiakk.employeeservice.employee.entity.Grade;
import com.zodiakk.employeeservice.employee.entity.Position;
import com.zodiakk.employeeservice.employee.entity.ResponsibilityType;
import com.zodiakk.employeeservice.employee.entity.Skill;
import com.zodiakk.employeeservice.employee.entity.Specialization;
import com.zodiakk.employeeservice.employee.entity.enums.EmploymentStatus;
import com.zodiakk.employeeservice.employee.repository.DepartmentRepository;
import com.zodiakk.employeeservice.employee.repository.EducationRepository;
import com.zodiakk.employeeservice.employee.repository.EmployeeProfilePhotoRepository;
import com.zodiakk.employeeservice.employee.repository.EmployeeRepository;
import com.zodiakk.employeeservice.employee.repository.EmployeeResponsibilityRepository;
import com.zodiakk.employeeservice.employee.repository.EmployeeSkillRepository;
import com.zodiakk.employeeservice.employee.repository.ExperienceRepository;
import com.zodiakk.employeeservice.employee.repository.GradeRepository;
import com.zodiakk.employeeservice.employee.repository.MilitaryObligationRepository;
import com.zodiakk.employeeservice.employee.repository.PositionRepository;
import com.zodiakk.employeeservice.employee.repository.ResponsibilityTypeRepository;
import com.zodiakk.employeeservice.employee.repository.SkillRepository;
import com.zodiakk.employeeservice.employee.repository.SpecializationRepository;
import com.zodiakk.employeeservice.integration.auth.repository.EmployeeAccountUnlinkRepository;
import com.zodiakk.employeeservice.integration.auth.repository.ProcessedEventRepository;
import com.zodiakk.employeeservice.outbox.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.AbstractMockHttpServletRequestBuilder;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    protected static final String USER_ID_HEADER = Headers.USER_ID;

    protected static final String USER_ROLE_HEADER = Headers.USER_ROLE;

    protected static final String USER_EMAIL_HEADER = Headers.USER_EMAIL;

    protected static final String INTERNAL_KEY_HEADER = Headers.INTERNAL_KEY;

    protected static final String INTERNAL_API_KEY = "test-internal-api-key";

    private static final AtomicInteger GRADE_LEVEL_SEQUENCE = new AtomicInteger(1_000);

    @MockitoBean
    protected ObjectStorageService objectStorageService;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected CacheManager cacheManager;

    @Autowired
    protected EmployeeRepository employeeRepository;

    @Autowired
    protected EmployeeProfilePhotoRepository employeeProfilePhotoRepository;

    @Autowired
    protected EmployeeResponsibilityRepository employeeResponsibilityRepository;

    @Autowired
    protected EmployeeSkillRepository employeeSkillRepository;

    @Autowired
    protected EducationRepository educationRepository;

    @Autowired
    protected ExperienceRepository experienceRepository;

    @Autowired
    protected MilitaryObligationRepository militaryObligationRepository;

    @Autowired
    protected DepartmentRepository departmentRepository;

    @Autowired
    protected PositionRepository positionRepository;

    @Autowired
    protected SpecializationRepository specializationRepository;

    @Autowired
    protected GradeRepository gradeRepository;

    @Autowired
    protected SkillRepository skillRepository;

    @Autowired
    protected ResponsibilityTypeRepository responsibilityTypeRepository;

    @Autowired
    protected OutboxEventRepository outboxEventRepository;

    @Autowired
    protected ProcessedEventRepository processedEventRepository;

    @Autowired
    protected EmployeeAccountUnlinkRepository employeeAccountUnlinkRepository;

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.kafka.bootstrap-servers",
                () -> TestcontainersConfiguration.kafka().getBootstrapServers()
        );
    }

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("""
                TRUNCATE TABLE employee_responsibilities, employee_profile_photos, military_obligations,
                    educations, experiences, employee_skills, employees, responsibility_types, skills,
                    grades, specializations, positions, departments, processed_events,
                    employee_account_unlinks, outbox_events CASCADE
                """);

        List.of(
                        CacheNames.EMPLOYEES,
                        CacheNames.POSITIONS,
                        CacheNames.GRADES,
                        CacheNames.DEPARTMENTS,
                        CacheNames.SPECIALIZATIONS,
                        CacheNames.SKILLS,
                        CacheNames.RESPONSIBILITY_TYPES
                )
                .forEach(name -> Objects.requireNonNull(cacheManager.getCache(name)).clear());
    }

    protected String toJson(Object value) {
        return objectMapper.writeValueAsString(value);
    }

    protected <B extends AbstractMockHttpServletRequestBuilder<B>> B asUser(
            B builder,
            UUID userId,
            String role
    ) {
        return builder
                .header(USER_ID_HEADER, userId.toString())
                .header(USER_ROLE_HEADER, role)
                .header(USER_EMAIL_HEADER, "user-" + userId + "@company.com");
    }

    protected <B extends AbstractMockHttpServletRequestBuilder<B>> B asHr(B builder) {
        return asUser(builder, UUID.randomUUID(), "HR");
    }

    protected <B extends AbstractMockHttpServletRequestBuilder<B>> B asAdmin(B builder) {
        return asUser(builder, UUID.randomUUID(), "ADMIN");
    }

    protected <B extends AbstractMockHttpServletRequestBuilder<B>> B asEmployee(B builder, UUID userId) {
        return asUser(builder, userId, "EMPLOYEE");
    }

    protected <B extends AbstractMockHttpServletRequestBuilder<B>> B asManager(B builder, UUID userId) {
        return asUser(builder, userId, "MANAGER");
    }

    protected <B extends AbstractMockHttpServletRequestBuilder<B>> B asInternal(B builder) {
        return builder.header(INTERNAL_KEY_HEADER, INTERNAL_API_KEY);
    }

    protected Department persistDepartment(String name) {
        return persistDepartment(name, true);
    }

    protected Department persistDepartment(String name, boolean active) {
        return departmentRepository.saveAndFlush(Department.builder()
                .name(name)
                .description("Department " + name)
                .active(active)
                .build());
    }

    protected Position persistPosition(String name) {
        return persistPosition(name, true);
    }

    protected Position persistPosition(String name, boolean active) {
        return positionRepository.saveAndFlush(Position.builder()
                .name(name)
                .description("Position " + name)
                .active(active)
                .build());
    }

    protected Specialization persistSpecialization(String name) {
        return persistSpecialization(name, true);
    }

    protected Specialization persistSpecialization(String name, boolean active) {
        return specializationRepository.saveAndFlush(Specialization.builder()
                .name(name)
                .description("Specialization " + name)
                .active(active)
                .build());
    }

    protected int nextGradeLevel() {
        return GRADE_LEVEL_SEQUENCE.incrementAndGet();
    }

    protected Grade persistGrade(String name, int level) {
        return persistGrade(name, level, true);
    }

    protected Grade persistGrade(String name, int level, boolean active) {
        return gradeRepository.saveAndFlush(Grade.builder()
                .name(name)
                .level(level)
                .description("Grade " + name)
                .active(active)
                .build());
    }

    protected Skill persistSkill(String name) {
        return persistSkill(name, true);
    }

    protected Skill persistSkill(String name, boolean active) {
        return skillRepository.saveAndFlush(Skill.builder()
                .name(name)
                .category("Backend")
                .description("Skill " + name)
                .active(active)
                .build());
    }

    protected ResponsibilityType persistResponsibilityType(String code) {
        return persistResponsibilityType(code, false, true);
    }

    protected ResponsibilityType persistResponsibilityType(
            String code,
            boolean multipleAllowed,
            boolean active
    ) {
        return responsibilityTypeRepository.saveAndFlush(ResponsibilityType.builder()
                .code(code)
                .name("Responsibility " + code)
                .description("Responsibility type " + code)
                .multipleAllowed(multipleAllowed)
                .active(active)
                .build());
    }

    protected Employee persistEmployee(Position position, Grade grade) {
        return persistEmployee(UUID.randomUUID(), position, grade, EmploymentStatus.ACTIVE);
    }

    protected Employee persistUnlinkedEmployee(Position position, Grade grade) {
        return persistEmployee(null, position, grade, EmploymentStatus.ACTIVE);
    }

    protected Employee persistEmployee(
            UUID userId,
            Position position,
            Grade grade,
            EmploymentStatus status
    ) {
        return persistEmployee(
                userId,
                "employee-" + UUID.randomUUID() + "@company.com",
                position,
                grade,
                status,
                LocalDate.now().minusYears(1)
        );
    }

    protected Employee persistEmployee(
            UUID userId,
            String email,
            Position position,
            Grade grade,
            EmploymentStatus status,
            LocalDate hireDate
    ) {
        return employeeRepository.saveAndFlush(Employee.builder()
                .userId(userId)
                .firstName("Ivan")
                .lastName("Petrov")
                .middleName("Ivanovich")
                .email(EmailUtils.normalize(email))
                .birthDate(LocalDate.of(1990, 1, 1))
                .hireDate(hireDate)
                .position(position)
                .grade(grade)
                .employmentStatus(status)
                .build());
    }
}
