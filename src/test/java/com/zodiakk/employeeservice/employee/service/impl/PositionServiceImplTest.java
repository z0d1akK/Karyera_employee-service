package com.zodiakk.employeeservice.employee.service.impl;

import com.zodiakk.employeeservice.common.dto.PageRequestDto;
import com.zodiakk.employeeservice.common.dto.SortDirection;
import com.zodiakk.employeeservice.employee.dto.request.create.PositionCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.filter.PositionFilterDto;
import com.zodiakk.employeeservice.employee.dto.request.update.PositionUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.PositionResponseDto;
import com.zodiakk.employeeservice.employee.entity.Position;
import com.zodiakk.employeeservice.employee.exception.position.PositionAlreadyExistsException;
import com.zodiakk.employeeservice.employee.exception.position.PositionInUseException;
import com.zodiakk.employeeservice.employee.exception.position.PositionNotFoundException;
import com.zodiakk.employeeservice.employee.mapper.PositionMapper;
import com.zodiakk.employeeservice.employee.repository.EmployeeRepository;
import com.zodiakk.employeeservice.employee.repository.PositionRepository;
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
class PositionServiceImplTest {

    private static final UUID POSITION_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private static final String NAME = "Software Engineer";

    private static final String DESCRIPTION = "Develops software";

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PositionMapper positionMapper;

    @InjectMocks
    private PositionServiceImpl positionService;

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("saves a new active position when the name is unique")
        void shouldCreatePosition() {
            PositionCreateRequestDto request = PositionCreateRequestDto.builder()
                    .name(NAME)
                    .description(DESCRIPTION)
                    .build();
            Position entity = Position.builder()
                    .name(NAME)
                    .description(DESCRIPTION)
                    .build();
            PositionResponseDto expected = response(NAME);

            when(positionRepository.existsByNameIgnoreCase(NAME)).thenReturn(false);
            when(positionMapper.toEntity(request)).thenReturn(entity);
            when(positionRepository.save(entity)).thenReturn(entity);
            when(positionMapper.toResponseDto(entity)).thenReturn(expected);

            PositionResponseDto result = positionService.create(request);

            assertThat(result).isSameAs(expected);

            ArgumentCaptor<Position> captor = ArgumentCaptor.forClass(Position.class);
            verify(positionRepository).save(captor.capture());

            Position saved = captor.getValue();
            assertThat(saved.getActive()).isTrue();
            assertThat(saved.getName()).isEqualTo(NAME);
            verify(positionMapper).toEntity(request);
            verify(positionMapper).toResponseDto(entity);
        }

        @Test
        @DisplayName("rejects a duplicate name")
        void shouldRejectDuplicateName() {
            PositionCreateRequestDto request = PositionCreateRequestDto.builder()
                    .name(NAME)
                    .description(DESCRIPTION)
                    .build();

            when(positionRepository.existsByNameIgnoreCase(NAME)).thenReturn(true);

            assertThatThrownBy(() -> positionService.create(request))
                    .isInstanceOf(PositionAlreadyExistsException.class)
                    .hasMessageContaining(NAME);

            verify(positionRepository, never()).save(any(Position.class));
            verifyNoInteractions(positionMapper);
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("returns the mapped position when it exists")
        void shouldReturnPosition() {
            Position entity = position(NAME);
            PositionResponseDto expected = response(NAME);

            when(positionRepository.findById(POSITION_ID)).thenReturn(Optional.of(entity));
            when(positionMapper.toResponseDto(entity)).thenReturn(expected);

            assertThat(positionService.getById(POSITION_ID)).isSameAs(expected);
        }

        @Test
        @DisplayName("throws when the position does not exist")
        void shouldThrowWhenMissing() {
            when(positionRepository.findById(POSITION_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> positionService.getById(POSITION_ID))
                    .isInstanceOf(PositionNotFoundException.class)
                    .hasMessageContaining(POSITION_ID.toString());

            verifyNoInteractions(positionMapper);
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("maps a filtered page and preserves paging metadata")
        void shouldReturnMappedPage() {
            PositionFilterDto filter = PositionFilterDto.builder()
                    .name("engineer")
                    .active(true)
                    .description("software")
                    .build();
            PageRequestDto pageRequest = PageRequestDto.builder()
                    .page(0)
                    .size(10)
                    .sortBy("name")
                    .sortDirection(SortDirection.ASC)
                    .build();

            Position entity = position(NAME);
            PositionResponseDto expected = response(NAME);
            Page<Position> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);

            when(positionRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
            when(positionMapper.toResponseDto(entity)).thenReturn(expected);

            Page<PositionResponseDto> result = positionService.findAll(filter, pageRequest);

            assertThat(result.getContent()).containsExactly(expected);
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.getNumber()).isZero();
            assertThat(result.getSize()).isEqualTo(10);

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(positionRepository).findAll(any(Specification.class), pageableCaptor.capture());

            Pageable pageable = pageableCaptor.getValue();
            assertThat(pageable.getPageNumber()).isZero();
            assertThat(pageable.getPageSize()).isEqualTo(10);
            assertThat(pageable.getSort().getOrderFor("name").getDirection()).isEqualTo(Sort.Direction.ASC);
        }

        @Test
        @DisplayName("returns an empty page without calling the mapper")
        void shouldReturnEmptyPage() {
            PositionFilterDto filter = PositionFilterDto.builder().name("missing").build();
            PageRequestDto pageRequest = PageRequestDto.builder().build();

            when(positionRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            Page<PositionResponseDto> result = positionService.findAll(filter, pageRequest);

            assertThat(result).isEmpty();
            assertThat(result.getTotalElements()).isZero();
            verifyNoInteractions(positionMapper);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("updates a position when the new name is unique")
        void shouldUpdatePosition() {
            Position entity = position(NAME);
            PositionUpdateRequestDto request = PositionUpdateRequestDto.builder()
                    .name("Senior Software Engineer")
                    .description("Leads software development")
                    .active(true)
                    .build();
            PositionResponseDto expected = response("Senior Software Engineer");

            when(positionRepository.findById(POSITION_ID)).thenReturn(Optional.of(entity));
            when(positionRepository.existsByNameIgnoreCase("Senior Software Engineer")).thenReturn(false);
            when(positionRepository.save(entity)).thenReturn(entity);
            when(positionMapper.toResponseDto(entity)).thenReturn(expected);

            PositionResponseDto result = positionService.update(POSITION_ID, request);

            assertThat(result).isSameAs(expected);
            verify(positionMapper).updateEntity(request, entity);
            verify(positionRepository).save(entity);
        }

        @Test
        @DisplayName("skips the uniqueness check when the name is unchanged ignoring case")
        void shouldSkipCheckForUnchangedName() {
            Position entity = position(NAME);
            PositionUpdateRequestDto request = PositionUpdateRequestDto.builder()
                    .name(NAME.toUpperCase())
                    .active(true)
                    .build();
            PositionResponseDto expected = response(NAME);

            when(positionRepository.findById(POSITION_ID)).thenReturn(Optional.of(entity));
            when(positionRepository.save(entity)).thenReturn(entity);
            when(positionMapper.toResponseDto(entity)).thenReturn(expected);

            assertThat(positionService.update(POSITION_ID, request)).isSameAs(expected);

            verify(positionRepository, never()).existsByNameIgnoreCase(anyString());
            verify(positionMapper).updateEntity(request, entity);
        }

        @Test
        @DisplayName("skips the uniqueness check when the name is null")
        void shouldSkipCheckForNullName() {
            Position entity = position(NAME);
            PositionUpdateRequestDto request = PositionUpdateRequestDto.builder()
                    .description("Updated description")
                    .active(true)
                    .build();
            PositionResponseDto expected = response(NAME);

            when(positionRepository.findById(POSITION_ID)).thenReturn(Optional.of(entity));
            when(positionRepository.save(entity)).thenReturn(entity);
            when(positionMapper.toResponseDto(entity)).thenReturn(expected);

            assertThat(positionService.update(POSITION_ID, request)).isSameAs(expected);

            verify(positionRepository, never()).existsByNameIgnoreCase(anyString());
        }

        @Test
        @DisplayName("skips the uniqueness check when the name is blank")
        void shouldSkipCheckForBlankName() {
            Position entity = position(NAME);
            PositionUpdateRequestDto request = PositionUpdateRequestDto.builder()
                    .name(" ")
                    .active(true)
                    .build();
            PositionResponseDto expected = response(NAME);

            when(positionRepository.findById(POSITION_ID)).thenReturn(Optional.of(entity));
            when(positionRepository.save(entity)).thenReturn(entity);
            when(positionMapper.toResponseDto(entity)).thenReturn(expected);

            assertThat(positionService.update(POSITION_ID, request)).isSameAs(expected);

            verify(positionRepository, never()).existsByNameIgnoreCase(anyString());
        }

        @Test
        @DisplayName("throws when the position does not exist")
        void shouldThrowWhenMissing() {
            PositionUpdateRequestDto request = PositionUpdateRequestDto.builder()
                    .name("Senior Software Engineer")
                    .active(true)
                    .build();

            when(positionRepository.findById(POSITION_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> positionService.update(POSITION_ID, request))
                    .isInstanceOf(PositionNotFoundException.class);

            verifyNoInteractions(positionMapper);
            verify(positionRepository, never()).save(any(Position.class));
        }

        @Test
        @DisplayName("throws when the new name is already taken")
        void shouldThrowForDuplicateName() {
            Position entity = position(NAME);
            PositionUpdateRequestDto request = PositionUpdateRequestDto.builder()
                    .name("Senior Software Engineer")
                    .active(true)
                    .build();

            when(positionRepository.findById(POSITION_ID)).thenReturn(Optional.of(entity));
            when(positionRepository.existsByNameIgnoreCase("Senior Software Engineer")).thenReturn(true);

            assertThatThrownBy(() -> positionService.update(POSITION_ID, request))
                    .isInstanceOf(PositionAlreadyExistsException.class)
                    .hasMessageContaining("Senior Software Engineer");

            verify(positionMapper, never()).updateEntity(any(), any());
            verify(positionRepository, never()).save(any(Position.class));
        }
    }

    @Nested
    @DisplayName("deactivate")
    class Deactivate {

        @Test
        @DisplayName("deactivates an active position")
        void shouldDeactivate() {
            Position entity = position(NAME);

            when(positionRepository.findById(POSITION_ID)).thenReturn(Optional.of(entity));

            positionService.deactivate(POSITION_ID);

            assertThat(entity.getActive()).isFalse();
            verify(positionRepository).save(entity);
        }

        @Test
        @DisplayName("leaves an already inactive position untouched")
        void shouldSkipAlreadyInactive() {
            Position entity = position(NAME);
            entity.setActive(false);

            when(positionRepository.findById(POSITION_ID)).thenReturn(Optional.of(entity));

            positionService.deactivate(POSITION_ID);

            assertThat(entity.getActive()).isFalse();
            verify(positionRepository, never()).save(any(Position.class));
        }

        @Test
        @DisplayName("throws when the position does not exist")
        void shouldThrowWhenMissing() {
            when(positionRepository.findById(POSITION_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> positionService.deactivate(POSITION_ID))
                    .isInstanceOf(PositionNotFoundException.class);

            verify(positionRepository, never()).save(any(Position.class));
        }
    }

    @Nested
    @DisplayName("activate")
    class Activate {

        @Test
        @DisplayName("activates an inactive position")
        void shouldActivate() {
            Position entity = position(NAME);
            entity.setActive(false);

            when(positionRepository.findById(POSITION_ID)).thenReturn(Optional.of(entity));

            positionService.activate(POSITION_ID);

            assertThat(entity.getActive()).isTrue();
            verify(positionRepository).save(entity);
        }

        @Test
        @DisplayName("leaves an already active position untouched")
        void shouldSkipAlreadyActive() {
            Position entity = position(NAME);

            when(positionRepository.findById(POSITION_ID)).thenReturn(Optional.of(entity));

            positionService.activate(POSITION_ID);

            assertThat(entity.getActive()).isTrue();
            verify(positionRepository, never()).save(any(Position.class));
        }

        @Test
        @DisplayName("throws when the position does not exist")
        void shouldThrowWhenMissing() {
            when(positionRepository.findById(POSITION_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> positionService.activate(POSITION_ID))
                    .isInstanceOf(PositionNotFoundException.class);

            verify(positionRepository, never()).save(any(Position.class));
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("deletes a position that has no employees")
        void shouldDelete() {
            Position entity = position(NAME);

            when(positionRepository.findById(POSITION_ID)).thenReturn(Optional.of(entity));
            when(employeeRepository.countByPositionId(POSITION_ID)).thenReturn(0L);

            positionService.delete(POSITION_ID);

            verify(employeeRepository).countByPositionId(POSITION_ID);
            verify(positionRepository).delete(entity);
        }

        @Test
        @DisplayName("rejects deletion when employees reference the position")
        void shouldRejectWhenInUse() {
            Position entity = position(NAME);

            when(positionRepository.findById(POSITION_ID)).thenReturn(Optional.of(entity));
            when(employeeRepository.countByPositionId(POSITION_ID)).thenReturn(2L);

            assertThatThrownBy(() -> positionService.delete(POSITION_ID))
                    .isInstanceOf(PositionInUseException.class)
                    .hasMessageContaining(POSITION_ID.toString());

            verify(positionRepository, never()).delete(any(Position.class));
        }

        @Test
        @DisplayName("throws when the position does not exist")
        void shouldThrowWhenMissing() {
            when(positionRepository.findById(POSITION_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> positionService.delete(POSITION_ID))
                    .isInstanceOf(PositionNotFoundException.class);

            verifyNoInteractions(employeeRepository);
            verify(positionRepository, never()).delete(any(Position.class));
        }
    }

    private Position position(String name) {
        return Position.builder()
                .id(POSITION_ID)
                .name(name)
                .description(DESCRIPTION)
                .active(true)
                .build();
    }

    private PositionResponseDto response(String name) {
        return PositionResponseDto.builder()
                .id(POSITION_ID)
                .name(name)
                .description(DESCRIPTION)
                .active(true)
                .build();
    }
}
