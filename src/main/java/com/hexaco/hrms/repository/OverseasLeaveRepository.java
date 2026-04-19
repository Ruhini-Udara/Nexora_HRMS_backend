package com.hexaco.hrms.repository;

import com.hexaco.hrms.models.OverseasLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OverseasLeaveRepository extends JpaRepository<OverseasLeave, Long> {
}
