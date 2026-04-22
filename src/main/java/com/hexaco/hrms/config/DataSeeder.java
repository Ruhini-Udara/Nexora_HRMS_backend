package com.hexaco.hrms.config;

import com.hexaco.hrms.models.Employee;
import com.hexaco.hrms.models.LeaveType;
import com.hexaco.hrms.repository.EmployeeRepository;
import com.hexaco.hrms.repository.LeaveTypeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner loadData(EmployeeRepository employeeRepository, LeaveTypeRepository leaveTypeRepository) {
        return args -> {
            // Seed a Dummy Employee if it doesn't exist
            if (employeeRepository.count() == 0) {
                Employee employee = new Employee();
                employee.setEmployeeCode("EMP001");
                employee.setFirstName("John");
                employee.setLastName("Doe");
                employee.setEmail("john.doe@example.com");
                employee.setNic("000000000V");
                employee.setPhoneNo("+94771234567");
                employee.setJoinedDate(LocalDate.now());
                employee.setGender("M");
                employeeRepository.save(employee);
            }

            // Seed Dummy Leave Types if they don't exist
            if (leaveTypeRepository.count() == 0) {
                LeaveType overseas = new LeaveType();
                overseas.setLeaveTypeName("Overseas Leave");
                overseas.setDescription("Leave for traveling abroad");
                leaveTypeRepository.save(overseas);
            }
        };
    }
}
