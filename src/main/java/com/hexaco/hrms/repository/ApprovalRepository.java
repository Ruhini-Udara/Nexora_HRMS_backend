package com.hexaco.hrms.repository;

import com.hexaco.hrms.models.Approval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalRepository extends JpaRepository<Approval, Long> {
    List<Approval> findByRefIdAndRefType(Long refId, String refType);
}
