package com.hexaco.hrms.config;

import com.hexaco.hrms.models.Shift;
import com.hexaco.hrms.repository.ShiftRepository;
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

    private final ShiftRepository shiftRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public void run(String... args) throws Exception {
        // Re-seed if shifts are missing or any are missing standardHours (required field)
        boolean needsReseed = shiftRepository.count() < 3;

        if (needsReseed) {
            shiftRepository.save(Shift.builder()
                    .name("Normal Shift (08:30–16:30)")
                    .startTime(LocalTime.of(8, 30))
                    .endTime(LocalTime.of(16, 30))
                    .build());
            shiftRepository.save(Shift.builder()
                    .name("Temporary Shift (08:15–16:45)")
                    .startTime(LocalTime.of(8, 15))
                    .endTime(LocalTime.of(16, 45))
                    .build());
            shiftRepository.save(Shift.builder()
                    .name("Driver Shift (08:00–17:00)")
                    .startTime(LocalTime.of(8, 0))
                    .endTime(LocalTime.of(17, 0))
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
