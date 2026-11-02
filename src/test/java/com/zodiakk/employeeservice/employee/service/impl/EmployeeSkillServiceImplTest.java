package com.zodiakk.employeeservice.employee.service.impl;

import com.zodiakk.employeeservice.common.constants.KafkaEventConstants;
import com.zodiakk.employeeservice.employee.dto.request.create.EmployeeSkillCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.EmployeeSkillUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.EmployeeSkillResponseDto;
import com.zodiakk.employeeservice.employee.dto.response.SkillResponseDto;
import com.zodiakk.employeeservice.employee.entity.Employee;
import com.zodiakk.employeeservice.employee.entity.EmployeeSkill;
import com.zodiakk.employeeservice.employee.entity.Skill;
import com.zodiakk.employeeservice.employee.exception.catalog.CatalogInactiveException;
import com.zodiakk.employeeservice.employee.exception.common.ResourceDoesNotBelongToEmployeeException;
import com.zodiakk.employeeservice.employee.exception.employee.EmployeeNotFoundException;
import com.zodiakk.employeeservice.employee.exception.employeeskill.EmployeeSkillAlreadyExistsException;
import com.zodiakk.employeeservice.employee.exception.employeeskill.EmployeeSkillNotFoundException;
import com.zodiakk.employeeservice.employee.exception.skill.SkillNotFoundException;
import com.zodiakk.employeeservice.employee.mapper.EmployeeSkillMapper;
import com.zodiakk.employeeservice.employee.repository.EmployeeRepository;
import com.zodiakk.employeeservice.employee.repository.EmployeeSkillRepository;
import com.zodiakk.employeeservice.employee.repository.SkillRepository;
import com.zodiakk.employeeservice.integration.employee.service.EmployeeEventService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeSkillServiceImplTest {

    private static final UUID EMPLOYEE_ID = UUID.fromString("77777777-7777-7777-7777-777777777771");

    private static final UUID OTHER_EMPLOYEE_ID = UUID.fromString("77777777-7777-7777-7777-777777777772");

    private static final UUID SKILL_ID = UUID.fromString("88888888-8888-8888-8888-888888888881");

    private static final UUID OTHER_SKILL_ID = UUID.fromString("88888888-8888-8888-8888-888888888882");

    private static final UUID EMPLOYEE_SKILL_ID = UUID.fromString("99999999-9999-9999-9999-999999999991");

    @Mock
    private EmployeeSkillRepository employeeSkillRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private EmployeeSkillMapper employeeSkillMapper;

    @Mock
    private EmployeeEventService employeeEventService;

    @InjectMocks
    private EmployeeSkillServiceImpl employeeSkillService;

    @Nested
    @DisplayName("add")
    class Add {

        @Test
        @DisplayName("adds an active skill to the employee and stamps the assessment date")
        void shouldAddSkill() {
            EmployeeSkillCreateRequestDto request = EmployeeSkillCreateRequestDto.builder()
                    .skillId(SKILL_ID)
                    .level(3)
                    .yearsOfExperience(new BigDecimal("2.5"))
                    .build();
            Employee employee = employee(EMPLOYEE_ID);
            Skill skill = skill(SKILL_ID, true);
            EmployeeSkill entity = EmployeeSkill.builder().level(3).build();
            EmployeeSkillResponseDto expected = response();

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
            when(skillRepository.findById(SKILL_ID)).thenReturn(Optional.of(skill));
            when(employeeSkillRepository.existsByEmployeeIdAndSkillId(EMPLOYEE_ID, SKILL_ID))
                    .thenReturn(false);
            when(employeeSkillMapper.toEntity(request)).thenReturn(entity);
            when(employeeSkillRepository.save(entity)).thenReturn(entity);
            when(employeeSkillMapper.toResponseDto(entity)).thenReturn(expected);

            EmployeeSkillResponseDto result = employeeSkillService.add(EMPLOYEE_ID, request);

            assertThat(result).isSameAs(expected);
            assertThat(entity.getEmployee()).isSameAs(employee);
            assertThat(entity.getSkill()).isSameAs(skill);
            assertThat(entity.getLastAssessedAt()).isEqualTo(LocalDate.now());
            verify(employeeSkillMapper).toEntity(request);
            verify(employeeSkillRepository).save(entity);
            verify(employeeEventService).recordSkillUpdated(
                    eq(EMPLOYEE_ID),
                    eq(SKILL_ID),
                    any(),
                    any(),
                    any(),
                    eq(KafkaEventConstants.SKILL_CHANGE_ADDED)
            );
        }

        @Test
        @DisplayName("throws when the employee does not exist")
        void shouldThrowWhenEmployeeMissing() {
            EmployeeSkillCreateRequestDto request = EmployeeSkillCreateRequestDto.builder()
                    .skillId(SKILL_ID)
                    .level(3)
                    .build();

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeSkillService.add(EMPLOYEE_ID, request))
                    .isInstanceOf(EmployeeNotFoundException.class)
                    .hasMessageContaining(EMPLOYEE_ID.toString());

            verifyNoInteractions(skillRepository, employeeSkillRepository, employeeSkillMapper);
        }

        @Test
        @DisplayName("throws when the skill does not exist")
        void shouldThrowWhenSkillMissing() {
            EmployeeSkillCreateRequestDto request = EmployeeSkillCreateRequestDto.builder()
                    .skillId(SKILL_ID)
                    .level(3)
                    .build();

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee(EMPLOYEE_ID)));
            when(skillRepository.findById(SKILL_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeSkillService.add(EMPLOYEE_ID, request))
                    .isInstanceOf(SkillNotFoundException.class)
                    .hasMessageContaining(SKILL_ID.toString());

            verifyNoInteractions(employeeSkillRepository, employeeSkillMapper);
        }

        @Test
        @DisplayName("throws when the skill is inactive")
        void shouldThrowWhenSkillInactive() {
            EmployeeSkillCreateRequestDto request = EmployeeSkillCreateRequestDto.builder()
                    .skillId(SKILL_ID)
                    .level(3)
                    .build();

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee(EMPLOYEE_ID)));
            when(skillRepository.findById(SKILL_ID)).thenReturn(Optional.of(skill(SKILL_ID, false)));

            assertThatThrownBy(() -> employeeSkillService.add(EMPLOYEE_ID, request))
                    .isInstanceOf(CatalogInactiveException.class);

            verifyNoInteractions(employeeSkillRepository, employeeSkillMapper);
        }

        @Test
        @DisplayName("rejects a skill that the employee already has")
        void shouldRejectDuplicateSkill() {
            EmployeeSkillCreateRequestDto request = EmployeeSkillCreateRequestDto.builder()
                    .skillId(SKILL_ID)
                    .level(3)
                    .build();

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee(EMPLOYEE_ID)));
            when(skillRepository.findById(SKILL_ID)).thenReturn(Optional.of(skill(SKILL_ID, true)));
            when(employeeSkillRepository.existsByEmployeeIdAndSkillId(EMPLOYEE_ID, SKILL_ID))
                    .thenReturn(true);

            assertThatThrownBy(() -> employeeSkillService.add(EMPLOYEE_ID, request))
                    .isInstanceOf(EmployeeSkillAlreadyExistsException.class)
                    .hasMessageContaining(SKILL_ID.toString());

            verifyNoInteractions(employeeSkillMapper);
            verify(employeeSkillRepository, never()).save(any(EmployeeSkill.class));
        }
    }

    @Nested
    @DisplayName("listByEmployee")
    class ListByEmployee {

        @Test
        @DisplayName("returns the mapped skills of the employee")
        void shouldListSkills() {
            EmployeeSkill first = employeeSkill(EMPLOYEE_SKILL_ID, EMPLOYEE_ID, SKILL_ID);
            EmployeeSkill second = employeeSkill(
                    UUID.fromString("99999999-9999-9999-9999-999999999992"),
                    EMPLOYEE_ID,
                    OTHER_SKILL_ID
            );
            EmployeeSkillResponseDto firstDto = response();
            EmployeeSkillResponseDto secondDto = response();

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee(EMPLOYEE_ID)));
            when(employeeSkillRepository.findAllByEmployeeId(EMPLOYEE_ID))
                    .thenReturn(List.of(first, second));
            when(employeeSkillMapper.toResponseDto(first)).thenReturn(firstDto);
            when(employeeSkillMapper.toResponseDto(second)).thenReturn(secondDto);

            List<EmployeeSkillResponseDto> result = employeeSkillService.listByEmployee(EMPLOYEE_ID);

            assertThat(result).containsExactly(firstDto, secondDto);
            verify(employeeSkillRepository).findAllByEmployeeId(EMPLOYEE_ID);
        }

        @Test
        @DisplayName("returns an empty list when the employee has no skills")
        void shouldReturnEmptyList() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee(EMPLOYEE_ID)));
            when(employeeSkillRepository.findAllByEmployeeId(EMPLOYEE_ID)).thenReturn(List.of());

            assertThat(employeeSkillService.listByEmployee(EMPLOYEE_ID)).isEmpty();
            verifyNoInteractions(employeeSkillMapper);
        }

        @Test
        @DisplayName("throws when the employee does not exist")
        void shouldThrowWhenEmployeeMissing() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeSkillService.listByEmployee(EMPLOYEE_ID))
                    .isInstanceOf(EmployeeNotFoundException.class);

            verifyNoInteractions(employeeSkillRepository, employeeSkillMapper);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("updates level and experience and re-stamps the assessment date")
        void shouldUpdateAndRestampWhenLevelChanges() {
            LocalDate previousAssessment = LocalDate.now().minusMonths(3);
            EmployeeSkill entity = employeeSkill(EMPLOYEE_SKILL_ID, EMPLOYEE_ID, SKILL_ID);
            entity.setLevel(2);
            entity.setLastAssessedAt(previousAssessment);

            EmployeeSkillUpdateRequestDto request = EmployeeSkillUpdateRequestDto.builder()
                    .level(4)
                    .yearsOfExperience(new BigDecimal("3.0"))
                    .build();
            EmployeeSkillResponseDto expected = response();

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee(EMPLOYEE_ID)));
            when(employeeSkillRepository.findById(EMPLOYEE_SKILL_ID)).thenReturn(Optional.of(entity));
            when(employeeSkillRepository.save(entity)).thenReturn(entity);
            when(employeeSkillMapper.toResponseDto(entity)).thenReturn(expected);

            EmployeeSkillResponseDto result =
                    employeeSkillService.update(EMPLOYEE_ID, EMPLOYEE_SKILL_ID, request);

            assertThat(result).isSameAs(expected);
            assertThat(entity.getLastAssessedAt())
                    .isEqualTo(LocalDate.now())
                    .isAfter(previousAssessment);
            verify(employeeSkillMapper).updateEntity(request, entity);
            verify(employeeSkillRepository).save(entity);
            verify(employeeEventService).recordSkillUpdated(
                    eq(EMPLOYEE_ID),
                    eq(SKILL_ID),
                    any(),
                    any(),
                    any(),
                    eq(KafkaEventConstants.SKILL_CHANGE_UPDATED)
            );
        }

        @Test
        @DisplayName("keeps the assessment date when the level does not change")
        void shouldKeepAssessmentDateWhenLevelUnchanged() {
            LocalDate previousAssessment = LocalDate.now().minusMonths(3);
            EmployeeSkill entity = employeeSkill(EMPLOYEE_SKILL_ID, EMPLOYEE_ID, SKILL_ID);
            entity.setLevel(2);
            entity.setLastAssessedAt(previousAssessment);

            EmployeeSkillUpdateRequestDto request = EmployeeSkillUpdateRequestDto.builder()
                    .level(2)
                    .yearsOfExperience(new BigDecimal("4.0"))
                    .build();

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee(EMPLOYEE_ID)));
            when(employeeSkillRepository.findById(EMPLOYEE_SKILL_ID)).thenReturn(Optional.of(entity));
            when(employeeSkillRepository.save(entity)).thenReturn(entity);
            when(employeeSkillMapper.toResponseDto(entity)).thenReturn(response());

            employeeSkillService.update(EMPLOYEE_ID, EMPLOYEE_SKILL_ID, request);

            assertThat(entity.getLastAssessedAt()).isEqualTo(previousAssessment);
            verify(employeeSkillMapper).updateEntity(request, entity);
        }

        @Test
        @DisplayName("keeps the assessment date when the level is not provided")
        void shouldKeepAssessmentDateWhenLevelIsNull() {
            LocalDate previousAssessment = LocalDate.now().minusMonths(3);
            EmployeeSkill entity = employeeSkill(EMPLOYEE_SKILL_ID, EMPLOYEE_ID, SKILL_ID);
            entity.setLastAssessedAt(previousAssessment);

            EmployeeSkillUpdateRequestDto request = EmployeeSkillUpdateRequestDto.builder()
                    .yearsOfExperience(new BigDecimal("4.0"))
                    .build();

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee(EMPLOYEE_ID)));
            when(employeeSkillRepository.findById(EMPLOYEE_SKILL_ID)).thenReturn(Optional.of(entity));
            when(employeeSkillRepository.save(entity)).thenReturn(entity);
            when(employeeSkillMapper.toResponseDto(entity)).thenReturn(response());

            employeeSkillService.update(EMPLOYEE_ID, EMPLOYEE_SKILL_ID, request);

            assertThat(entity.getLastAssessedAt()).isEqualTo(previousAssessment);
        }

        @Test
        @DisplayName("throws when the employee does not exist")
        void shouldThrowWhenEmployeeMissing() {
            EmployeeSkillUpdateRequestDto request = EmployeeSkillUpdateRequestDto.builder()
                    .level(4)
                    .build();

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeSkillService.update(EMPLOYEE_ID, EMPLOYEE_SKILL_ID, request))
                    .isInstanceOf(EmployeeNotFoundException.class);

            verifyNoInteractions(employeeSkillRepository, employeeSkillMapper);
        }

        @Test
        @DisplayName("throws when the employee skill does not exist")
        void shouldThrowWhenEmployeeSkillMissing() {
            EmployeeSkillUpdateRequestDto request = EmployeeSkillUpdateRequestDto.builder()
                    .level(4)
                    .build();

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee(EMPLOYEE_ID)));
            when(employeeSkillRepository.findById(EMPLOYEE_SKILL_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeSkillService.update(EMPLOYEE_ID, EMPLOYEE_SKILL_ID, request))
                    .isInstanceOf(EmployeeSkillNotFoundException.class)
                    .hasMessageContaining(EMPLOYEE_SKILL_ID.toString());

            verifyNoInteractions(employeeSkillMapper);
            verify(employeeSkillRepository, never()).save(any(EmployeeSkill.class));
        }

        @Test
        @DisplayName("throws when the employee skill belongs to another employee")
        void shouldThrowWhenNotOwned() {
            EmployeeSkill entity = employeeSkill(EMPLOYEE_SKILL_ID, OTHER_EMPLOYEE_ID, SKILL_ID);
            EmployeeSkillUpdateRequestDto request = EmployeeSkillUpdateRequestDto.builder()
                    .level(4)
                    .build();

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee(EMPLOYEE_ID)));
            when(employeeSkillRepository.findById(EMPLOYEE_SKILL_ID)).thenReturn(Optional.of(entity));

            assertThatThrownBy(() -> employeeSkillService.update(EMPLOYEE_ID, EMPLOYEE_SKILL_ID, request))
                    .isInstanceOf(ResourceDoesNotBelongToEmployeeException.class)
                    .hasMessageContaining(EMPLOYEE_ID.toString());

            verifyNoInteractions(employeeSkillMapper);
            verify(employeeSkillRepository, never()).save(any(EmployeeSkill.class));
        }
    }

    @Nested
    @DisplayName("remove")
    class Remove {

        @Test
        @DisplayName("removes an owned employee skill")
        void shouldRemoveSkill() {
            EmployeeSkill entity = employeeSkill(EMPLOYEE_SKILL_ID, EMPLOYEE_ID, SKILL_ID);

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee(EMPLOYEE_ID)));
            when(employeeSkillRepository.findById(EMPLOYEE_SKILL_ID)).thenReturn(Optional.of(entity));

            employeeSkillService.remove(EMPLOYEE_ID, EMPLOYEE_SKILL_ID);

            verify(employeeSkillRepository).delete(entity);
            verify(employeeEventService).recordSkillUpdated(
                    eq(EMPLOYEE_ID),
                    eq(SKILL_ID),
                    any(),
                    any(),
                    any(),
                    eq(KafkaEventConstants.SKILL_CHANGE_REMOVED)
            );
        }

        @Test
        @DisplayName("throws when the employee does not exist")
        void shouldThrowWhenEmployeeMissing() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeSkillService.remove(EMPLOYEE_ID, EMPLOYEE_SKILL_ID))
                    .isInstanceOf(EmployeeNotFoundException.class);

            verifyNoInteractions(employeeSkillRepository);
        }

        @Test
        @DisplayName("throws when the employee skill does not exist")
        void shouldThrowWhenEmployeeSkillMissing() {
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee(EMPLOYEE_ID)));
            when(employeeSkillRepository.findById(EMPLOYEE_SKILL_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeSkillService.remove(EMPLOYEE_ID, EMPLOYEE_SKILL_ID))
                    .isInstanceOf(EmployeeSkillNotFoundException.class);

            verify(employeeSkillRepository, never()).delete(any(EmployeeSkill.class));
        }

        @Test
        @DisplayName("throws when the employee skill belongs to another employee")
        void shouldThrowWhenNotOwned() {
            EmployeeSkill entity = employeeSkill(EMPLOYEE_SKILL_ID, OTHER_EMPLOYEE_ID, SKILL_ID);

            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee(EMPLOYEE_ID)));
            when(employeeSkillRepository.findById(EMPLOYEE_SKILL_ID)).thenReturn(Optional.of(entity));

            assertThatThrownBy(() -> employeeSkillService.remove(EMPLOYEE_ID, EMPLOYEE_SKILL_ID))
                    .isInstanceOf(ResourceDoesNotBelongToEmployeeException.class)
                    .hasMessageContaining(EMPLOYEE_ID.toString());

            verify(employeeSkillRepository, never()).delete(any(EmployeeSkill.class));
        }
    }

    private Employee employee(UUID id) {
        return Employee.builder()
                .id(id)
                .userId(UUID.randomUUID())
                .firstName("Ivan")
                .lastName("Petrov")
                .email("employee@company.com")
                .hireDate(LocalDate.of(2020, 1, 15))
                .build();
    }

    private Skill skill(UUID id, boolean active) {
        return Skill.builder()
                .id(id)
                .name("Java")
                .category("Backend")
                .active(active)
                .build();
    }

    private EmployeeSkill employeeSkill(UUID id, UUID ownerId, UUID skillId) {
        return EmployeeSkill.builder()
                .id(id)
                .employee(employee(ownerId))
                .skill(skill(skillId, true))
                .level(2)
                .build();
    }

    private EmployeeSkillResponseDto response() {
        return EmployeeSkillResponseDto.builder()
                .id(EMPLOYEE_SKILL_ID)
                .employeeId(EMPLOYEE_ID)
                .skill(SkillResponseDto.builder().id(SKILL_ID).name("Java").build())
                .level(3)
                .yearsOfExperience(new BigDecimal("2.5"))
                .lastAssessedAt(LocalDate.now())
                .build();
    }
}
