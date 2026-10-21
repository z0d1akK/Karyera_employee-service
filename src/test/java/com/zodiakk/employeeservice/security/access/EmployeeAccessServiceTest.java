package com.zodiakk.employeeservice.security.access;

import com.zodiakk.employeeservice.common.constants.SecurityConstants;
import com.zodiakk.employeeservice.employee.entity.Employee;
import com.zodiakk.employeeservice.employee.entity.enums.EmploymentStatus;
import com.zodiakk.employeeservice.employee.repository.EmployeeRepository;
import com.zodiakk.employeeservice.employee.repository.EmployeeResponsibilityRepository;
import com.zodiakk.employeeservice.security.principal.CurrentUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeAccessServiceTest {

    private static final UUID CURRENT_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final UUID EMPLOYEE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private static final UUID MANAGER_EMPLOYEE_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    private static final UUID OTHER_USER_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    private static final String EMPLOYEE_ROLE = SecurityConstants.EMPLOYEE_AUTHORITY;

    private static final String MANAGER_ROLE = SecurityConstants.MANAGER_AUTHORITY;

    private static final String HR_ROLE = SecurityConstants.HR_AUTHORITY;

    private static final String ADMIN_ROLE = SecurityConstants.ADMIN_AUTHORITY;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeResponsibilityRepository employeeResponsibilityRepository;

    @InjectMocks
    private EmployeeAccessService employeeAccessService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("isPrivileged")
    class IsPrivileged {

        @Test
        @DisplayName("is true for HR")
        void shouldBeTrueForHr() {
            authenticate(HR_ROLE);

            assertThat(employeeAccessService.isPrivileged()).isTrue();
        }

        @Test
        @DisplayName("is true for ADMIN")
        void shouldBeTrueForAdmin() {
            authenticate(ADMIN_ROLE);

            assertThat(employeeAccessService.isPrivileged()).isTrue();
        }

        @Test
        @DisplayName("is false for MANAGER")
        void shouldBeFalseForManager() {
            authenticate(MANAGER_ROLE);

            assertThat(employeeAccessService.isPrivileged()).isFalse();
        }

        @Test
        @DisplayName("is false for EMPLOYEE")
        void shouldBeFalseForEmployee() {
            authenticate(EMPLOYEE_ROLE);

            assertThat(employeeAccessService.isPrivileged()).isFalse();
        }
    }

    @Nested
    @DisplayName("isSelf")
    class IsSelf {

        @Test
        @DisplayName("is true when the employee belongs to the current user")
        void shouldBeTrueForOwnEmployee() {
            authenticate(EMPLOYEE_ROLE);
            when(employeeRepository.findById(EMPLOYEE_ID))
                    .thenReturn(Optional.of(employee(EMPLOYEE_ID, CURRENT_USER_ID)));

            assertThat(employeeAccessService.isSelf(EMPLOYEE_ID)).isTrue();
        }

        @Test
        @DisplayName("is false when the employee belongs to another user")
        void shouldBeFalseForForeignEmployee() {
            authenticate(EMPLOYEE_ROLE);
            when(employeeRepository.findById(EMPLOYEE_ID))
                    .thenReturn(Optional.of(employee(EMPLOYEE_ID, OTHER_USER_ID)));

            assertThat(employeeAccessService.isSelf(EMPLOYEE_ID)).isFalse();
        }

        @Test
        @DisplayName("is false when the employee does not exist")
        void shouldBeFalseWhenEmployeeMissing() {
            authenticate(EMPLOYEE_ROLE);
            when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.empty());

            assertThat(employeeAccessService.isSelf(EMPLOYEE_ID)).isFalse();
        }
    }

    @Nested
    @DisplayName("isSelfUser")
    class IsSelfUser {

        @Test
        @DisplayName("is true for the current user")
        void shouldBeTrueForCurrentUser() {
            authenticate(EMPLOYEE_ROLE);

            assertThat(employeeAccessService.isSelfUser(CURRENT_USER_ID)).isTrue();
        }

        @Test
        @DisplayName("is false for another user")
        void shouldBeFalseForOtherUser() {
            authenticate(EMPLOYEE_ROLE);

            assertThat(employeeAccessService.isSelfUser(OTHER_USER_ID)).isFalse();
        }
    }

    @Nested
    @DisplayName("isManagerOf")
    class IsManagerOf {

        @Test
        @DisplayName("is true when an active responsibility links the subordinate to the current user")
        void shouldBeTrueForSubordinate() {
            authenticate(MANAGER_ROLE);
            when(employeeRepository.findByUserId(CURRENT_USER_ID))
                    .thenReturn(Optional.of(employee(MANAGER_EMPLOYEE_ID, CURRENT_USER_ID)));
            when(employeeResponsibilityRepository
                    .existsByEmployeeIdAndResponsibleEmployeeIdAndEndDateIsNull(
                            EMPLOYEE_ID,
                            MANAGER_EMPLOYEE_ID
                    ))
                    .thenReturn(true);

            assertThat(employeeAccessService.isManagerOf(EMPLOYEE_ID)).isTrue();
        }

        @Test
        @DisplayName("is false when no active responsibility links the subordinate")
        void shouldBeFalseWithoutActiveResponsibility() {
            authenticate(MANAGER_ROLE);
            when(employeeRepository.findByUserId(CURRENT_USER_ID))
                    .thenReturn(Optional.of(employee(MANAGER_EMPLOYEE_ID, CURRENT_USER_ID)));
            when(employeeResponsibilityRepository
                    .existsByEmployeeIdAndResponsibleEmployeeIdAndEndDateIsNull(
                            EMPLOYEE_ID,
                            MANAGER_EMPLOYEE_ID
                    ))
                    .thenReturn(false);

            assertThat(employeeAccessService.isManagerOf(EMPLOYEE_ID)).isFalse();
        }

        @Test
        @DisplayName("is false when the current user has no employee profile")
        void shouldBeFalseWithoutManagerEmployee() {
            authenticate(MANAGER_ROLE);
            when(employeeRepository.findByUserId(CURRENT_USER_ID)).thenReturn(Optional.empty());

            assertThat(employeeAccessService.isManagerOf(EMPLOYEE_ID)).isFalse();
        }
    }

    @Nested
    @DisplayName("canViewEmployee")
    class CanViewEmployee {

        @Test
        @DisplayName("is true for a privileged user")
        void shouldBeTrueForPrivilegedUser() {
            authenticate(HR_ROLE);

            assertThat(employeeAccessService.canViewEmployee(EMPLOYEE_ID)).isTrue();
            verifyNoInteractions(employeeRepository, employeeResponsibilityRepository);
        }

        @Test
        @DisplayName("is true for the employee himself")
        void shouldBeTrueForSelf() {
            authenticate(EMPLOYEE_ROLE);
            when(employeeRepository.findById(EMPLOYEE_ID))
                    .thenReturn(Optional.of(employee(EMPLOYEE_ID, CURRENT_USER_ID)));

            assertThat(employeeAccessService.canViewEmployee(EMPLOYEE_ID)).isTrue();
        }

        @Test
        @DisplayName("is true for the manager of the employee")
        void shouldBeTrueForManager() {
            authenticate(MANAGER_ROLE);
            stubManagerOf(true);

            assertThat(employeeAccessService.canViewEmployee(EMPLOYEE_ID)).isTrue();
        }

        @Test
        @DisplayName("is false for an unrelated employee")
        void shouldBeFalseForUnrelatedEmployee() {
            authenticate(MANAGER_ROLE);
            stubManagerOf(false);

            assertThat(employeeAccessService.canViewEmployee(EMPLOYEE_ID)).isFalse();
        }
    }

    @Nested
    @DisplayName("canViewUser")
    class CanViewUser {

        @Test
        @DisplayName("is true for a privileged user")
        void shouldBeTrueForPrivilegedUser() {
            authenticate(ADMIN_ROLE);

            assertThat(employeeAccessService.canViewUser(OTHER_USER_ID)).isTrue();
            verifyNoInteractions(employeeRepository);
        }

        @Test
        @DisplayName("is true for the current user")
        void shouldBeTrueForSelf() {
            authenticate(EMPLOYEE_ROLE);

            assertThat(employeeAccessService.canViewUser(CURRENT_USER_ID)).isTrue();
            verifyNoInteractions(employeeRepository);
        }

        @Test
        @DisplayName("is true when the target user is a subordinate")
        void shouldBeTrueForSubordinateUser() {
            authenticate(MANAGER_ROLE);
            when(employeeRepository.findByUserId(OTHER_USER_ID))
                    .thenReturn(Optional.of(employee(EMPLOYEE_ID, OTHER_USER_ID)));
            when(employeeRepository.findByUserId(CURRENT_USER_ID))
                    .thenReturn(Optional.of(employee(MANAGER_EMPLOYEE_ID, CURRENT_USER_ID)));
            when(employeeResponsibilityRepository
                    .existsByEmployeeIdAndResponsibleEmployeeIdAndEndDateIsNull(
                            EMPLOYEE_ID,
                            MANAGER_EMPLOYEE_ID
                    ))
                    .thenReturn(true);

            assertThat(employeeAccessService.canViewUser(OTHER_USER_ID)).isTrue();
        }

        @Test
        @DisplayName("is false when the target user has no employee profile")
        void shouldBeFalseWhenTargetUserHasNoEmployee() {
            authenticate(MANAGER_ROLE);
            when(employeeRepository.findByUserId(OTHER_USER_ID)).thenReturn(Optional.empty());

            assertThat(employeeAccessService.canViewUser(OTHER_USER_ID)).isFalse();
        }

        @Test
        @DisplayName("is false when the target user is not a subordinate")
        void shouldBeFalseWhenTargetUserIsNotSubordinate() {
            authenticate(MANAGER_ROLE);
            when(employeeRepository.findByUserId(OTHER_USER_ID))
                    .thenReturn(Optional.of(employee(EMPLOYEE_ID, OTHER_USER_ID)));
            when(employeeRepository.findByUserId(CURRENT_USER_ID))
                    .thenReturn(Optional.of(employee(MANAGER_EMPLOYEE_ID, CURRENT_USER_ID)));
            when(employeeResponsibilityRepository
                    .existsByEmployeeIdAndResponsibleEmployeeIdAndEndDateIsNull(
                            EMPLOYEE_ID,
                            MANAGER_EMPLOYEE_ID
                    ))
                    .thenReturn(false);

            assertThat(employeeAccessService.canViewUser(OTHER_USER_ID)).isFalse();
        }
    }

    @Nested
    @DisplayName("canEditEmployee")
    class CanEditEmployee {

        @Test
        @DisplayName("is true for a privileged user")
        void shouldBeTrueForPrivilegedUser() {
            authenticate(HR_ROLE);

            assertThat(employeeAccessService.canEditEmployee(EMPLOYEE_ID)).isTrue();
            verifyNoInteractions(employeeRepository);
        }

        @Test
        @DisplayName("is true for the employee himself")
        void shouldBeTrueForSelf() {
            authenticate(EMPLOYEE_ROLE);
            when(employeeRepository.findById(EMPLOYEE_ID))
                    .thenReturn(Optional.of(employee(EMPLOYEE_ID, CURRENT_USER_ID)));

            assertThat(employeeAccessService.canEditEmployee(EMPLOYEE_ID)).isTrue();
        }

        @Test
        @DisplayName("is false for the manager of the employee")
        void shouldBeFalseForManager() {
            authenticate(MANAGER_ROLE);
            when(employeeRepository.findById(EMPLOYEE_ID))
                    .thenReturn(Optional.of(employee(EMPLOYEE_ID, OTHER_USER_ID)));

            assertThat(employeeAccessService.canEditEmployee(EMPLOYEE_ID)).isFalse();
        }

        @Test
        @DisplayName("is false for an unrelated employee")
        void shouldBeFalseForUnrelatedEmployee() {
            authenticate(EMPLOYEE_ROLE);
            when(employeeRepository.findById(EMPLOYEE_ID))
                    .thenReturn(Optional.of(employee(EMPLOYEE_ID, OTHER_USER_ID)));

            assertThat(employeeAccessService.canEditEmployee(EMPLOYEE_ID)).isFalse();
        }
    }

    @Nested
    @DisplayName("canManageEmployeeProfiles")
    class CanManageEmployeeProfiles {

        @Test
        @DisplayName("is true for HR")
        void shouldBeTrueForHr() {
            authenticate(HR_ROLE);

            assertThat(employeeAccessService.canManageEmployeeProfiles()).isTrue();
        }

        @Test
        @DisplayName("is true for ADMIN")
        void shouldBeTrueForAdmin() {
            authenticate(ADMIN_ROLE);

            assertThat(employeeAccessService.canManageEmployeeProfiles()).isTrue();
        }

        @Test
        @DisplayName("is false for MANAGER")
        void shouldBeFalseForManager() {
            authenticate(MANAGER_ROLE);

            assertThat(employeeAccessService.canManageEmployeeProfiles()).isFalse();
        }

        @Test
        @DisplayName("is false for EMPLOYEE")
        void shouldBeFalseForEmployee() {
            authenticate(EMPLOYEE_ROLE);

            assertThat(employeeAccessService.canManageEmployeeProfiles()).isFalse();
        }
    }

    @Nested
    @DisplayName("canManageCatalogs")
    class CanManageCatalogs {

        @Test
        @DisplayName("is true for ADMIN")
        void shouldBeTrueForAdmin() {
            authenticate(ADMIN_ROLE);

            assertThat(employeeAccessService.canManageCatalogs()).isTrue();
        }

        @Test
        @DisplayName("is false for HR")
        void shouldBeFalseForHr() {
            authenticate(HR_ROLE);

            assertThat(employeeAccessService.canManageCatalogs()).isFalse();
        }

        @Test
        @DisplayName("is false for MANAGER")
        void shouldBeFalseForManager() {
            authenticate(MANAGER_ROLE);

            assertThat(employeeAccessService.canManageCatalogs()).isFalse();
        }
    }

    @Nested
    @DisplayName("canListEmployees")
    class CanListEmployees {

        @Test
        @DisplayName("is true for MANAGER")
        void shouldBeTrueForManager() {
            authenticate(MANAGER_ROLE);

            assertThat(employeeAccessService.canListEmployees()).isTrue();
        }

        @Test
        @DisplayName("is true for HR")
        void shouldBeTrueForHr() {
            authenticate(HR_ROLE);

            assertThat(employeeAccessService.canListEmployees()).isTrue();
        }

        @Test
        @DisplayName("is true for ADMIN")
        void shouldBeTrueForAdmin() {
            authenticate(ADMIN_ROLE);

            assertThat(employeeAccessService.canListEmployees()).isTrue();
        }

        @Test
        @DisplayName("is false for EMPLOYEE")
        void shouldBeFalseForEmployee() {
            authenticate(EMPLOYEE_ROLE);

            assertThat(employeeAccessService.canListEmployees()).isFalse();
        }
    }

    @Nested
    @DisplayName("isManagerOnly")
    class IsManagerOnly {

        @Test
        @DisplayName("is true only for MANAGER")
        void shouldBeTrueForManager() {
            authenticate(MANAGER_ROLE);

            assertThat(employeeAccessService.isManagerOnly()).isTrue();
        }

        @Test
        @DisplayName("is false for HR")
        void shouldBeFalseForHr() {
            authenticate(HR_ROLE);

            assertThat(employeeAccessService.isManagerOnly()).isFalse();
        }

        @Test
        @DisplayName("is false for ADMIN")
        void shouldBeFalseForAdmin() {
            authenticate(ADMIN_ROLE);

            assertThat(employeeAccessService.isManagerOnly()).isFalse();
        }

        @Test
        @DisplayName("is false for EMPLOYEE")
        void shouldBeFalseForEmployee() {
            authenticate(EMPLOYEE_ROLE);

            assertThat(employeeAccessService.isManagerOnly()).isFalse();
        }
    }

    private void authenticate(String role) {
        CurrentUser currentUser = CurrentUser.builder()
                .userId(CURRENT_USER_ID)
                .role(role)
                .email("current.user@company.com")
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(currentUser, null, null)
        );
    }

    private void stubManagerOf(boolean subordinate) {
        when(employeeRepository.findById(EMPLOYEE_ID))
                .thenReturn(Optional.of(employee(EMPLOYEE_ID, OTHER_USER_ID)));
        when(employeeRepository.findByUserId(CURRENT_USER_ID))
                .thenReturn(Optional.of(employee(MANAGER_EMPLOYEE_ID, CURRENT_USER_ID)));
        when(employeeResponsibilityRepository
                .existsByEmployeeIdAndResponsibleEmployeeIdAndEndDateIsNull(
                        EMPLOYEE_ID,
                        MANAGER_EMPLOYEE_ID
                ))
                .thenReturn(subordinate);
    }

    private static Employee employee(UUID id, UUID userId) {
        return Employee.builder()
                .id(id)
                .userId(userId)
                .firstName("Ivan")
                .lastName("Petrov")
                .email("employee@company.com")
                .hireDate(LocalDate.of(2024, 1, 1))
                .employmentStatus(EmploymentStatus.ACTIVE)
                .build();
    }
}
