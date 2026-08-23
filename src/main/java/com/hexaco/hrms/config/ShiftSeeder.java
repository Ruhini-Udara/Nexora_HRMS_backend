package com.hexaco.hrms.config;

import com.hexaco.hrms.models.AttendanceShift;
import com.hexaco.hrms.repository.AttendanceShiftRepository;
import lombok.RequiredArgsConstructor;
import com.hexaco.hrms.models.Employee;
import com.hexaco.hrms.repository.EmployeeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ShiftSeeder implements CommandLineRunner {

    private final AttendanceShiftRepository shiftRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public void run(String... args) throws Exception {
        if (shiftRepository.count() < 3) {
            shiftRepository.deleteAll();
            shiftRepository.save(AttendanceShift.builder()
                    .shiftName("Normal Shift (08:30–16:30)")
                    .startTime(LocalTime.of(8, 30))
                    .endTime(LocalTime.of(16, 30))
                    .standardHours(8.0)
                    .build());
            shiftRepository.save(AttendanceShift.builder()
                    .shiftName("Temporary Shift (08:15–16:45)")
                    .startTime(LocalTime.of(8, 15))
                    .endTime(LocalTime.of(16, 45))
                    .standardHours(8.5)
                    .build());
            shiftRepository.save(AttendanceShift.builder()
                    .shiftName("Driver Shift (08:00–17:00)")
                    .startTime(LocalTime.of(8, 0))
                    .endTime(LocalTime.of(17, 0))
                    .standardHours(9.0)
                    .build());
        }

        // Demo fallback: if no employees have a reporting officer, assign them to employee ID 1 (or the first employee)
        List<Employee> allEmployees = employeeRepository.findAll();
        boolean hasHierarchy = allEmployees.stream().anyMatch(e -> e.getReportingOfficer() != null);
        
        if (!hasHierarchy && !allEmployees.isEmpty()) {
            // Find a supervisor candidate (e.g. employee with ID 1 or the first one)
            Employee supervisor = allEmployees.stream().filter(e -> e.getId() == 1L).findFirst().orElse(allEmployees.get(0));
            
            for (Employee e : allEmployees) {
                if (!e.getId().equals(supervisor.getId())) {
                    e.setReportingOfficer(supervisor);
                    employeeRepository.save(e);
                }
            }
        }
    }
}
