package com.zodiakk.employeeservice.employee.service.impl;

import com.zodiakk.employeeservice.common.dto.PageRequestDto;
import com.zodiakk.employeeservice.common.dto.SortDirection;
import com.zodiakk.employeeservice.employee.dto.request.create.DepartmentCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.filter.DepartmentFilterDto;
import com.zodiakk.employeeservice.employee.dto.request.update.DepartmentUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.DepartmentResponseDto;
import com.zodiakk.employeeservice.employee.entity.Department;
import com.zodiakk.employeeservice.employee.exception.department.DepartmentAlreadyExistsException;
import com.zodiakk.employeeservice.employee.exception.department.DepartmentInUseException;
import com.zodiakk.employeeservice.employee.exception.department.DepartmentNotFoundException;
import com.zodiakk.employeeservice.employee.mapper.DepartmentMapper;
import com.zodiakk.employeeservice.employee.repository.DepartmentRepository;
import com.zodiakk.employeeservice.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceImplTest {

    private static final UUID DEPARTMENT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final String NAME = "Engineering";

    private static final String DESCRIPTION = "Software engineering department";

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentMapper departmentMapper;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("saves a new active department when the name is unique")
        void shouldCreateDepartment() {
            DepartmentCreateRequestDto request = DepartmentCreateRequestDto.builder()
                    .name(NAME)
                    .description(DESCRIPTION)
                    .build();
            Department entity = Department.builder()
                    .name(NAME)
                    .description(DESCRIPTION)
                    .build();
            DepartmentResponseDto expected = response(NAME);

            when(departmentRepository.existsByNameIgnoreCase(NAME)).thenReturn(false);
            when(departmentMapper.toEntity(request)).thenReturn(entity);
            when(departmentRepository.save(entity)).thenReturn(entity);
            when(departmentMapper.toResponseDto(entity)).thenReturn(expected);

            DepartmentResponseDto result = departmentService.create(request);

            assertThat(result).isSameAs(expected);

            ArgumentCaptor<Department> captor = ArgumentCaptor.forClass(Department.class);
            verify(departmentRepository).save(captor.capture());

            Department saved = captor.getValue();
            assertThat(saved.getActive()).isTrue();
            assertThat(saved.getName()).isEqualTo(NAME);
            verify(departmentMapper).toEntity(request);
            verify(departmentMapper).toResponseDto(entity);
        }

        @Test
        @DisplayName("rejects a duplicate name")
        void shouldRejectDuplicateName() {
            DepartmentCreateRequestDto request = DepartmentCreateRequestDto.builder()
                    .name(NAME)
                    .description(DESCRIPTION)
                    .build();

            when(departmentRepository.existsByNameIgnoreCase(NAME)).thenReturn(true);

            assertThatThrownBy(() -> departmentService.create(request))
                    .isInstanceOf(DepartmentAlreadyExistsException.class)
                    .hasMessageContaining(NAME);

            verify(departmentRepository, never()).save(any(Department.class));
            verifyNoInteractions(departmentMapper);
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("returns the mapped department when it exists")
        void shouldReturnDepartment() {
            Department entity = department(NAME);
            DepartmentResponseDto expected = response(NAME);

            when(departmentRepository.findById(DEPARTMENT_ID)).thenReturn(Optional.of(entity));
            when(departmentMapper.toResponseDto(entity)).thenReturn(expected);

            assertThat(departmentService.getById(DEPARTMENT_ID)).isSameAs(expected);
        }

        @Test
        @DisplayName("throws when the department does not exist")
        void shouldThrowWhenMissing() {
            when(departmentRepository.findById(DEPARTMENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> departmentService.getById(DEPARTMENT_ID))
                    .isInstanceOf(DepartmentNotFoundException.class)
                    .hasMessageContaining(DEPARTMENT_ID.toString());

            verifyNoInteractions(departmentMapper);
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("maps a filtered page and preserves paging metadata")
        void shouldReturnMappedPage() {
            DepartmentFilterDto filter = DepartmentFilterDto.builder()
                    .name("eng")
                    .active(true)
                    .description("software")
                    .build();
            PageRequestDto pageRequest = PageRequestDto.builder()
                    .page(1)
                    .size(5)
                    .sortBy("name")
                    .sortDirection(SortDirection.DESC)
                    .build();

            Department entity = department(NAME);
            DepartmentResponseDto expected = response(NAME);
            Page<Department> page = new PageImpl<>(List.of(entity), PageRequest.of(1, 5), 6);

            when(departmentRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
            when(departmentMapper.toResponseDto(entity)).thenReturn(expected);

            Page<DepartmentResponseDto> result = departmentService.findAll(filter, pageRequest);

            assertThat(result.getContent()).containsExactly(expected);
            assertThat(result.getTotalElements()).isEqualTo(6);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.getNumber()).isEqualTo(1);
            assertThat(result.getSize()).isEqualTo(5);

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(departmentRepository).findAll(any(Specification.class), pageableCaptor.capture());

            Pageable pageable = pageableCaptor.getValue();
            assertThat(pageable.getPageNumber()).isEqualTo(1);
            assertThat(pageable.getPageSize()).isEqualTo(5);
            assertThat(pageable.getSort().getOrderFor("name")).isNotNull();
            assertThat(pageable.getSort().getOrderFor("name").getDirection()).isEqualTo(Sort.Direction.DESC);
        }

        @Test
        @DisplayName("returns an empty page without calling the mapper")
        void shouldReturnEmptyPage() {
            DepartmentFilterDto filter = DepartmentFilterDto.builder().name("missing").build();
            PageRequestDto pageRequest = PageRequestDto.builder().page(0).size(20).build();

            when(departmentRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            Page<DepartmentResponseDto> result = departmentService.findAll(filter, pageRequest);

            assertThat(result).isEmpty();
            assertThat(result.getTotalElements()).isZero();
            assertThat(result.getNumber()).isZero();
            verifyNoInteractions(departmentMapper);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("updates a department when the new name is unique")
        void shouldUpdateDepartment() {
            Department entity = department(NAME);
            DepartmentUpdateRequestDto request = DepartmentUpdateRequestDto.builder()
                    .name("Platform")
                    .description("Platform department")
                    .build();
            DepartmentResponseDto expected = response("Platform");

            when(departmentRepository.findById(DEPARTMENT_ID)).thenReturn(Optional.of(entity));
            when(departmentRepository.existsByNameIgnoreCase("Platform")).thenReturn(false);
            when(departmentRepository.save(entity)).thenReturn(entity);
            when(departmentMapper.toResponseDto(entity)).thenReturn(expected);

            DepartmentResponseDto result = departmentService.update(DEPARTMENT_ID, request);

            assertThat(result).isSameAs(expected);
            verify(departmentMapper).updateEntity(request, entity);
            verify(departmentRepository).save(entity);
        }

        @Test
        @DisplayName("skips the uniqueness check when the name is unchanged ignoring case")
        void shouldSkipCheckForUnchangedName() {
            Department entity = department(NAME);
            DepartmentUpdateRequestDto request = DepartmentUpdateRequestDto.builder()
                    .name("ENGINEERING")
                    .description("Updated description")
                    .build();
            DepartmentResponseDto expected = response(NAME);

            when(departmentRepository.findById(DEPARTMENT_ID)).thenReturn(Optional.of(entity));
            when(departmentRepository.save(entity)).thenReturn(entity);
            when(departmentMapper.toResponseDto(entity)).thenReturn(expected);

            assertThat(departmentService.update(DEPARTMENT_ID, request)).isSameAs(expected);

            verify(departmentRepository, never()).existsByNameIgnoreCase(anyString());
            verify(departmentMapper).updateEntity(request, entity);
        }

        @Test
        @DisplayName("skips the uniqueness check when the name is null")
        void shouldSkipCheckForNullName() {
            Department entity = department(NAME);
            DepartmentUpdateRequestDto request = DepartmentUpdateRequestDto.builder()
                    .description("Updated description")
                    .build();
            DepartmentResponseDto expected = response(NAME);

            when(departmentRepository.findById(DEPARTMENT_ID)).thenReturn(Optional.of(entity));
            when(departmentRepository.save(entity)).thenReturn(entity);
            when(departmentMapper.toResponseDto(entity)).thenReturn(expected);

            assertThat(departmentService.update(DEPARTMENT_ID, request)).isSameAs(expected);

            verify(departmentRepository, never()).existsByNameIgnoreCase(anyString());
        }

        @Test
        @DisplayName("skips the uniqueness check when the name is blank")
        void shouldSkipCheckForBlankName() {
            Department entity = department(NAME);
            DepartmentUpdateRequestDto request = DepartmentUpdateRequestDto.builder()
                    .name("   ")
                    .description("Updated description")
                    .build();
            DepartmentResponseDto expected = response(NAME);

            when(departmentRepository.findById(DEPARTMENT_ID)).thenReturn(Optional.of(entity));
            when(departmentRepository.save(entity)).thenReturn(entity);
            when(departmentMapper.toResponseDto(entity)).thenReturn(expected);

            assertThat(departmentService.update(DEPARTMENT_ID, request)).isSameAs(expected);

            verify(departmentRepository, never()).existsByNameIgnoreCase(anyString());
        }

        @Test
        @DisplayName("throws when the department does not exist")
        void shouldThrowWhenMissing() {
            DepartmentUpdateRequestDto request = DepartmentUpdateRequestDto.builder()
                    .name("Platform")
                    .build();

            when(departmentRepository.findById(DEPARTMENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> departmentService.update(DEPARTMENT_ID, request))
                    .isInstanceOf(DepartmentNotFoundException.class);

            verifyNoInteractions(departmentMapper);
            verify(departmentRepository, never()).save(any(Department.class));
        }

        @Test
        @DisplayName("throws when the new name is already taken")
        void shouldThrowForDuplicateName() {
            Department entity = department(NAME);
            DepartmentUpdateRequestDto request = DepartmentUpdateRequestDto.builder()
                    .name("Platform")
                    .build();

            when(departmentRepository.findById(DEPARTMENT_ID)).thenReturn(Optional.of(entity));
            when(departmentRepository.existsByNameIgnoreCase("Platform")).thenReturn(true);

            assertThatThrownBy(() -> departmentService.update(DEPARTMENT_ID, request))
                    .isInstanceOf(DepartmentAlreadyExistsException.class)
                    .hasMessageContaining("Platform");

            verify(departmentMapper, never()).updateEntity(any(), any());
            verify(departmentRepository, never()).save(any(Department.class));
        }
    }

    @Nested
    @DisplayName("deactivate")
    class Deactivate {

        @Test
        @DisplayName("deactivates an active department")
        void shouldDeactivate() {
            Department entity = department(NAME);

            when(departmentRepository.findById(DEPARTMENT_ID)).thenReturn(Optional.of(entity));

            departmentService.deactivate(DEPARTMENT_ID);

            assertThat(entity.getActive()).isFalse();
            verify(departmentRepository).save(entity);
        }

        @Test
        @DisplayName("leaves an already inactive department untouched")
        void shouldSkipAlreadyInactive() {
            Department entity = department(NAME);
            entity.setActive(false);

            when(departmentRepository.findById(DEPARTMENT_ID)).thenReturn(Optional.of(entity));

            departmentService.deactivate(DEPARTMENT_ID);

            assertThat(entity.getActive()).isFalse();
            verify(departmentRepository, never()).save(any(Department.class));
        }

        @Test
        @DisplayName("throws when the department does not exist")
        void shouldThrowWhenMissing() {
            when(departmentRepository.findById(DEPARTMENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> departmentService.deactivate(DEPARTMENT_ID))
                    .isInstanceOf(DepartmentNotFoundException.class);

            verify(departmentRepository, never()).save(any(Department.class));
        }
    }

    @Nested
    @DisplayName("activate")
    class Activate {

        @Test
        @DisplayName("activates an inactive department")
        void shouldActivate() {
            Department entity = department(NAME);
            entity.setActive(false);

            when(departmentRepository.findById(DEPARTMENT_ID)).thenReturn(Optional.of(entity));

            departmentService.activate(DEPARTMENT_ID);

            assertThat(entity.getActive()).isTrue();
            verify(departmentRepository).save(entity);
        }

        @Test
        @DisplayName("leaves an already active department untouched")
        void shouldSkipAlreadyActive() {
            Department entity = department(NAME);

            when(departmentRepository.findById(DEPARTMENT_ID)).thenReturn(Optional.of(entity));

            departmentService.activate(DEPARTMENT_ID);

            assertThat(entity.getActive()).isTrue();
            verify(departmentRepository, never()).save(any(Department.class));
        }

        @Test
        @DisplayName("throws when the department does not exist")
        void shouldThrowWhenMissing() {
            when(departmentRepository.findById(DEPARTMENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> departmentService.activate(DEPARTMENT_ID))
                    .isInstanceOf(DepartmentNotFoundException.class);

            verify(departmentRepository, never()).save(any(Department.class));
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("deletes a department that has no employees")
        void shouldDelete() {
            Department entity = department(NAME);

            when(departmentRepository.findById(DEPARTMENT_ID)).thenReturn(Optional.of(entity));
            when(employeeRepository.countByDepartmentId(DEPARTMENT_ID)).thenReturn(0L);

            departmentService.delete(DEPARTMENT_ID);

            verify(employeeRepository).countByDepartmentId(DEPARTMENT_ID);
            verify(departmentRepository).delete(entity);
        }

        @Test
        @DisplayName("rejects deletion when employees reference the department")
        void shouldRejectWhenInUse() {
            Department entity = department(NAME);

            when(departmentRepository.findById(DEPARTMENT_ID)).thenReturn(Optional.of(entity));
            when(employeeRepository.countByDepartmentId(DEPARTMENT_ID)).thenReturn(3L);

            assertThatThrownBy(() -> departmentService.delete(DEPARTMENT_ID))
                    .isInstanceOf(DepartmentInUseException.class)
                    .hasMessageContaining(DEPARTMENT_ID.toString());

            verify(departmentRepository, never()).delete(any(Department.class));
        }

        @Test
        @DisplayName("throws when the department does not exist")
        void shouldThrowWhenMissing() {
            when(departmentRepository.findById(DEPARTMENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> departmentService.delete(DEPARTMENT_ID))
                    .isInstanceOf(DepartmentNotFoundException.class);

            verifyNoInteractions(employeeRepository);
            verify(departmentRepository, never()).delete(any(Department.class));
        }
    }

    private Department department(String name) {
        return Department.builder()
                .id(DEPARTMENT_ID)
                .name(name)
                .description(DESCRIPTION)
                .active(true)
                .build();
    }

    private DepartmentResponseDto response(String name) {
        return DepartmentResponseDto.builder()
                .id(DEPARTMENT_ID)
                .name(name)
                .description(DESCRIPTION)
                .active(true)
                .build();
    }
}
