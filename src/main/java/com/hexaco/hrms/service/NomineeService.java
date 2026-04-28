package com.hexaco.hrms.service;

import com.hexaco.hrms.dto.NomineeDto;
import com.hexaco.hrms.models.Employee;
import com.hexaco.hrms.models.Nominee;
import com.hexaco.hrms.repository.EmployeeRepository;
import com.hexaco.hrms.repository.NomineeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NomineeService {

    private final NomineeRepository nomineeRepository;
    private final EmployeeRepository employeeRepository;

    public NomineeDto getNomineeByEmployeeId(Long employeeId) {
        Nominee nominee = nomineeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Nominee not found for employee ID: " + employeeId));
        return mapToDto(nominee);
    }

    public NomineeDto saveOrUpdateNominee(NomineeDto dto) {
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        Nominee nominee = nomineeRepository.findById(dto.getEmployeeId())
                .orElse(new Nominee());

        nominee.setEmployee(employee);
        nominee.setNomineeName(dto.getNomineeName());
        nominee.setRelationship(dto.getRelationship());
        nominee.setNic(dto.getNic());
        nominee.setPhoneNo(dto.getPhoneNo());
        nominee.setAddress(dto.getAddress());
        nominee.setBankName(dto.getBankName());
        nominee.setBankBranch(dto.getBankBranch());
        nominee.setAccountNumber(dto.getAccountNumber());

        return mapToDto(nomineeRepository.save(nominee));
    }

    private NomineeDto mapToDto(Nominee nominee) {
        return NomineeDto.builder()
                .employeeId(nominee.getEmployee() != null ? nominee.getEmployee().getId() : null)
                .nomineeName(nominee.getNomineeName())
                .relationship(nominee.getRelationship())
                .nic(nominee.getNic())
                .phoneNo(nominee.getPhoneNo())
                .address(nominee.getAddress())
                .bankName(nominee.getBankName())
                .bankBranch(nominee.getBankBranch())
                .accountNumber(nominee.getAccountNumber())
                .build();
    }
}
