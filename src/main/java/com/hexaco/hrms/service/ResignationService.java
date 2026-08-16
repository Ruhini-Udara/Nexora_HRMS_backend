package com.hexaco.hrms.service;

import com.hexaco.hrms.dto.ResignationDto;
import java.util.List;

public interface ResignationService {
    ResignationDto createResignation(ResignationDto resignationDto);
    List<ResignationDto> getAllResignations();
    List<ResignationDto> getResignationsByEmployeeId(Long employeeId);
    ResignationDto updateResignationStatus(Long id, String status, String remarks, String boardMeetingDate);
    ResignationDto getResignationById(Long id);
}
