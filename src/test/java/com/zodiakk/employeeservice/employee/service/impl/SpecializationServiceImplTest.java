package com.zodiakk.employeeservice.employee.service.impl;

import com.zodiakk.employeeservice.common.dto.PageRequestDto;
import com.zodiakk.employeeservice.common.dto.SortDirection;
import com.zodiakk.employeeservice.employee.dto.request.create.SpecializationCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.filter.SpecializationFilterDto;
import com.zodiakk.employeeservice.employee.dto.request.update.SpecializationUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.SpecializationResponseDto;
import com.zodiakk.employeeservice.employee.entity.Specialization;
import com.zodiakk.employeeservice.employee.exception.specialization.SpecializationAlreadyExistsException;
import com.zodiakk.employeeservice.employee.exception.specialization.SpecializationInUseException;
import com.zodiakk.employeeservice.employee.exception.specialization.SpecializationNotFoundException;
import com.zodiakk.employeeservice.employee.mapper.SpecializationMapper;
import com.zodiakk.employeeservice.employee.repository.EmployeeRepository;
import com.zodiakk.employeeservice.employee.repository.SpecializationRepository;
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
class SpecializationServiceImplTest {

    private static final UUID SPECIALIZATION_ID =
            UUID.fromString("33333333-3333-3333-3333-333333333333");

    private static final String NAME = "Backend Developer";

    private static final String DESCRIPTION = "Server-side development";

    @Mock
    private SpecializationRepository specializationRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private SpecializationMapper specializationMapper;

    @InjectMocks
    private SpecializationServiceImpl specializationService;

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("saves a new active specialization when the name is unique")
        void shouldCreateSpecialization() {
            SpecializationCreateRequestDto request = SpecializationCreateRequestDto.builder()
                    .name(NAME)
                    .description(DESCRIPTION)
                    .build();
            Specialization entity = Specialization.builder()
                    .name(NAME)
                    .description(DESCRIPTION)
                    .build();
            SpecializationResponseDto expected = response(NAME);

            when(specializationRepository.existsByNameIgnoreCase(NAME)).thenReturn(false);
            when(specializationMapper.toEntity(request)).thenReturn(entity);
            when(specializationRepository.save(entity)).thenReturn(entity);
            when(specializationMapper.toResponseDto(entity)).thenReturn(expected);

            SpecializationResponseDto result = specializationService.create(request);

            assertThat(result).isSameAs(expected);

            ArgumentCaptor<Specialization> captor = ArgumentCaptor.forClass(Specialization.class);
            verify(specializationRepository).save(captor.capture());

            Specialization saved = captor.getValue();
            assertThat(saved.getActive()).isTrue();
            assertThat(saved.getName()).isEqualTo(NAME);
            verify(specializationMapper).toEntity(request);
            verify(specializationMapper).toResponseDto(entity);
        }

        @Test
        @DisplayName("rejects a duplicate name")
        void shouldRejectDuplicateName() {
            SpecializationCreateRequestDto request = SpecializationCreateRequestDto.builder()
                    .name(NAME)
                    .description(DESCRIPTION)
                    .build();

            when(specializationRepository.existsByNameIgnoreCase(NAME)).thenReturn(true);

            assertThatThrownBy(() -> specializationService.create(request))
                    .isInstanceOf(SpecializationAlreadyExistsException.class)
                    .hasMessageContaining(NAME);

            verify(specializationRepository, never()).save(any(Specialization.class));
            verifyNoInteractions(specializationMapper);
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("returns the mapped specialization when it exists")
        void shouldReturnSpecialization() {
            Specialization entity = specialization(NAME);
            SpecializationResponseDto expected = response(NAME);

            when(specializationRepository.findById(SPECIALIZATION_ID)).thenReturn(Optional.of(entity));
            when(specializationMapper.toResponseDto(entity)).thenReturn(expected);

            assertThat(specializationService.getById(SPECIALIZATION_ID)).isSameAs(expected);
        }

        @Test
        @DisplayName("throws when the specialization does not exist")
        void shouldThrowWhenMissing() {
            when(specializationRepository.findById(SPECIALIZATION_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> specializationService.getById(SPECIALIZATION_ID))
                    .isInstanceOf(SpecializationNotFoundException.class)
                    .hasMessageContaining(SPECIALIZATION_ID.toString());

            verifyNoInteractions(specializationMapper);
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("maps a filtered page and preserves paging metadata")
        void shouldReturnMappedPage() {
            SpecializationFilterDto filter = SpecializationFilterDto.builder()
                    .name("backend")
                    .active(true)
                    .description("server")
                    .build();
            PageRequestDto pageRequest = PageRequestDto.builder()
                    .page(1)
                    .size(5)
                    .sortBy("name")
                    .sortDirection(SortDirection.DESC)
                    .build();

            Specialization entity = specialization(NAME);
            SpecializationResponseDto expected = response(NAME);
            Page<Specialization> page = new PageImpl<>(List.of(entity), PageRequest.of(1, 5), 6);

            when(specializationRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(page);
            when(specializationMapper.toResponseDto(entity)).thenReturn(expected);

            Page<SpecializationResponseDto> result = specializationService.findAll(filter, pageRequest);

            assertThat(result.getContent()).containsExactly(expected);
            assertThat(result.getTotalElements()).isEqualTo(6);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.getNumber()).isEqualTo(1);
            assertThat(result.getSize()).isEqualTo(5);

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(specializationRepository).findAll(any(Specification.class), pageableCaptor.capture());

            Pageable pageable = pageableCaptor.getValue();
            assertThat(pageable.getPageNumber()).isEqualTo(1);
            assertThat(pageable.getPageSize()).isEqualTo(5);
            assertThat(pageable.getSort().getOrderFor("name").getDirection()).isEqualTo(Sort.Direction.DESC);
        }

        @Test
        @DisplayName("returns an empty page without calling the mapper")
        void shouldReturnEmptyPage() {
            SpecializationFilterDto filter = SpecializationFilterDto.builder().name("missing").build();
            PageRequestDto pageRequest = PageRequestDto.builder().build();

            when(specializationRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            Page<SpecializationResponseDto> result = specializationService.findAll(filter, pageRequest);

            assertThat(result).isEmpty();
            assertThat(result.getTotalElements()).isZero();
            verifyNoInteractions(specializationMapper);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("updates a specialization when the new name is unique")
        void shouldUpdateSpecialization() {
            Specialization entity = specialization(NAME);
            SpecializationUpdateRequestDto request = SpecializationUpdateRequestDto.builder()
                    .name("Fullstack Developer")
                    .description("Client and server development")
                    .active(true)
                    .build();
            SpecializationResponseDto expected = response("Fullstack Developer");

            when(specializationRepository.findById(SPECIALIZATION_ID)).thenReturn(Optional.of(entity));
            when(specializationRepository.existsByNameIgnoreCase("Fullstack Developer")).thenReturn(false);
            when(specializationRepository.save(entity)).thenReturn(entity);
            when(specializationMapper.toResponseDto(entity)).thenReturn(expected);

            SpecializationResponseDto result = specializationService.update(SPECIALIZATION_ID, request);

            assertThat(result).isSameAs(expected);
            verify(specializationMapper).updateEntity(request, entity);
            verify(specializationRepository).save(entity);
        }

        @Test
        @DisplayName("skips the uniqueness check when the name is unchanged ignoring case")
        void shouldSkipCheckForUnchangedName() {
            Specialization entity = specialization(NAME);
            SpecializationUpdateRequestDto request = SpecializationUpdateRequestDto.builder()
                    .name(NAME.toLowerCase())
                    .active(true)
                    .build();
            SpecializationResponseDto expected = response(NAME);

            when(specializationRepository.findById(SPECIALIZATION_ID)).thenReturn(Optional.of(entity));
            when(specializationRepository.save(entity)).thenReturn(entity);
            when(specializationMapper.toResponseDto(entity)).thenReturn(expected);

            assertThat(specializationService.update(SPECIALIZATION_ID, request)).isSameAs(expected);

            verify(specializationRepository, never()).existsByNameIgnoreCase(anyString());
            verify(specializationMapper).updateEntity(request, entity);
        }

        @Test
        @DisplayName("skips the uniqueness check when the name is null")
        void shouldSkipCheckForNullName() {
            Specialization entity = specialization(NAME);
            SpecializationUpdateRequestDto request = SpecializationUpdateRequestDto.builder()
                    .description("Updated description")
                    .build();
            SpecializationResponseDto expected = response(NAME);

            when(specializationRepository.findById(SPECIALIZATION_ID)).thenReturn(Optional.of(entity));
            when(specializationRepository.save(entity)).thenReturn(entity);
            when(specializationMapper.toResponseDto(entity)).thenReturn(expected);

            assertThat(specializationService.update(SPECIALIZATION_ID, request)).isSameAs(expected);

            verify(specializationRepository, never()).existsByNameIgnoreCase(anyString());
        }

        @Test
        @DisplayName("skips the uniqueness check when the name is blank")
        void shouldSkipCheckForBlankName() {
            Specialization entity = specialization(NAME);
            SpecializationUpdateRequestDto request = SpecializationUpdateRequestDto.builder()
                    .name("  ")
                    .build();
            SpecializationResponseDto expected = response(NAME);

            when(specializationRepository.findById(SPECIALIZATION_ID)).thenReturn(Optional.of(entity));
            when(specializationRepository.save(entity)).thenReturn(entity);
            when(specializationMapper.toResponseDto(entity)).thenReturn(expected);

            assertThat(specializationService.update(SPECIALIZATION_ID, request)).isSameAs(expected);

            verify(specializationRepository, never()).existsByNameIgnoreCase(anyString());
        }

        @Test
        @DisplayName("throws when the specialization does not exist")
        void shouldThrowWhenMissing() {
            SpecializationUpdateRequestDto request = SpecializationUpdateRequestDto.builder()
                    .name("Fullstack Developer")
                    .build();

            when(specializationRepository.findById(SPECIALIZATION_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> specializationService.update(SPECIALIZATION_ID, request))
                    .isInstanceOf(SpecializationNotFoundException.class);

            verifyNoInteractions(specializationMapper);
            verify(specializationRepository, never()).save(any(Specialization.class));
        }

        @Test
        @DisplayName("throws when the new name is already taken")
        void shouldThrowForDuplicateName() {
            Specialization entity = specialization(NAME);
            SpecializationUpdateRequestDto request = SpecializationUpdateRequestDto.builder()
                    .name("Fullstack Developer")
                    .build();

            when(specializationRepository.findById(SPECIALIZATION_ID)).thenReturn(Optional.of(entity));
            when(specializationRepository.existsByNameIgnoreCase("Fullstack Developer")).thenReturn(true);

            assertThatThrownBy(() -> specializationService.update(SPECIALIZATION_ID, request))
                    .isInstanceOf(SpecializationAlreadyExistsException.class)
                    .hasMessageContaining("Fullstack Developer");

            verify(specializationMapper, never()).updateEntity(any(), any());
            verify(specializationRepository, never()).save(any(Specialization.class));
        }
    }

    @Nested
    @DisplayName("deactivate")
    class Deactivate {

        @Test
        @DisplayName("deactivates an active specialization")
        void shouldDeactivate() {
            Specialization entity = specialization(NAME);

            when(specializationRepository.findById(SPECIALIZATION_ID)).thenReturn(Optional.of(entity));

            specializationService.deactivate(SPECIALIZATION_ID);

            assertThat(entity.getActive()).isFalse();
            verify(specializationRepository).save(entity);
        }

        @Test
        @DisplayName("leaves an already inactive specialization untouched")
        void shouldSkipAlreadyInactive() {
            Specialization entity = specialization(NAME);
            entity.setActive(false);

            when(specializationRepository.findById(SPECIALIZATION_ID)).thenReturn(Optional.of(entity));

            specializationService.deactivate(SPECIALIZATION_ID);

            assertThat(entity.getActive()).isFalse();
            verify(specializationRepository, never()).save(any(Specialization.class));
        }

        @Test
        @DisplayName("throws when the specialization does not exist")
        void shouldThrowWhenMissing() {
            when(specializationRepository.findById(SPECIALIZATION_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> specializationService.deactivate(SPECIALIZATION_ID))
                    .isInstanceOf(SpecializationNotFoundException.class);

            verify(specializationRepository, never()).save(any(Specialization.class));
        }
    }

    @Nested
    @DisplayName("activate")
    class Activate {

        @Test
        @DisplayName("activates an inactive specialization")
        void shouldActivate() {
            Specialization entity = specialization(NAME);
            entity.setActive(false);

            when(specializationRepository.findById(SPECIALIZATION_ID)).thenReturn(Optional.of(entity));

            specializationService.activate(SPECIALIZATION_ID);

            assertThat(entity.getActive()).isTrue();
            verify(specializationRepository).save(entity);
        }

        @Test
        @DisplayName("leaves an already active specialization untouched")
        void shouldSkipAlreadyActive() {
            Specialization entity = specialization(NAME);

            when(specializationRepository.findById(SPECIALIZATION_ID)).thenReturn(Optional.of(entity));

            specializationService.activate(SPECIALIZATION_ID);

            assertThat(entity.getActive()).isTrue();
            verify(specializationRepository, never()).save(any(Specialization.class));
        }

        @Test
        @DisplayName("throws when the specialization does not exist")
        void shouldThrowWhenMissing() {
            when(specializationRepository.findById(SPECIALIZATION_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> specializationService.activate(SPECIALIZATION_ID))
                    .isInstanceOf(SpecializationNotFoundException.class);

            verify(specializationRepository, never()).save(any(Specialization.class));
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("deletes a specialization that no employee references")
        void shouldDelete() {
            Specialization entity = specialization(NAME);

            when(specializationRepository.findById(SPECIALIZATION_ID)).thenReturn(Optional.of(entity));
            when(employeeRepository.countBySpecializationId(SPECIALIZATION_ID)).thenReturn(0L);

            specializationService.delete(SPECIALIZATION_ID);

            verify(employeeRepository).countBySpecializationId(SPECIALIZATION_ID);
            verify(specializationRepository).delete(entity);
        }

        @Test
        @DisplayName("rejects deletion when employees reference the specialization")
        void shouldRejectWhenInUse() {
            Specialization entity = specialization(NAME);

            when(specializationRepository.findById(SPECIALIZATION_ID)).thenReturn(Optional.of(entity));
            when(employeeRepository.countBySpecializationId(SPECIALIZATION_ID)).thenReturn(4L);

            assertThatThrownBy(() -> specializationService.delete(SPECIALIZATION_ID))
                    .isInstanceOf(SpecializationInUseException.class)
                    .hasMessageContaining(SPECIALIZATION_ID.toString());

            verify(specializationRepository, never()).delete(any(Specialization.class));
        }

        @Test
        @DisplayName("throws when the specialization does not exist")
        void shouldThrowWhenMissing() {
            when(specializationRepository.findById(SPECIALIZATION_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> specializationService.delete(SPECIALIZATION_ID))
                    .isInstanceOf(SpecializationNotFoundException.class);

            verifyNoInteractions(employeeRepository);
            verify(specializationRepository, never()).delete(any(Specialization.class));
        }
    }

    private Specialization specialization(String name) {
        return Specialization.builder()
                .id(SPECIALIZATION_ID)
                .name(name)
                .description(DESCRIPTION)
                .active(true)
                .build();
    }

    private SpecializationResponseDto response(String name) {
        return SpecializationResponseDto.builder()
                .id(SPECIALIZATION_ID)
                .name(name)
                .description(DESCRIPTION)
                .active(true)
                .build();
    }
}
