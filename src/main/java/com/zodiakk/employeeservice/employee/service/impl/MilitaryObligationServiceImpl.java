package com.zodiakk.employeeservice.employee.service.impl;

import com.zodiakk.employeeservice.employee.dto.request.create.MilitaryObligationCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.MilitaryObligationUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.MilitaryObligationResponseDto;
import com.zodiakk.employeeservice.employee.entity.Employee;
import com.zodiakk.employeeservice.employee.entity.MilitaryObligation;
import com.zodiakk.employeeservice.employee.exception.employee.EmployeeNotFoundException;
import com.zodiakk.employeeservice.employee.exception.military.MilitaryObligationAlreadyExistsException;
import com.zodiakk.employeeservice.employee.exception.military.MilitaryObligationNotFoundException;
import com.zodiakk.employeeservice.employee.mapper.MilitaryObligationMapper;
import com.zodiakk.employeeservice.employee.repository.EmployeeRepository;
import com.zodiakk.employeeservice.employee.repository.MilitaryObligationRepository;
import com.zodiakk.employeeservice.employee.service.MilitaryObligationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MilitaryObligationServiceImpl implements MilitaryObligationService {

    private final MilitaryObligationRepository militaryObligationRepository;

    private final EmployeeRepository employeeRepository;

    private final MilitaryObligationMapper militaryObligationMapper;

    @Override
    @Transactional
    public MilitaryObligationResponseDto create(UUID employeeId, MilitaryObligationCreateRequestDto request) {
        Employee employee = findEmployee(employeeId);

        if (militaryObligationRepository.existsByEmployeeId(employeeId)) {
            throw new MilitaryObligationAlreadyExistsException(employeeId);
        }

        MilitaryObligation obligation = militaryObligationMapper.toEntity(request);

        obligation.setEmployee(employee);
        employee.setMilitaryObligation(obligation);

        return militaryObligationMapper.toResponseDto(
                militaryObligationRepository.save(obligation)
        );
    }

    @Override
    public MilitaryObligationResponseDto getByEmployeeId(UUID employeeId) {
        findEmployee(employeeId);

        MilitaryObligation obligation = findByEmployeeId(employeeId);

        return militaryObligationMapper.toResponseDto(obligation);
    }

    @Override
    @Transactional
    public MilitaryObligationResponseDto update(UUID employeeId, MilitaryObligationUpdateRequestDto request) {
        MilitaryObligation obligation = findByEmployeeId(employeeId);

        militaryObligationMapper.updateEntity(request, obligation);

        return militaryObligationMapper.toResponseDto(
                militaryObligationRepository.save(obligation)
        );
    }

    @Override
    @Transactional
    public MilitaryObligationResponseDto upsert(UUID employeeId, MilitaryObligationCreateRequestDto request) {
        findEmployee(employeeId);

        return militaryObligationRepository
                .findByEmployeeId(employeeId)
                .map(existing -> {
                    MilitaryObligationUpdateRequestDto updateRequest =
                            MilitaryObligationUpdateRequestDto.builder()
                                    .status(request.getStatus())
                                    .militaryRank(request.getMilitaryRank())
                                    .category(request.getCategory())
                                    .documentNumber(request.getDocumentNumber())
                                    .build();

                    militaryObligationMapper.updateEntity(updateRequest, existing);

                    return militaryObligationMapper.toResponseDto(
                            militaryObligationRepository.save(existing)
                    );
                })
                .orElseGet(() -> create(employeeId, request));
    }

    @Override
    @Transactional
    public void delete(UUID employeeId) {
        Employee employee = findEmployee(employeeId);

        MilitaryObligation obligation = findByEmployeeId(employeeId);

        employee.setMilitaryObligation(null);
        militaryObligationRepository.delete(obligation);
    }

    private MilitaryObligation findByEmployeeId(UUID employeeId) {
        return militaryObligationRepository
                .findByEmployeeId(employeeId)
                .orElseThrow(() -> new MilitaryObligationNotFoundException(employeeId));
    }

    private Employee findEmployee(UUID employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));
    }
}