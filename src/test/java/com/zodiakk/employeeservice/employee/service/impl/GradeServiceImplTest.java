package com.zodiakk.employeeservice.employee.service.impl;

import com.zodiakk.employeeservice.common.dto.PageRequestDto;
import com.zodiakk.employeeservice.common.dto.SortDirection;
import com.zodiakk.employeeservice.employee.dto.request.create.GradeCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.filter.GradeFilterDto;
import com.zodiakk.employeeservice.employee.dto.request.update.GradeUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.GradeResponseDto;
import com.zodiakk.employeeservice.employee.entity.Grade;
import com.zodiakk.employeeservice.employee.exception.grade.GradeAlreadyExistsException;
import com.zodiakk.employeeservice.employee.exception.grade.GradeInUseException;
import com.zodiakk.employeeservice.employee.exception.grade.GradeNotFoundException;
import com.zodiakk.employeeservice.employee.mapper.GradeMapper;
import com.zodiakk.employeeservice.employee.repository.EmployeeRepository;
import com.zodiakk.employeeservice.employee.repository.GradeRepository;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GradeServiceImplTest {

    private static final UUID GRADE_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");

    private static final String NAME = "Senior Engineer";

    private static final int LEVEL = 5;

    private static final String DESCRIPTION = "Senior engineering grade";

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private GradeMapper gradeMapper;

    @InjectMocks
    private GradeServiceImpl gradeService;

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("saves a new active grade when name and level are unique")
        void shouldCreateGrade() {
            GradeCreateRequestDto request = GradeCreateRequestDto.builder()
                    .name(NAME)
                    .level(LEVEL)
                    .description(DESCRIPTION)
                    .build();
            Grade entity = Grade.builder()
                    .name(NAME)
                    .level(LEVEL)
                    .description(DESCRIPTION)
                    .build();
            GradeResponseDto expected = response(NAME, LEVEL);

            when(gradeRepository.existsByNameIgnoreCase(NAME)).thenReturn(false);
            when(gradeRepository.existsByLevel(LEVEL)).thenReturn(false);
            when(gradeMapper.toEntity(request)).thenReturn(entity);
            when(gradeRepository.save(entity)).thenReturn(entity);
            when(gradeMapper.toResponseDto(entity)).thenReturn(expected);

            GradeResponseDto result = gradeService.create(request);

            assertThat(result).isSameAs(expected);

            ArgumentCaptor<Grade> captor = ArgumentCaptor.forClass(Grade.class);
            verify(gradeRepository).save(captor.capture());

            Grade saved = captor.getValue();
            assertThat(saved.getActive()).isTrue();
            assertThat(saved.getName()).isEqualTo(NAME);
            assertThat(saved.getLevel()).isEqualTo(LEVEL);
            verify(gradeMapper).toEntity(request);
            verify(gradeMapper).toResponseDto(entity);
        }

        @Test
        @DisplayName("rejects a duplicate name without checking the level")
        void shouldRejectDuplicateName() {
            GradeCreateRequestDto request = GradeCreateRequestDto.builder()
                    .name(NAME)
                    .level(LEVEL)
                    .build();

            when(gradeRepository.existsByNameIgnoreCase(NAME)).thenReturn(true);

            assertThatThrownBy(() -> gradeService.create(request))
                    .isInstanceOf(GradeAlreadyExistsException.class);

            verify(gradeRepository, never()).existsByLevel(anyInt());
            verify(gradeRepository, never()).save(any(Grade.class));
            verifyNoInteractions(gradeMapper);
        }

        @Test
        @DisplayName("rejects a duplicate level")
        void shouldRejectDuplicateLevel() {
            GradeCreateRequestDto request = GradeCreateRequestDto.builder()
                    .name(NAME)
                    .level(LEVEL)
                    .build();

            when(gradeRepository.existsByNameIgnoreCase(NAME)).thenReturn(false);
            when(gradeRepository.existsByLevel(LEVEL)).thenReturn(true);

            assertThatThrownBy(() -> gradeService.create(request))
                    .isInstanceOf(GradeAlreadyExistsException.class);

            verify(gradeRepository, never()).save(any(Grade.class));
            verifyNoInteractions(gradeMapper);
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("returns the mapped grade when it exists")
        void shouldReturnGrade() {
            Grade entity = grade(NAME, LEVEL);
            GradeResponseDto expected = response(NAME, LEVEL);

            when(gradeRepository.findById(GRADE_ID)).thenReturn(Optional.of(entity));
            when(gradeMapper.toResponseDto(entity)).thenReturn(expected);

            assertThat(gradeService.getById(GRADE_ID)).isSameAs(expected);
        }

        @Test
        @DisplayName("throws when the grade does not exist")
        void shouldThrowWhenMissing() {
            when(gradeRepository.findById(GRADE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> gradeService.getById(GRADE_ID))
                    .isInstanceOf(GradeNotFoundException.class)
                    .hasMessageContaining(GRADE_ID.toString());

            verifyNoInteractions(gradeMapper);
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("maps a filtered page and preserves paging metadata")
        void shouldReturnMappedPage() {
            GradeFilterDto filter = GradeFilterDto.builder()
                    .name("senior")
                    .level(LEVEL)
                    .active(true)
                    .description("engineering")
                    .build();
            PageRequestDto pageRequest = PageRequestDto.builder()
                    .page(1)
                    .size(5)
                    .sortBy("level")
                    .sortDirection(SortDirection.DESC)
                    .build();

            Grade entity = grade(NAME, LEVEL);
            GradeResponseDto expected = response(NAME, LEVEL);
            Page<Grade> page = new PageImpl<>(List.of(entity), PageRequest.of(1, 5), 6);

            when(gradeRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
            when(gradeMapper.toResponseDto(entity)).thenReturn(expected);

            Page<GradeResponseDto> result = gradeService.findAll(filter, pageRequest);

            assertThat(result.getContent()).containsExactly(expected);
            assertThat(result.getTotalElements()).isEqualTo(6);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.getNumber()).isEqualTo(1);
            assertThat(result.getSize()).isEqualTo(5);

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(gradeRepository).findAll(any(Specification.class), pageableCaptor.capture());

            Pageable pageable = pageableCaptor.getValue();
            assertThat(pageable.getPageNumber()).isEqualTo(1);
            assertThat(pageable.getPageSize()).isEqualTo(5);
            assertThat(pageable.getSort().getOrderFor("level")).isNotNull();
            assertThat(pageable.getSort().getOrderFor("level").getDirection()).isEqualTo(Sort.Direction.DESC);
        }

        @Test
        @DisplayName("returns an empty page without calling the mapper")
        void shouldReturnEmptyPage() {
            GradeFilterDto filter = GradeFilterDto.builder().name("missing").build();
            PageRequestDto pageRequest = PageRequestDto.builder().build();

            when(gradeRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            Page<GradeResponseDto> result = gradeService.findAll(filter, pageRequest);

            assertThat(result).isEmpty();
            assertThat(result.getTotalElements()).isZero();
            verifyNoInteractions(gradeMapper);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("checks both name and level when both change")
        void shouldUpdateNameAndLevel() {
            Grade entity = grade(NAME, LEVEL);
            GradeUpdateRequestDto request = GradeUpdateRequestDto.builder()
                    .name("Lead Engineer")
                    .level(6)
                    .description("Lead engineering grade")
                    .active(true)
                    .build();
            GradeResponseDto expected = response("Lead Engineer", 6);

            when(gradeRepository.findById(GRADE_ID)).thenReturn(Optional.of(entity));
            when(gradeRepository.existsByNameIgnoreCase("Lead Engineer")).thenReturn(false);
            when(gradeRepository.existsByLevel(6)).thenReturn(false);
            when(gradeRepository.save(entity)).thenReturn(entity);
            when(gradeMapper.toResponseDto(entity)).thenReturn(expected);

            GradeResponseDto result = gradeService.update(GRADE_ID, request);

            assertThat(result).isSameAs(expected);
            verify(gradeMapper).updateEntity(request, entity);
            verify(gradeRepository).existsByNameIgnoreCase("Lead Engineer");
            verify(gradeRepository).existsByLevel(6);
            verify(gradeRepository).save(entity);
        }

        @Test
        @DisplayName("skips uniqueness checks when name and level are unchanged")
        void shouldSkipChecksWhenNothingChanged() {
            Grade entity = grade(NAME, LEVEL);
            GradeUpdateRequestDto request = GradeUpdateRequestDto.builder()
                    .name("senior engineer")
                    .level(LEVEL)
                    .description("Updated description")
                    .active(true)
                    .build();
            GradeResponseDto expected = response(NAME, LEVEL);

            when(gradeRepository.findById(GRADE_ID)).thenReturn(Optional.of(entity));
            when(gradeRepository.save(entity)).thenReturn(entity);
            when(gradeMapper.toResponseDto(entity)).thenReturn(expected);

            assertThat(gradeService.update(GRADE_ID, request)).isSameAs(expected);

            verify(gradeRepository, never()).existsByNameIgnoreCase(anyString());
            verify(gradeRepository, never()).existsByLevel(anyInt());
            verify(gradeMapper).updateEntity(request, entity);
        }

        @Test
        @DisplayName("checks only the level when only the level changes")
        void shouldCheckOnlyLevel() {
            Grade entity = grade(NAME, LEVEL);
            GradeUpdateRequestDto request = GradeUpdateRequestDto.builder()
                    .level(6)
                    .active(true)
                    .build();
            GradeResponseDto expected = response(NAME, 6);

            when(gradeRepository.findById(GRADE_ID)).thenReturn(Optional.of(entity));
            when(gradeRepository.existsByLevel(6)).thenReturn(false);
            when(gradeRepository.save(entity)).thenReturn(entity);
            when(gradeMapper.toResponseDto(entity)).thenReturn(expected);

            assertThat(gradeService.update(GRADE_ID, request)).isSameAs(expected);

            verify(gradeRepository, never()).existsByNameIgnoreCase(anyString());
            verify(gradeRepository).existsByLevel(6);
        }

        @Test
        @DisplayName("checks only the name when only the name changes")
        void shouldCheckOnlyName() {
            Grade entity = grade(NAME, LEVEL);
            GradeUpdateRequestDto request = GradeUpdateRequestDto.builder()
                    .name("Lead Engineer")
                    .active(true)
                    .build();
            GradeResponseDto expected = response("Lead Engineer", LEVEL);

            when(gradeRepository.findById(GRADE_ID)).thenReturn(Optional.of(entity));
            when(gradeRepository.existsByNameIgnoreCase("Lead Engineer")).thenReturn(false);
            when(gradeRepository.save(entity)).thenReturn(entity);
            when(gradeMapper.toResponseDto(entity)).thenReturn(expected);

            assertThat(gradeService.update(GRADE_ID, request)).isSameAs(expected);

            verify(gradeRepository).existsByNameIgnoreCase("Lead Engineer");
            verify(gradeRepository, never()).existsByLevel(anyInt());
        }

        @Test
        @DisplayName("throws when the grade does not exist")
        void shouldThrowWhenMissing() {
            GradeUpdateRequestDto request = GradeUpdateRequestDto.builder()
                    .name("Lead Engineer")
                    .active(true)
                    .build();

            when(gradeRepository.findById(GRADE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> gradeService.update(GRADE_ID, request))
                    .isInstanceOf(GradeNotFoundException.class);

            verifyNoInteractions(gradeMapper);
            verify(gradeRepository, never()).save(any(Grade.class));
        }

        @Test
        @DisplayName("throws when the new name is already taken")
        void shouldThrowForDuplicateName() {
            Grade entity = grade(NAME, LEVEL);
            GradeUpdateRequestDto request = GradeUpdateRequestDto.builder()
                    .name("Lead Engineer")
                    .active(true)
                    .build();

            when(gradeRepository.findById(GRADE_ID)).thenReturn(Optional.of(entity));
            when(gradeRepository.existsByNameIgnoreCase("Lead Engineer")).thenReturn(true);

            assertThatThrownBy(() -> gradeService.update(GRADE_ID, request))
                    .isInstanceOf(GradeAlreadyExistsException.class);

            verify(gradeRepository, never()).existsByLevel(anyInt());
            verify(gradeMapper, never()).updateEntity(any(), any());
            verify(gradeRepository, never()).save(any(Grade.class));
        }

        @Test
        @DisplayName("throws when the new level is already taken")
        void shouldThrowForDuplicateLevel() {
            Grade entity = grade(NAME, LEVEL);
            GradeUpdateRequestDto request = GradeUpdateRequestDto.builder()
                    .level(6)
                    .active(true)
                    .build();

            when(gradeRepository.findById(GRADE_ID)).thenReturn(Optional.of(entity));
            when(gradeRepository.existsByLevel(6)).thenReturn(true);

            assertThatThrownBy(() -> gradeService.update(GRADE_ID, request))
                    .isInstanceOf(GradeAlreadyExistsException.class);

            verify(gradeRepository, never()).existsByNameIgnoreCase(anyString());
            verify(gradeMapper, never()).updateEntity(any(), any());
            verify(gradeRepository, never()).save(any(Grade.class));
        }
    }

    @Nested
    @DisplayName("deactivate")
    class Deactivate {

        @Test
        @DisplayName("deactivates an active grade")
        void shouldDeactivate() {
            Grade entity = grade(NAME, LEVEL);

            when(gradeRepository.findById(GRADE_ID)).thenReturn(Optional.of(entity));

            gradeService.deactivate(GRADE_ID);

            assertThat(entity.getActive()).isFalse();
            verify(gradeRepository).save(entity);
        }

        @Test
        @DisplayName("leaves an already inactive grade untouched")
        void shouldSkipAlreadyInactive() {
            Grade entity = grade(NAME, LEVEL);
            entity.setActive(false);

            when(gradeRepository.findById(GRADE_ID)).thenReturn(Optional.of(entity));

            gradeService.deactivate(GRADE_ID);

            assertThat(entity.getActive()).isFalse();
            verify(gradeRepository, never()).save(any(Grade.class));
        }

        @Test
        @DisplayName("throws when the grade does not exist")
        void shouldThrowWhenMissing() {
            when(gradeRepository.findById(GRADE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> gradeService.deactivate(GRADE_ID))
                    .isInstanceOf(GradeNotFoundException.class);

            verify(gradeRepository, never()).save(any(Grade.class));
        }
    }

    @Nested
    @DisplayName("activate")
    class Activate {

        @Test
        @DisplayName("activates an inactive grade")
        void shouldActivate() {
            Grade entity = grade(NAME, LEVEL);
            entity.setActive(false);

            when(gradeRepository.findById(GRADE_ID)).thenReturn(Optional.of(entity));

            gradeService.activate(GRADE_ID);

            assertThat(entity.getActive()).isTrue();
            verify(gradeRepository).save(entity);
        }

        @Test
        @DisplayName("leaves an already active grade untouched")
        void shouldSkipAlreadyActive() {
            Grade entity = grade(NAME, LEVEL);

            when(gradeRepository.findById(GRADE_ID)).thenReturn(Optional.of(entity));

            gradeService.activate(GRADE_ID);

            assertThat(entity.getActive()).isTrue();
            verify(gradeRepository, never()).save(any(Grade.class));
        }

        @Test
        @DisplayName("throws when the grade does not exist")
        void shouldThrowWhenMissing() {
            when(gradeRepository.findById(GRADE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> gradeService.activate(GRADE_ID))
                    .isInstanceOf(GradeNotFoundException.class);

            verify(gradeRepository, never()).save(any(Grade.class));
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("deletes a grade that no employee references")
        void shouldDelete() {
            Grade entity = grade(NAME, LEVEL);

            when(gradeRepository.findById(GRADE_ID)).thenReturn(Optional.of(entity));
            when(employeeRepository.countByGradeId(GRADE_ID)).thenReturn(0L);

            gradeService.delete(GRADE_ID);

            verify(employeeRepository).countByGradeId(GRADE_ID);
            verify(gradeRepository).delete(entity);
        }

        @Test
        @DisplayName("rejects deletion when employees reference the grade")
        void shouldRejectWhenInUse() {
            Grade entity = grade(NAME, LEVEL);

            when(gradeRepository.findById(GRADE_ID)).thenReturn(Optional.of(entity));
            when(employeeRepository.countByGradeId(GRADE_ID)).thenReturn(5L);

            assertThatThrownBy(() -> gradeService.delete(GRADE_ID))
                    .isInstanceOf(GradeInUseException.class)
                    .hasMessageContaining(GRADE_ID.toString());

            verify(gradeRepository, never()).delete(any(Grade.class));
        }

        @Test
        @DisplayName("throws when the grade does not exist")
        void shouldThrowWhenMissing() {
            when(gradeRepository.findById(GRADE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> gradeService.delete(GRADE_ID))
                    .isInstanceOf(GradeNotFoundException.class);

            verifyNoInteractions(employeeRepository);
            verify(gradeRepository, never()).delete(any(Grade.class));
        }
    }

    private Grade grade(String name, int level) {
        return Grade.builder()
                .id(GRADE_ID)
                .name(name)
                .level(level)
                .description(DESCRIPTION)
                .active(true)
                .build();
    }

    private GradeResponseDto response(String name, int level) {
        return GradeResponseDto.builder()
                .id(GRADE_ID)
                .name(name)
                .level(level)
                .description(DESCRIPTION)
                .active(true)
                .build();
    }
}
