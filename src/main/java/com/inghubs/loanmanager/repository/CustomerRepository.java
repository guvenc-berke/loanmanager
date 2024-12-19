package com.inghubs.loanmanager.repository;

import com.inghubs.loanmanager.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    @Query("SELECT c FROM Customer c where c.user.id = :userId")
    Customer findByUserId(@Param("userId") UUID userId);
}
