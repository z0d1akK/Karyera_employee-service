package com.zodiakk.employeeservice.employee.mapper;

import com.zodiakk.employeeservice.config.MapStructConfig;
import com.zodiakk.employeeservice.employee.dto.request.create.EmployeeCreateRequestDto;
import com.zodiakk.employeeservice.employee.dto.request.update.EmployeeUpdateRequestDto;
import com.zodiakk.employeeservice.employee.dto.response.EmployeeResponseDto;
import com.zodiakk.employeeservice.employee.dto.response.EmployeeSummaryResponseDto;
import com.zodiakk.employeeservice.employee.entity.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class,
        uses = {PositionMapper.class,
                SpecializationMapper.class,
                GradeMapper.class,
                DepartmentMapper.class,
                EmployeeProfilePhotoMapper.class,
                EmployeeSkillMapper.class,
                ExperienceMapper.class,
                EducationMapper.class,
                MilitaryObligationMapper.class
        }
)
public interface EmployeeMapper {

    @Mapping(target = "position", ignore = true)
    @Mapping(target = "specialization", ignore = true)
    @Mapping(target = "grade", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "skills", ignore = true)
    @Mapping(target = "experiences", ignore = true)
    @Mapping(target = "educations", ignore = true)
    @Mapping(target = "militaryObligation", ignore = true)
    @Mapping(target = "profilePhoto", ignore = true)
    @Mapping(target = "version", ignore = true)
    Employee toEntity(EmployeeCreateRequestDto dto);

    @Mapping(target = "responsibilities", ignore = true)
    EmployeeResponseDto toResponseDto(Employee employee);

    @Mapping(target = "position", ignore = true)
    @Mapping(target = "specialization", ignore = true)
    @Mapping(target = "grade", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "skills", ignore = true)
    @Mapping(target = "experiences", ignore = true)
    @Mapping(target = "educations", ignore = true)
    @Mapping(target = "militaryObligation", ignore = true)
    @Mapping(target = "profilePhoto", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(EmployeeUpdateRequestDto dto, @MappingTarget Employee employee);

    EmployeeSummaryResponseDto toSummaryResponseDto(Employee employee);
}
