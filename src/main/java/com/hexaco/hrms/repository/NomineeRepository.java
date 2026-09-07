package com.hexaco.hrms.repository;

import com.hexaco.hrms.models.Nominee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NomineeRepository extends JpaRepository<Nominee, Long> {
    @Query("SELECT n FROM Nominee n WHERE n.employee.id = :employeeId")
    java.util.Optional<Nominee> findByEmployeeId(@org.springframework.data.repository.query.Param("employeeId") Long employeeId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query(value = """
        INSERT INTO nominee (employee_id, nominee_name, relationship, nic, phone_no, address, bank_name, bank_branch, account_number)
        VALUES (:empId, :name, :relationship, :nic, :phone, :address, :bank, :branch, :account)
        ON CONFLICT (employee_id) DO UPDATE SET
            nominee_name = COALESCE(EXCLUDED.nominee_name, nominee.nominee_name),
            relationship = COALESCE(EXCLUDED.relationship, nominee.relationship),
            nic = COALESCE(EXCLUDED.nic, nominee.nic),
            phone_no = COALESCE(EXCLUDED.phone_no, nominee.phone_no),
            address = COALESCE(EXCLUDED.address, nominee.address),
            bank_name = COALESCE(EXCLUDED.bank_name, nominee.bank_name),
            bank_branch = COALESCE(EXCLUDED.bank_branch, nominee.bank_branch),
            account_number = COALESCE(EXCLUDED.account_number, nominee.account_number)
        """, nativeQuery = true)
    void upsertNominee(
            @org.springframework.data.repository.query.Param("empId") Long empId,
            @org.springframework.data.repository.query.Param("name") String name,
            @org.springframework.data.repository.query.Param("relationship") String relationship,
            @org.springframework.data.repository.query.Param("nic") String nic,
            @org.springframework.data.repository.query.Param("phone") String phone,
            @org.springframework.data.repository.query.Param("address") String address,
            @org.springframework.data.repository.query.Param("bank") String bank,
            @org.springframework.data.repository.query.Param("branch") String branch,
            @org.springframework.data.repository.query.Param("account") String account
    );
}
