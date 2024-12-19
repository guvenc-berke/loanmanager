package com.inghubs.loanmanager.service.impl;

import com.inghubs.loanmanager.exception.ServiceException;
import com.inghubs.loanmanager.mapper.LoanMapper;
import com.inghubs.loanmanager.model.Customer;
import com.inghubs.loanmanager.model.InstallmentPeriod;
import com.inghubs.loanmanager.model.Loan;
import com.inghubs.loanmanager.model.LoanInstallment;
import com.inghubs.loanmanager.repository.LoanInstallmentRepository;
import com.inghubs.loanmanager.repository.LoanRepository;
import com.inghubs.loanmanager.service.CustomerService;
import com.inghubs.loanmanager.service.LoanService;
import com.inghubs.model.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final LoanInstallmentRepository loanInstallmentRepository;
    private final CustomerService customerService;

    @Override
    @Transactional
    public LoanResponse createLoan(final UUID userId, final CreateLoanRequest request) {

        final Customer customer = customerService.findByUserId(userId);

        if (request.getAmount().compareTo(customer.getAvailableCreditLimit()) > 0) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "Not enough credit");
        }

        InstallmentPeriod.validateNumberOfInstallments(request.getNumberOfInstallments());

        final BigDecimal totalAmount = request.getAmount().multiply(BigDecimal.ONE.add(BigDecimal.valueOf(request.getInterestRate())));
        final BigDecimal installmentAmount = totalAmount.divide(BigDecimal.valueOf(request.getNumberOfInstallments()), 2, RoundingMode.HALF_EVEN);

        final Loan loan = Loan.builder()
                .loanAmount(totalAmount)
                .createDate(LocalDate.now())
                .numberOfInstallments(request.getNumberOfInstallments())
                .isPaid(false)
                .customer(customer)
                .build();

        final Loan savedLoan = loanRepository.save(loan);

        final List<LoanInstallment> loanInstallments = new ArrayList<>();
        for (int i = 0; i < request.getNumberOfInstallments(); i++) {
            LoanInstallment installment = new LoanInstallment();

            installment.setAmount(installmentAmount);
            installment.setPaidAmount(BigDecimal.ZERO);
            installment.setDueDate(LocalDate.now().withDayOfMonth(1).plusMonths(1 + i));
            installment.setPaid(false);
            installment.setLoan(loan);

            loanInstallments.add(installment);
        }
        final List<LoanInstallment> savedInstallments = loanInstallmentRepository.saveAll(loanInstallments);


        customer.setUsedCreditLimit(customer.getUsedCreditLimit().add(totalAmount));
        customerService.save(customer);

        return LoanMapper.map(savedLoan, savedInstallments);
    }

    @Override
    @Transactional
    public List<LoanResponse> listLoans(final UUID userId, final Integer numberOfInstallments, final Boolean isPaid) {
        final Customer customer = customerService.findByUserId(userId);
        final List<Loan> loans = new ArrayList<>(customer.getLoans());

        if (CollectionUtils.isEmpty(loans)) {
            return new ArrayList<>();
        }

        if (numberOfInstallments != null) {
            InstallmentPeriod.validateNumberOfInstallments(numberOfInstallments);

            loans.removeIf(loan -> !Objects.equals(loan.getNumberOfInstallments(), numberOfInstallments));
        }
        if (isPaid != null) {
            loans.removeIf(loan -> !Objects.equals(loan.getIsPaid(), isPaid));
        }

        return customer.getLoans().stream().map(LoanMapper::map).toList();
    }

    @Override
    @Transactional
    public List<InstallmentDetail> getInstallments(final UUID userId, final String loanId) {
        final Customer customer = customerService.findByUserId(userId);
        final Loan loan = findCustomerLoan(customer, loanId);

        return loan.getInstallments().stream()
                .map(LoanMapper::map)
                .toList();
    }

    @Override
    @Transactional
    public void payLoan(final UUID userId, final PayLoanRequest request) {
        final Customer customer = customerService.findByUserId(userId);
        final Loan loan = findCustomerLoan(customer, request.getLoanId());

        final BigDecimal installmentAmount = loan.getLoanAmount()
                .divide(BigDecimal.valueOf(loan.getNumberOfInstallments()), 2, RoundingMode.HALF_EVEN);

        final int numOfPayments = request.getAmount().divide(installmentAmount).intValue();

        final List<LoanInstallment> installmentsToPay = loan.getInstallments().stream()
                .filter(i -> !i.isPaid())
                .sorted(Comparator.comparing(LoanInstallment::getDueDate))
                .limit(Math.min(3, numOfPayments))
                .toList();

        installmentsToPay.forEach(installment -> {
            installment.setPaidAmount(installmentAmount);
            installment.setPaymentDate(LocalDate.now());
            installment.setPaid(true);
        });

        loanInstallmentRepository.saveAll(installmentsToPay);

        final boolean allInstallmentsPaid = loan.getInstallments().stream().allMatch(LoanInstallment::isPaid);
        if (allInstallmentsPaid) {
            loan.setIsPaid(true);
            loanRepository.save(loan);
        }
    }

    private Loan findCustomerLoan(Customer customer, String loanId) {
        return loanRepository.findByIdAndCustomer(UUID.fromString(loanId), customer)
                .orElseThrow(() -> new ServiceException(HttpStatus.BAD_REQUEST, "Loan not found"));
    }
}
