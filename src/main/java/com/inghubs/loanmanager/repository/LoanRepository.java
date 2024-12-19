package com.inghubs.loanmanager.repository;

import com.inghubs.loanmanager.model.Customer;
import com.inghubs.loanmanager.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoanRepository extends JpaRepository<Loan, UUID> {

    Optional<Loan> findByIdAndCustomer(UUID id, Customer customer);
}
