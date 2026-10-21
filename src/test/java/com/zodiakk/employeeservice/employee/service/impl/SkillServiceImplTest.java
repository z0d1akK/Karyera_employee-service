package com.zodiakk.employeeservice.employee.service.impl;

import com.zodiakk.employeeservice.common.dto.PageRequestDto;
import com.zodiakk.employeeservice.common.dto.SortDirection;
import com.zodiakk.employeeservice.employee.dto.request.create.SkillCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.filter.SkillFilterDto;
import com.zodiakk.employeeservice.employee.dto.request.update.SkillUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.SkillResponseDto;
import com.zodiakk.employeeservice.employee.entity.Skill;
import com.zodiakk.employeeservice.employee.exception.skill.SkillAlreadyExistsException;
import com.zodiakk.employeeservice.employee.exception.skill.SkillInUseException;
import com.zodiakk.employeeservice.employee.exception.skill.SkillNotFoundException;
import com.zodiakk.employeeservice.employee.mapper.SkillMapper;
import com.zodiakk.employeeservice.employee.repository.EmployeeSkillRepository;
import com.zodiakk.employeeservice.employee.repository.SkillRepository;
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
class SkillServiceImplTest {

    private static final UUID SKILL_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    private static final String NAME = "Java";

    private static final String CATEGORY = "Backend";

    private static final String DESCRIPTION = "JVM programming language";

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private EmployeeSkillRepository employeeSkillRepository;

    @Mock
    private SkillMapper skillMapper;

    @InjectMocks
    private SkillServiceImpl skillService;

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("saves a new active skill when the name is unique")
        void shouldCreateSkill() {
            SkillCreateRequestDto request = SkillCreateRequestDto.builder()
                    .name(NAME)
                    .category(CATEGORY)
                    .description(DESCRIPTION)
                    .build();
            Skill entity = Skill.builder()
                    .name(NAME)
                    .category(CATEGORY)
                    .description(DESCRIPTION)
                    .build();
            SkillResponseDto expected = response(NAME);

            when(skillRepository.existsByNameIgnoreCase(NAME)).thenReturn(false);
            when(skillMapper.toEntity(request)).thenReturn(entity);
            when(skillRepository.save(entity)).thenReturn(entity);
            when(skillMapper.toResponseDto(entity)).thenReturn(expected);

            SkillResponseDto result = skillService.create(request);

            assertThat(result).isSameAs(expected);

            ArgumentCaptor<Skill> captor = ArgumentCaptor.forClass(Skill.class);
            verify(skillRepository).save(captor.capture());

            Skill saved = captor.getValue();
            assertThat(saved.getActive()).isTrue();
            assertThat(saved.getName()).isEqualTo(NAME);
            assertThat(saved.getCategory()).isEqualTo(CATEGORY);
            verify(skillMapper).toEntity(request);
            verify(skillMapper).toResponseDto(entity);
        }

        @Test
        @DisplayName("rejects a duplicate name")
        void shouldRejectDuplicateName() {
            SkillCreateRequestDto request = SkillCreateRequestDto.builder()
                    .name(NAME)
                    .category(CATEGORY)
                    .build();

            when(skillRepository.existsByNameIgnoreCase(NAME)).thenReturn(true);

            assertThatThrownBy(() -> skillService.create(request))
                    .isInstanceOf(SkillAlreadyExistsException.class)
                    .hasMessageContaining(NAME);

            verify(skillRepository, never()).save(any(Skill.class));
            verifyNoInteractions(skillMapper);
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("returns the mapped skill when it exists")
        void shouldReturnSkill() {
            Skill entity = skill(NAME);
            SkillResponseDto expected = response(NAME);

            when(skillRepository.findById(SKILL_ID)).thenReturn(Optional.of(entity));
            when(skillMapper.toResponseDto(entity)).thenReturn(expected);

            assertThat(skillService.getById(SKILL_ID)).isSameAs(expected);
        }

        @Test
        @DisplayName("throws when the skill does not exist")
        void shouldThrowWhenMissing() {
            when(skillRepository.findById(SKILL_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> skillService.getById(SKILL_ID))
                    .isInstanceOf(SkillNotFoundException.class)
                    .hasMessageContaining(SKILL_ID.toString());

            verifyNoInteractions(skillMapper);
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("maps a filtered page and preserves paging metadata")
        void shouldReturnMappedPage() {
            SkillFilterDto filter = SkillFilterDto.builder()
                    .name("ja")
                    .category(CATEGORY)
                    .active(true)
                    .description("language")
                    .build();
            PageRequestDto pageRequest = PageRequestDto.builder()
                    .page(1)
                    .size(5)
                    .sortBy("category")
                    .sortDirection(SortDirection.DESC)
                    .build();

            Skill entity = skill(NAME);
            SkillResponseDto expected = response(NAME);
            Page<Skill> page = new PageImpl<>(List.of(entity), PageRequest.of(1, 5), 6);

            when(skillRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
            when(skillMapper.toResponseDto(entity)).thenReturn(expected);

            Page<SkillResponseDto> result = skillService.findAll(filter, pageRequest);

            assertThat(result.getContent()).containsExactly(expected);
            assertThat(result.getTotalElements()).isEqualTo(6);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.getNumber()).isEqualTo(1);
            assertThat(result.getSize()).isEqualTo(5);

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(skillRepository).findAll(any(Specification.class), pageableCaptor.capture());

            Pageable pageable = pageableCaptor.getValue();
            assertThat(pageable.getPageNumber()).isEqualTo(1);
            assertThat(pageable.getPageSize()).isEqualTo(5);
            assertThat(pageable.getSort().getOrderFor("category")).isNotNull();
            assertThat(pageable.getSort().getOrderFor("category").getDirection()).isEqualTo(Sort.Direction.DESC);
        }

        @Test
        @DisplayName("returns an empty page without calling the mapper")
        void shouldReturnEmptyPage() {
            SkillFilterDto filter = SkillFilterDto.builder().name("missing").build();
            PageRequestDto pageRequest = PageRequestDto.builder().build();

            when(skillRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            Page<SkillResponseDto> result = skillService.findAll(filter, pageRequest);

            assertThat(result).isEmpty();
            assertThat(result.getTotalElements()).isZero();
            verifyNoInteractions(skillMapper);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("updates a skill when the new name is unique")
        void shouldUpdateSkill() {
            Skill entity = skill(NAME);
            SkillUpdateRequestDto request = SkillUpdateRequestDto.builder()
                    .name("Kotlin")
                    .category("Mobile")
                    .description("JVM language for Android")
                    .active(true)
                    .build();
            SkillResponseDto expected = response("Kotlin");

            when(skillRepository.findById(SKILL_ID)).thenReturn(Optional.of(entity));
            when(skillRepository.existsByNameIgnoreCase("Kotlin")).thenReturn(false);
            when(skillRepository.save(entity)).thenReturn(entity);
            when(skillMapper.toResponseDto(entity)).thenReturn(expected);

            SkillResponseDto result = skillService.update(SKILL_ID, request);

            assertThat(result).isSameAs(expected);
            verify(skillMapper).updateEntity(request, entity);
            verify(skillRepository).save(entity);
        }

        @Test
        @DisplayName("skips the uniqueness check when the name is unchanged ignoring case")
        void shouldSkipCheckForUnchangedName() {
            Skill entity = skill(NAME);
            SkillUpdateRequestDto request = SkillUpdateRequestDto.builder()
                    .name("JAVA")
                    .active(true)
                    .build();
            SkillResponseDto expected = response(NAME);

            when(skillRepository.findById(SKILL_ID)).thenReturn(Optional.of(entity));
            when(skillRepository.save(entity)).thenReturn(entity);
            when(skillMapper.toResponseDto(entity)).thenReturn(expected);

            assertThat(skillService.update(SKILL_ID, request)).isSameAs(expected);

            verify(skillRepository, never()).existsByNameIgnoreCase(anyString());
            verify(skillMapper).updateEntity(request, entity);
        }

        @Test
        @DisplayName("skips the uniqueness check when the name is null")
        void shouldSkipCheckForNullName() {
            Skill entity = skill(NAME);
            SkillUpdateRequestDto request = SkillUpdateRequestDto.builder()
                    .description("Updated description")
                    .active(true)
                    .build();
            SkillResponseDto expected = response(NAME);

            when(skillRepository.findById(SKILL_ID)).thenReturn(Optional.of(entity));
            when(skillRepository.save(entity)).thenReturn(entity);
            when(skillMapper.toResponseDto(entity)).thenReturn(expected);

            assertThat(skillService.update(SKILL_ID, request)).isSameAs(expected);

            verify(skillRepository, never()).existsByNameIgnoreCase(anyString());
        }

        @Test
        @DisplayName("skips the uniqueness check when the name is blank")
        void shouldSkipCheckForBlankName() {
            Skill entity = skill(NAME);
            SkillUpdateRequestDto request = SkillUpdateRequestDto.builder()
                    .name("  ")
                    .active(true)
                    .build();
            SkillResponseDto expected = response(NAME);

            when(skillRepository.findById(SKILL_ID)).thenReturn(Optional.of(entity));
            when(skillRepository.save(entity)).thenReturn(entity);
            when(skillMapper.toResponseDto(entity)).thenReturn(expected);

            assertThat(skillService.update(SKILL_ID, request)).isSameAs(expected);

            verify(skillRepository, never()).existsByNameIgnoreCase(anyString());
        }

        @Test
        @DisplayName("throws when the skill does not exist")
        void shouldThrowWhenMissing() {
            SkillUpdateRequestDto request = SkillUpdateRequestDto.builder()
                    .name("Kotlin")
                    .active(true)
                    .build();

            when(skillRepository.findById(SKILL_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> skillService.update(SKILL_ID, request))
                    .isInstanceOf(SkillNotFoundException.class);

            verifyNoInteractions(skillMapper);
            verify(skillRepository, never()).save(any(Skill.class));
        }

        @Test
        @DisplayName("throws when the new name is already taken")
        void shouldThrowForDuplicateName() {
            Skill entity = skill(NAME);
            SkillUpdateRequestDto request = SkillUpdateRequestDto.builder()
                    .name("Kotlin")
                    .active(true)
                    .build();

            when(skillRepository.findById(SKILL_ID)).thenReturn(Optional.of(entity));
            when(skillRepository.existsByNameIgnoreCase("Kotlin")).thenReturn(true);

            assertThatThrownBy(() -> skillService.update(SKILL_ID, request))
                    .isInstanceOf(SkillAlreadyExistsException.class)
                    .hasMessageContaining("Kotlin");

            verify(skillMapper, never()).updateEntity(any(), any());
            verify(skillRepository, never()).save(any(Skill.class));
        }
    }

    @Nested
    @DisplayName("deactivate")
    class Deactivate {

        @Test
        @DisplayName("deactivates an active skill")
        void shouldDeactivate() {
            Skill entity = skill(NAME);

            when(skillRepository.findById(SKILL_ID)).thenReturn(Optional.of(entity));

            skillService.deactivate(SKILL_ID);

            assertThat(entity.getActive()).isFalse();
            verify(skillRepository).save(entity);
        }

        @Test
        @DisplayName("leaves an already inactive skill untouched")
        void shouldSkipAlreadyInactive() {
            Skill entity = skill(NAME);
            entity.setActive(false);

            when(skillRepository.findById(SKILL_ID)).thenReturn(Optional.of(entity));

            skillService.deactivate(SKILL_ID);

            assertThat(entity.getActive()).isFalse();
            verify(skillRepository, never()).save(any(Skill.class));
        }

        @Test
        @DisplayName("throws when the skill does not exist")
        void shouldThrowWhenMissing() {
            when(skillRepository.findById(SKILL_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> skillService.deactivate(SKILL_ID))
                    .isInstanceOf(SkillNotFoundException.class);

            verify(skillRepository, never()).save(any(Skill.class));
        }
    }

    @Nested
    @DisplayName("activate")
    class Activate {

        @Test
        @DisplayName("activates an inactive skill")
        void shouldActivate() {
            Skill entity = skill(NAME);
            entity.setActive(false);

            when(skillRepository.findById(SKILL_ID)).thenReturn(Optional.of(entity));

            skillService.activate(SKILL_ID);

            assertThat(entity.getActive()).isTrue();
            verify(skillRepository).save(entity);
        }

        @Test
        @DisplayName("leaves an already active skill untouched")
        void shouldSkipAlreadyActive() {
            Skill entity = skill(NAME);

            when(skillRepository.findById(SKILL_ID)).thenReturn(Optional.of(entity));

            skillService.activate(SKILL_ID);

            assertThat(entity.getActive()).isTrue();
            verify(skillRepository, never()).save(any(Skill.class));
        }

        @Test
        @DisplayName("throws when the skill does not exist")
        void shouldThrowWhenMissing() {
            when(skillRepository.findById(SKILL_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> skillService.activate(SKILL_ID))
                    .isInstanceOf(SkillNotFoundException.class);

            verify(skillRepository, never()).save(any(Skill.class));
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("deletes a skill that no employee references")
        void shouldDelete() {
            Skill entity = skill(NAME);

            when(skillRepository.findById(SKILL_ID)).thenReturn(Optional.of(entity));
            when(employeeSkillRepository.existsBySkillId(SKILL_ID)).thenReturn(false);

            skillService.delete(SKILL_ID);

            verify(employeeSkillRepository).existsBySkillId(SKILL_ID);
            verify(skillRepository).delete(entity);
        }

        @Test
        @DisplayName("rejects deletion when employees reference the skill")
        void shouldRejectWhenInUse() {
            Skill entity = skill(NAME);

            when(skillRepository.findById(SKILL_ID)).thenReturn(Optional.of(entity));
            when(employeeSkillRepository.existsBySkillId(SKILL_ID)).thenReturn(true);

            assertThatThrownBy(() -> skillService.delete(SKILL_ID))
                    .isInstanceOf(SkillInUseException.class)
                    .hasMessageContaining(SKILL_ID.toString());

            verify(skillRepository, never()).delete(any(Skill.class));
        }

        @Test
        @DisplayName("throws when the skill does not exist")
        void shouldThrowWhenMissing() {
            when(skillRepository.findById(SKILL_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> skillService.delete(SKILL_ID))
                    .isInstanceOf(SkillNotFoundException.class);

            verifyNoInteractions(employeeSkillRepository);
            verify(skillRepository, never()).delete(any(Skill.class));
        }
    }

    private Skill skill(String name) {
        return Skill.builder()
                .id(SKILL_ID)
                .name(name)
                .category(CATEGORY)
                .description(DESCRIPTION)
                .active(true)
                .build();
    }

    private SkillResponseDto response(String name) {
        return SkillResponseDto.builder()
                .id(SKILL_ID)
                .name(name)
                .category(CATEGORY)
                .description(DESCRIPTION)
                .active(true)
                .build();
    }
}
