package com.hexaco.hrms.repository;

import com.hexaco.hrms.models.Nominee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NomineeRepository extends JpaRepository<Nominee, Long> {
    @Query("SELECT n FROM Nominee n WHERE n.employee.id = :employeeId")
    java.util.Optional<Nominee> findByEmployeeId(@org.springframework.data.repository.query.Param("employeeId") Long employeeId);
}
