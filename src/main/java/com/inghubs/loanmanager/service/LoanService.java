package com.inghubs.loanmanager.service;

import com.inghubs.model.*;

import java.util.List;
import java.util.UUID;

public interface LoanService {

    LoanResponse createLoan(UUID userId, CreateLoanRequest request);

    List<LoanResponse> listLoans(UUID userId, Integer numberOfInstallments, Boolean isPaid);

    List<InstallmentDetail> getInstallments(UUID userId, String loanId);

    void payLoan(UUID userId, PayLoanRequest request);
}
