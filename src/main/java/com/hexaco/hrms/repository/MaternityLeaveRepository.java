package com.hexaco.hrms.repository;

import com.hexaco.hrms.models.MaternityLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaternityLeaveRepository extends JpaRepository<MaternityLeave, Long> {
}
