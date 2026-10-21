package com.zodiakk.employeeservice.employee.service.impl;

import com.zodiakk.employeeservice.common.dto.PageRequestDto;
import com.zodiakk.employeeservice.common.dto.SortDirection;
import com.zodiakk.employeeservice.employee.dto.request.create.ResponsibilityTypeCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.filter.ResponsibilityTypeFilterDto;
import com.zodiakk.employeeservice.employee.dto.request.update.ResponsibilityTypeUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.ResponsibilityTypeResponseDto;
import com.zodiakk.employeeservice.employee.entity.ResponsibilityType;
import com.zodiakk.employeeservice.employee.exception.responsibilitytype.ResponsibilityTypeAlreadyExistsException;
import com.zodiakk.employeeservice.employee.exception.responsibilitytype.ResponsibilityTypeInUseException;
import com.zodiakk.employeeservice.employee.exception.responsibilitytype.ResponsibilityTypeNotFoundException;
import com.zodiakk.employeeservice.employee.mapper.ResponsibilityTypeMapper;
import com.zodiakk.employeeservice.employee.repository.EmployeeResponsibilityRepository;
import com.zodiakk.employeeservice.employee.repository.ResponsibilityTypeRepository;
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
class ResponsibilityTypeServiceImplTest {

    private static final UUID RESPONSIBILITY_TYPE_ID =
            UUID.fromString("66666666-6666-6666-6666-666666666666");

    private static final String CODE = "MENTOR";

    private static final String NAME = "Mentor";

    private static final String DESCRIPTION = "Mentors other employees";

    @Mock
    private ResponsibilityTypeRepository responsibilityTypeRepository;

    @Mock
    private EmployeeResponsibilityRepository employeeResponsibilityRepository;

    @Mock
    private ResponsibilityTypeMapper responsibilityTypeMapper;

    @InjectMocks
    private ResponsibilityTypeServiceImpl responsibilityTypeService;

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("saves a new active responsibility type when the code is unique")
        void shouldCreateResponsibilityType() {
            ResponsibilityTypeCreateRequestDto request = ResponsibilityTypeCreateRequestDto.builder()
                    .code(CODE)
                    .name(NAME)
                    .description(DESCRIPTION)
                    .multipleAllowed(false)
                    .build();
            ResponsibilityType entity = ResponsibilityType.builder()
                    .code(CODE)
                    .name(NAME)
                    .description(DESCRIPTION)
                    .multipleAllowed(false)
                    .build();
            ResponsibilityTypeResponseDto expected = response(CODE);

            when(responsibilityTypeRepository.existsByCode(CODE)).thenReturn(false);
            when(responsibilityTypeMapper.toEntity(request)).thenReturn(entity);
            when(responsibilityTypeRepository.save(entity)).thenReturn(entity);
            when(responsibilityTypeMapper.toResponseDto(entity)).thenReturn(expected);

            ResponsibilityTypeResponseDto result = responsibilityTypeService.create(request);

            assertThat(result).isSameAs(expected);

            ArgumentCaptor<ResponsibilityType> captor =
                    ArgumentCaptor.forClass(ResponsibilityType.class);
            verify(responsibilityTypeRepository).save(captor.capture());

            ResponsibilityType saved = captor.getValue();
            assertThat(saved.getActive()).isTrue();
            assertThat(saved.getCode()).isEqualTo(CODE);
            assertThat(saved.getMultipleAllowed()).isFalse();
            verify(responsibilityTypeMapper).toEntity(request);
            verify(responsibilityTypeMapper).toResponseDto(entity);
        }

        @Test
        @DisplayName("rejects a duplicate code")
        void shouldRejectDuplicateCode() {
            ResponsibilityTypeCreateRequestDto request = ResponsibilityTypeCreateRequestDto.builder()
                    .code(CODE)
                    .name(NAME)
                    .multipleAllowed(false)
                    .build();

            when(responsibilityTypeRepository.existsByCode(CODE)).thenReturn(true);

            assertThatThrownBy(() -> responsibilityTypeService.create(request))
                    .isInstanceOf(ResponsibilityTypeAlreadyExistsException.class)
                    .hasMessageContaining(CODE);

            verify(responsibilityTypeRepository, never()).save(any(ResponsibilityType.class));
            verifyNoInteractions(responsibilityTypeMapper);
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("returns the mapped responsibility type when it exists")
        void shouldReturnResponsibilityType() {
            ResponsibilityType entity = responsibilityType(CODE);
            ResponsibilityTypeResponseDto expected = response(CODE);

            when(responsibilityTypeRepository.findById(RESPONSIBILITY_TYPE_ID)).thenReturn(Optional.of(entity));
            when(responsibilityTypeMapper.toResponseDto(entity)).thenReturn(expected);

            assertThat(responsibilityTypeService.getById(RESPONSIBILITY_TYPE_ID)).isSameAs(expected);
        }

        @Test
        @DisplayName("throws when the responsibility type does not exist")
        void shouldThrowWhenMissing() {
            when(responsibilityTypeRepository.findById(RESPONSIBILITY_TYPE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> responsibilityTypeService.getById(RESPONSIBILITY_TYPE_ID))
                    .isInstanceOf(ResponsibilityTypeNotFoundException.class)
                    .hasMessageContaining(RESPONSIBILITY_TYPE_ID.toString());

            verifyNoInteractions(responsibilityTypeMapper);
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("maps a filtered page and preserves paging metadata")
        void shouldReturnMappedPage() {
            ResponsibilityTypeFilterDto filter = ResponsibilityTypeFilterDto.builder()
                    .code("ment")
                    .name("mentor")
                    .active(true)
                    .description("guidance")
                    .multipleAllowed(false)
                    .build();
            PageRequestDto pageRequest = PageRequestDto.builder()
                    .page(1)
                    .size(5)
                    .sortBy("code")
                    .sortDirection(SortDirection.DESC)
                    .build();

            ResponsibilityType entity = responsibilityType(CODE);
            ResponsibilityTypeResponseDto expected = response(CODE);
            Page<ResponsibilityType> page = new PageImpl<>(List.of(entity), PageRequest.of(1, 5), 6);

            when(responsibilityTypeRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(page);
            when(responsibilityTypeMapper.toResponseDto(entity)).thenReturn(expected);

            Page<ResponsibilityTypeResponseDto> result =
                    responsibilityTypeService.findAll(filter, pageRequest);

            assertThat(result.getContent()).containsExactly(expected);
            assertThat(result.getTotalElements()).isEqualTo(6);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.getNumber()).isEqualTo(1);
            assertThat(result.getSize()).isEqualTo(5);

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(responsibilityTypeRepository)
                    .findAll(any(Specification.class), pageableCaptor.capture());

            Pageable pageable = pageableCaptor.getValue();
            assertThat(pageable.getPageNumber()).isEqualTo(1);
            assertThat(pageable.getPageSize()).isEqualTo(5);
            assertThat(pageable.getSort().getOrderFor("code")).isNotNull();
            assertThat(pageable.getSort().getOrderFor("code").getDirection()).isEqualTo(Sort.Direction.DESC);
        }

        @Test
        @DisplayName("returns an empty page without calling the mapper")
        void shouldReturnEmptyPage() {
            ResponsibilityTypeFilterDto filter = ResponsibilityTypeFilterDto.builder()
                    .code("missing")
                    .build();
            PageRequestDto pageRequest = PageRequestDto.builder().build();

            when(responsibilityTypeRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            Page<ResponsibilityTypeResponseDto> result =
                    responsibilityTypeService.findAll(filter, pageRequest);

            assertThat(result).isEmpty();
            assertThat(result.getTotalElements()).isZero();
            verifyNoInteractions(responsibilityTypeMapper);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("updates a responsibility type when the new code is unique")
        void shouldUpdateResponsibilityType() {
            ResponsibilityType entity = responsibilityType(CODE);
            ResponsibilityTypeUpdateRequestDto request = ResponsibilityTypeUpdateRequestDto.builder()
                    .code("REVIEWER")
                    .name("Reviewer")
                    .description("Reviews work")
                    .multipleAllowed(true)
                    .active(true)
                    .build();
            ResponsibilityTypeResponseDto expected = response("REVIEWER");

            when(responsibilityTypeRepository.findById(RESPONSIBILITY_TYPE_ID)).thenReturn(Optional.of(entity));
            when(responsibilityTypeRepository.existsByCode("REVIEWER")).thenReturn(false);
            when(responsibilityTypeRepository.save(entity)).thenReturn(entity);
            when(responsibilityTypeMapper.toResponseDto(entity)).thenReturn(expected);

            ResponsibilityTypeResponseDto result =
                    responsibilityTypeService.update(RESPONSIBILITY_TYPE_ID, request);

            assertThat(result).isSameAs(expected);
            verify(responsibilityTypeMapper).updateEntity(request, entity);
            verify(responsibilityTypeRepository).save(entity);
        }

        @Test
        @DisplayName("skips the uniqueness check when the code is unchanged ignoring case")
        void shouldSkipCheckForUnchangedCode() {
            ResponsibilityType entity = responsibilityType(CODE);
            ResponsibilityTypeUpdateRequestDto request = ResponsibilityTypeUpdateRequestDto.builder()
                    .code(CODE.toLowerCase())
                    .name(NAME)
                    .active(true)
                    .build();
            ResponsibilityTypeResponseDto expected = response(CODE);

            when(responsibilityTypeRepository.findById(RESPONSIBILITY_TYPE_ID)).thenReturn(Optional.of(entity));
            when(responsibilityTypeRepository.save(entity)).thenReturn(entity);
            when(responsibilityTypeMapper.toResponseDto(entity)).thenReturn(expected);

            assertThat(responsibilityTypeService.update(RESPONSIBILITY_TYPE_ID, request)).isSameAs(expected);

            verify(responsibilityTypeRepository, never()).existsByCode(anyString());
            verify(responsibilityTypeMapper).updateEntity(request, entity);
        }

        @Test
        @DisplayName("skips the uniqueness check when the code is null")
        void shouldSkipCheckForNullCode() {
            ResponsibilityType entity = responsibilityType(CODE);
            ResponsibilityTypeUpdateRequestDto request = ResponsibilityTypeUpdateRequestDto.builder()
                    .name(NAME)
                    .description("Updated description")
                    .active(true)
                    .build();
            ResponsibilityTypeResponseDto expected = response(CODE);

            when(responsibilityTypeRepository.findById(RESPONSIBILITY_TYPE_ID)).thenReturn(Optional.of(entity));
            when(responsibilityTypeRepository.save(entity)).thenReturn(entity);
            when(responsibilityTypeMapper.toResponseDto(entity)).thenReturn(expected);

            assertThat(responsibilityTypeService.update(RESPONSIBILITY_TYPE_ID, request)).isSameAs(expected);

            verify(responsibilityTypeRepository, never()).existsByCode(anyString());
        }

        @Test
        @DisplayName("skips the uniqueness check when the code is blank")
        void shouldSkipCheckForBlankCode() {
            ResponsibilityType entity = responsibilityType(CODE);
            ResponsibilityTypeUpdateRequestDto request = ResponsibilityTypeUpdateRequestDto.builder()
                    .code("   ")
                    .active(true)
                    .build();
            ResponsibilityTypeResponseDto expected = response(CODE);

            when(responsibilityTypeRepository.findById(RESPONSIBILITY_TYPE_ID)).thenReturn(Optional.of(entity));
            when(responsibilityTypeRepository.save(entity)).thenReturn(entity);
            when(responsibilityTypeMapper.toResponseDto(entity)).thenReturn(expected);

            assertThat(responsibilityTypeService.update(RESPONSIBILITY_TYPE_ID, request)).isSameAs(expected);

            verify(responsibilityTypeRepository, never()).existsByCode(anyString());
        }

        @Test
        @DisplayName("throws when the responsibility type does not exist")
        void shouldThrowWhenMissing() {
            ResponsibilityTypeUpdateRequestDto request = ResponsibilityTypeUpdateRequestDto.builder()
                    .code("REVIEWER")
                    .active(true)
                    .build();

            when(responsibilityTypeRepository.findById(RESPONSIBILITY_TYPE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> responsibilityTypeService.update(RESPONSIBILITY_TYPE_ID, request))
                    .isInstanceOf(ResponsibilityTypeNotFoundException.class);

            verifyNoInteractions(responsibilityTypeMapper);
            verify(responsibilityTypeRepository, never()).save(any(ResponsibilityType.class));
        }

        @Test
        @DisplayName("throws when the new code is already taken")
        void shouldThrowForDuplicateCode() {
            ResponsibilityType entity = responsibilityType(CODE);
            ResponsibilityTypeUpdateRequestDto request = ResponsibilityTypeUpdateRequestDto.builder()
                    .code("REVIEWER")
                    .active(true)
                    .build();

            when(responsibilityTypeRepository.findById(RESPONSIBILITY_TYPE_ID)).thenReturn(Optional.of(entity));
            when(responsibilityTypeRepository.existsByCode("REVIEWER")).thenReturn(true);

            assertThatThrownBy(() -> responsibilityTypeService.update(RESPONSIBILITY_TYPE_ID, request))
                    .isInstanceOf(ResponsibilityTypeAlreadyExistsException.class)
                    .hasMessageContaining("REVIEWER");

            verify(responsibilityTypeMapper, never()).updateEntity(any(), any());
            verify(responsibilityTypeRepository, never()).save(any(ResponsibilityType.class));
        }
    }

    @Nested
    @DisplayName("deactivate")
    class Deactivate {

        @Test
        @DisplayName("deactivates an active responsibility type")
        void shouldDeactivate() {
            ResponsibilityType entity = responsibilityType(CODE);

            when(responsibilityTypeRepository.findById(RESPONSIBILITY_TYPE_ID)).thenReturn(Optional.of(entity));

            responsibilityTypeService.deactivate(RESPONSIBILITY_TYPE_ID);

            assertThat(entity.getActive()).isFalse();
            verify(responsibilityTypeRepository).save(entity);
        }

        @Test
        @DisplayName("leaves an already inactive responsibility type untouched")
        void shouldSkipAlreadyInactive() {
            ResponsibilityType entity = responsibilityType(CODE);
            entity.setActive(false);

            when(responsibilityTypeRepository.findById(RESPONSIBILITY_TYPE_ID)).thenReturn(Optional.of(entity));

            responsibilityTypeService.deactivate(RESPONSIBILITY_TYPE_ID);

            assertThat(entity.getActive()).isFalse();
            verify(responsibilityTypeRepository, never()).save(any(ResponsibilityType.class));
        }

        @Test
        @DisplayName("throws when the responsibility type does not exist")
        void shouldThrowWhenMissing() {
            when(responsibilityTypeRepository.findById(RESPONSIBILITY_TYPE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> responsibilityTypeService.deactivate(RESPONSIBILITY_TYPE_ID))
                    .isInstanceOf(ResponsibilityTypeNotFoundException.class);

            verify(responsibilityTypeRepository, never()).save(any(ResponsibilityType.class));
        }
    }

    @Nested
    @DisplayName("activate")
    class Activate {

        @Test
        @DisplayName("activates an inactive responsibility type")
        void shouldActivate() {
            ResponsibilityType entity = responsibilityType(CODE);
            entity.setActive(false);

            when(responsibilityTypeRepository.findById(RESPONSIBILITY_TYPE_ID)).thenReturn(Optional.of(entity));

            responsibilityTypeService.activate(RESPONSIBILITY_TYPE_ID);

            assertThat(entity.getActive()).isTrue();
            verify(responsibilityTypeRepository).save(entity);
        }

        @Test
        @DisplayName("leaves an already active responsibility type untouched")
        void shouldSkipAlreadyActive() {
            ResponsibilityType entity = responsibilityType(CODE);

            when(responsibilityTypeRepository.findById(RESPONSIBILITY_TYPE_ID)).thenReturn(Optional.of(entity));

            responsibilityTypeService.activate(RESPONSIBILITY_TYPE_ID);

            assertThat(entity.getActive()).isTrue();
            verify(responsibilityTypeRepository, never()).save(any(ResponsibilityType.class));
        }

        @Test
        @DisplayName("throws when the responsibility type does not exist")
        void shouldThrowWhenMissing() {
            when(responsibilityTypeRepository.findById(RESPONSIBILITY_TYPE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> responsibilityTypeService.activate(RESPONSIBILITY_TYPE_ID))
                    .isInstanceOf(ResponsibilityTypeNotFoundException.class);

            verify(responsibilityTypeRepository, never()).save(any(ResponsibilityType.class));
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("deletes a responsibility type that is not assigned")
        void shouldDelete() {
            ResponsibilityType entity = responsibilityType(CODE);

            when(responsibilityTypeRepository.findById(RESPONSIBILITY_TYPE_ID)).thenReturn(Optional.of(entity));
            when(employeeResponsibilityRepository.existsByResponsibilityTypeId(RESPONSIBILITY_TYPE_ID))
                    .thenReturn(false);

            responsibilityTypeService.delete(RESPONSIBILITY_TYPE_ID);

            verify(employeeResponsibilityRepository).existsByResponsibilityTypeId(RESPONSIBILITY_TYPE_ID);
            verify(responsibilityTypeRepository).delete(entity);
        }

        @Test
        @DisplayName("rejects deletion when the responsibility type is assigned")
        void shouldRejectWhenInUse() {
            ResponsibilityType entity = responsibilityType(CODE);

            when(responsibilityTypeRepository.findById(RESPONSIBILITY_TYPE_ID)).thenReturn(Optional.of(entity));
            when(employeeResponsibilityRepository.existsByResponsibilityTypeId(RESPONSIBILITY_TYPE_ID))
                    .thenReturn(true);

            assertThatThrownBy(() -> responsibilityTypeService.delete(RESPONSIBILITY_TYPE_ID))
                    .isInstanceOf(ResponsibilityTypeInUseException.class)
                    .hasMessageContaining(RESPONSIBILITY_TYPE_ID.toString());

            verify(responsibilityTypeRepository, never()).delete(any(ResponsibilityType.class));
        }

        @Test
        @DisplayName("throws when the responsibility type does not exist")
        void shouldThrowWhenMissing() {
            when(responsibilityTypeRepository.findById(RESPONSIBILITY_TYPE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> responsibilityTypeService.delete(RESPONSIBILITY_TYPE_ID))
                    .isInstanceOf(ResponsibilityTypeNotFoundException.class);

            verifyNoInteractions(employeeResponsibilityRepository);
            verify(responsibilityTypeRepository, never()).delete(any(ResponsibilityType.class));
        }
    }

    private ResponsibilityType responsibilityType(String code) {
        return ResponsibilityType.builder()
                .id(RESPONSIBILITY_TYPE_ID)
                .code(code)
                .name(NAME)
                .description(DESCRIPTION)
                .multipleAllowed(false)
                .active(true)
                .build();
    }

    private ResponsibilityTypeResponseDto response(String code) {
        return ResponsibilityTypeResponseDto.builder()
                .id(RESPONSIBILITY_TYPE_ID)
                .code(code)
                .name(NAME)
                .description(DESCRIPTION)
                .multipleAllowed(false)
                .active(true)
                .build();
    }
}
