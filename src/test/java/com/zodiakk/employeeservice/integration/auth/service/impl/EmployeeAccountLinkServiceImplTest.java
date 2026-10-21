package com.zodiakk.employeeservice.integration.auth.service.impl;

import com.zodiakk.employeeservice.common.exception.ErrorMessages;
import com.zodiakk.employeeservice.employee.entity.Employee;
import com.zodiakk.employeeservice.employee.repository.EmployeeRepository;
import com.zodiakk.employeeservice.integration.auth.entity.EmployeeAccountUnlink;
import com.zodiakk.employeeservice.integration.auth.exception.EmployeeAccountLinkConflictException;
import com.zodiakk.employeeservice.integration.auth.repository.EmployeeAccountUnlinkRepository;
import com.zodiakk.employeeservice.integration.auth.repository.ProcessedEventRepository;
import com.zodiakk.employeeservice.integration.auth.service.EmployeeAccountEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeAccountLinkServiceImplTest {

    private static final UUID EVENT_ID = UUID.fromString("11112222-3333-4444-5555-666677778888");

    private static final UUID EMPLOYEE_ID = UUID.fromString("99990000-1111-2222-3333-444455556666");

    private static final UUID USER_ID = UUID.fromString("77778888-9999-aaaa-bbbb-ccccddddeeee");

    private static final UUID OTHER_USER_ID = UUID.fromString("00001111-2222-3333-4444-555566667777");

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @Mock
    private EmployeeAccountUnlinkRepository unlinkRepository;

    @Mock
    private EmployeeAccountEventService eventService;

    private EmployeeAccountLinkServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new EmployeeAccountLinkServiceImpl(
                employeeRepository,
                processedEventRepository,
                unlinkRepository,
                eventService
        );
    }

    @Nested
    @DisplayName("linkAccount")
    class LinkAccount {

        @Test
        @DisplayName("links a free employee to the requested user")
        void shouldLinkAccount() {
            Employee employee = employee(null);

            when(employeeRepository.findByIdForUpdate(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
            when(processedEventRepository.registerIfAbsent(EVENT_ID)).thenReturn(1);
            when(unlinkRepository.existsById(USER_ID)).thenReturn(false);

            service.linkAccount(EVENT_ID, EMPLOYEE_ID, USER_ID);

            assertThat(employee.getUserId()).isEqualTo(USER_ID);
            verify(employeeRepository).saveAndFlush(employee);
            verifyNoInteractions(eventService);
        }

        @Test
        @DisplayName("throws when the employee does not exist")
        void shouldRejectUnknownEmployee() {
            when(employeeRepository.findByIdForUpdate(EMPLOYEE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.linkAccount(EVENT_ID, EMPLOYEE_ID, USER_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage(ErrorMessages.EMPLOYEE_NOT_FOUND.formatted(EMPLOYEE_ID));

            verifyNoInteractions(processedEventRepository, unlinkRepository, eventService);
        }

        @Test
        @DisplayName("does nothing for an already processed event")
        void shouldIgnoreDuplicateEvent() {
            Employee employee = employee(null);

            when(employeeRepository.findByIdForUpdate(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
            when(processedEventRepository.registerIfAbsent(EVENT_ID)).thenReturn(0);

            service.linkAccount(EVENT_ID, EMPLOYEE_ID, USER_ID);

            assertThat(employee.getUserId()).isNull();
            verify(employeeRepository, never()).saveAndFlush(any(Employee.class));
            verifyNoInteractions(unlinkRepository, eventService);
        }

        @Test
        @DisplayName("ignores a USER_CREATED event obsoleted by an unlink")
        void shouldIgnoreObsoleteUserCreatedEvent() {
            Employee employee = employee(null);

            when(employeeRepository.findByIdForUpdate(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
            when(processedEventRepository.registerIfAbsent(EVENT_ID)).thenReturn(1);
            when(unlinkRepository.existsById(USER_ID)).thenReturn(true);

            service.linkAccount(EVENT_ID, EMPLOYEE_ID, USER_ID);

            assertThat(employee.getUserId()).isNull();
            verify(employeeRepository, never()).saveAndFlush(any(Employee.class));
            verifyNoInteractions(eventService);
        }

        @Test
        @DisplayName("throws a conflict when the employee is linked to another user")
        void shouldRejectConflictingLink() {
            Employee employee = employee(OTHER_USER_ID);

            when(employeeRepository.findByIdForUpdate(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
            when(processedEventRepository.registerIfAbsent(EVENT_ID)).thenReturn(1);
            when(unlinkRepository.existsById(USER_ID)).thenReturn(false);

            assertThatThrownBy(() -> service.linkAccount(EVENT_ID, EMPLOYEE_ID, USER_ID))
                    .isInstanceOf(EmployeeAccountLinkConflictException.class)
                    .hasMessage(ErrorMessages.ACCOUNT_ALREADY_LINKED.formatted(
                            EMPLOYEE_ID,
                            OTHER_USER_ID,
                            USER_ID
                    ));

            assertThat(employee.getUserId()).isEqualTo(OTHER_USER_ID);
            verify(employeeRepository, never()).saveAndFlush(any(Employee.class));
        }

        @Test
        @DisplayName("is idempotent when the employee is already linked to the same user")
        void shouldAcceptAlreadyLinkedEmployee() {
            Employee employee = employee(USER_ID);

            when(employeeRepository.findByIdForUpdate(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
            when(processedEventRepository.registerIfAbsent(EVENT_ID)).thenReturn(1);
            when(unlinkRepository.existsById(USER_ID)).thenReturn(false);

            assertThatCode(() -> service.linkAccount(EVENT_ID, EMPLOYEE_ID, USER_ID))
                    .doesNotThrowAnyException();

            assertThat(employee.getUserId()).isEqualTo(USER_ID);
            verify(employeeRepository, never()).saveAndFlush(any(Employee.class));
            verifyNoInteractions(eventService);
        }
    }

    @Nested
    @DisplayName("unlinkAccount")
    class UnlinkAccount {

        @Test
        @DisplayName("unlinks the employee, records the unlink and emits an outbox event")
        void shouldUnlinkAccount() {
            Employee employee = employee(USER_ID);

            when(employeeRepository.findByIdForUpdate(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
            when(processedEventRepository.registerIfAbsent(EVENT_ID)).thenReturn(1);
            when(unlinkRepository.existsById(USER_ID)).thenReturn(false);

            service.unlinkAccount(EVENT_ID, EMPLOYEE_ID, USER_ID);

            assertThat(employee.getUserId()).isNull();
            verify(employeeRepository).saveAndFlush(employee);

            ArgumentCaptor<EmployeeAccountUnlink> unlinkCaptor =
                    ArgumentCaptor.forClass(EmployeeAccountUnlink.class);
            verify(unlinkRepository).save(unlinkCaptor.capture());

            EmployeeAccountUnlink savedUnlink = unlinkCaptor.getValue();
            assertThat(savedUnlink.getUserId()).isEqualTo(USER_ID);
            assertThat(savedUnlink.getEmployeeId()).isEqualTo(EMPLOYEE_ID);
            assertThat(savedUnlink.getUnlinkedAt()).isNotNull();
            assertThat(savedUnlink.getUnlinkedAt().getOffset()).isEqualTo(ZoneOffset.UTC);

            verify(eventService).recordAccountUnlinked(USER_ID, EMPLOYEE_ID);
        }

        @Test
        @DisplayName("throws when the employee does not exist")
        void shouldRejectUnknownEmployee() {
            when(employeeRepository.findByIdForUpdate(EMPLOYEE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.unlinkAccount(EVENT_ID, EMPLOYEE_ID, USER_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage(ErrorMessages.EMPLOYEE_NOT_FOUND.formatted(EMPLOYEE_ID));

            verifyNoInteractions(processedEventRepository, unlinkRepository, eventService);
        }

        @Test
        @DisplayName("does nothing for an already processed event")
        void shouldIgnoreDuplicateEvent() {
            Employee employee = employee(USER_ID);

            when(employeeRepository.findByIdForUpdate(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
            when(processedEventRepository.registerIfAbsent(EVENT_ID)).thenReturn(0);

            service.unlinkAccount(EVENT_ID, EMPLOYEE_ID, USER_ID);

            assertThat(employee.getUserId()).isEqualTo(USER_ID);
            verify(employeeRepository, never()).saveAndFlush(any(Employee.class));
            verifyNoInteractions(unlinkRepository, eventService);
        }

        @Test
        @DisplayName("ignores a stale USER_DELETED event for a different user but records a tombstone")
        void shouldIgnoreStaleUserDeletedEvent() {
            Employee employee = employee(OTHER_USER_ID);

            when(employeeRepository.findByIdForUpdate(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
            when(processedEventRepository.registerIfAbsent(EVENT_ID)).thenReturn(1);
            when(unlinkRepository.existsById(USER_ID)).thenReturn(false);

            service.unlinkAccount(EVENT_ID, EMPLOYEE_ID, USER_ID);

            assertThat(employee.getUserId()).isEqualTo(OTHER_USER_ID);
            verify(employeeRepository, never()).saveAndFlush(any(Employee.class));

            ArgumentCaptor<EmployeeAccountUnlink> unlinkCaptor =
                    ArgumentCaptor.forClass(EmployeeAccountUnlink.class);
            verify(unlinkRepository).save(unlinkCaptor.capture());
            assertThat(unlinkCaptor.getValue().getUserId()).isEqualTo(USER_ID);
            assertThat(unlinkCaptor.getValue().getEmployeeId()).isEqualTo(EMPLOYEE_ID);

            verify(eventService).recordAccountUnlinked(USER_ID, EMPLOYEE_ID);
        }

        @Test
        @DisplayName("records an unlink when the employee was not linked to any user")
        void shouldHandleNotLinkedEmployee() {
            Employee employee = employee(null);

            when(employeeRepository.findByIdForUpdate(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
            when(processedEventRepository.registerIfAbsent(EVENT_ID)).thenReturn(1);
            when(unlinkRepository.existsById(USER_ID)).thenReturn(false);

            service.unlinkAccount(EVENT_ID, EMPLOYEE_ID, USER_ID);

            assertThat(employee.getUserId()).isNull();
            verify(employeeRepository, never()).saveAndFlush(any(Employee.class));
            verify(unlinkRepository).save(any(EmployeeAccountUnlink.class));
            verify(eventService).recordAccountUnlinked(USER_ID, EMPLOYEE_ID);
        }

        @Test
        @DisplayName("does not persist a duplicate unlink tombstone but still emits the event")
        void shouldNotPersistDuplicateUnlink() {
            Employee employee = employee(USER_ID);

            when(employeeRepository.findByIdForUpdate(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
            when(processedEventRepository.registerIfAbsent(EVENT_ID)).thenReturn(1);
            when(unlinkRepository.existsById(USER_ID)).thenReturn(true);

            service.unlinkAccount(EVENT_ID, EMPLOYEE_ID, USER_ID);

            assertThat(employee.getUserId()).isNull();
            verify(employeeRepository).saveAndFlush(employee);
            verify(unlinkRepository, never()).save(any(EmployeeAccountUnlink.class));
            verify(eventService).recordAccountUnlinked(USER_ID, EMPLOYEE_ID);
        }

        @Test
        @DisplayName("does not touch the employee when the event is stale and the tombstone already exists")
        void shouldIgnoreStaleEventWithExistingUnlink() {
            Employee employee = employee(OTHER_USER_ID);

            when(employeeRepository.findByIdForUpdate(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
            when(processedEventRepository.registerIfAbsent(EVENT_ID)).thenReturn(1);
            when(unlinkRepository.existsById(USER_ID)).thenReturn(true);

            service.unlinkAccount(EVENT_ID, EMPLOYEE_ID, USER_ID);

            assertThat(employee.getUserId()).isEqualTo(OTHER_USER_ID);
            verify(employeeRepository, never()).saveAndFlush(any(Employee.class));
            verify(unlinkRepository, never()).save(any(EmployeeAccountUnlink.class));
            verify(eventService).recordAccountUnlinked(USER_ID, EMPLOYEE_ID);
        }
    }

    private Employee employee(UUID userId) {
        return Employee.builder()
                .id(EMPLOYEE_ID)
                .userId(userId)
                .firstName("Ivan")
                .lastName("Ivanov")
                .email("ivan.ivanov@example.com")
                .build();
    }
}
