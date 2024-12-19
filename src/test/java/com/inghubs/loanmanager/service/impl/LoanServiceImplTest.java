package com.inghubs.loanmanager.service.impl;

import com.inghubs.loanmanager.exception.ServiceException;
import com.inghubs.loanmanager.model.Customer;
import com.inghubs.loanmanager.model.Loan;
import com.inghubs.loanmanager.model.LoanInstallment;
import com.inghubs.loanmanager.repository.LoanInstallmentRepository;
import com.inghubs.loanmanager.repository.LoanRepository;
import com.inghubs.loanmanager.service.CustomerService;
import com.inghubs.model.CreateLoanRequest;
import com.inghubs.model.InstallmentDetail;
import com.inghubs.model.LoanResponse;
import com.inghubs.model.PayLoanRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanServiceImplTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private LoanInstallmentRepository loanInstallmentRepository;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private LoanServiceImpl loanService;

    @Test
    void createLoan_success() {
        Customer customer = createCustomerEntity();
        Loan loan = createLoanEntity(customer);

        when(customerService.findByUserId(any())).thenReturn(customer);
        when(loanRepository.save(any())).thenReturn(loan);
        when(loanInstallmentRepository.saveAll(any())).thenReturn(loan.getInstallments());

        CreateLoanRequest request = new CreateLoanRequest();
        request.setAmount(BigDecimal.valueOf(300));
        request.setInterestRate(0.1);
        request.setNumberOfInstallments(6);

        LoanResponse loanResponse = loanService.createLoan(customer.getId(), request);

        verify(customerService).save(any());

        assertNotNull(loanResponse);
        assertEquals(BigDecimal.valueOf(330), loanResponse.getAmount());
        assertEquals(LocalDate.now().toString(), loanResponse.getCreateDate());
        assertEquals(6, loanResponse.getNumberOfInstallments());
        assertEquals(6, loanResponse.getInstallments().size());
        assertEquals(BigDecimal.valueOf(55), loanResponse.getInstallments().getFirst().getAmount());
        assertEquals(LocalDate.now().withDayOfMonth(1).plusMonths(1).toString(), loanResponse.getInstallments().getFirst().getDueDate());
    }

    @Test
    void createLoan_notEnoughCredit() {
        Customer customer = createCustomerEntity();
        customer.setUsedCreditLimit(customer.getAvailableCreditLimit());

        CreateLoanRequest request = new CreateLoanRequest();
        request.setAmount(BigDecimal.valueOf(300));
        request.setInterestRate(0.1);
        request.setNumberOfInstallments(6);

        when(customerService.findByUserId(any())).thenReturn(customer);

        ServiceException serviceException = assertThrows(ServiceException.class, () -> loanService.createLoan(customer.getId(), request));

        assertEquals(HttpStatus.BAD_REQUEST, serviceException.getHttpStatus());
        assertEquals("Not enough credit", serviceException.getErrorMessage());
    }

    @Test
    void createLoan_invalidNumberOfInstallments() {
        final Customer customer = createCustomerEntity();
        final CreateLoanRequest request = new CreateLoanRequest();

        request.setAmount(BigDecimal.valueOf(300));
        request.setInterestRate(0.1);
        request.setNumberOfInstallments(1);

        when(customerService.findByUserId(any())).thenReturn(customer);

        final ServiceException serviceException = assertThrows(ServiceException.class, () -> loanService.createLoan(customer.getId(), request));

        assertEquals(HttpStatus.BAD_REQUEST, serviceException.getHttpStatus());
        assertEquals("Invalid number of installments!", serviceException.getErrorMessage());
    }

    @Test
    void listLoans_success() {
        final Customer customer = createCustomerEntity();
        final Loan loan = createLoanEntity(customer);

        customer.setLoans(List.of(loan));

        when(customerService.findByUserId(any())).thenReturn(customer);

        final List<LoanResponse> loans = loanService.listLoans(customer.getId(), loan.getNumberOfInstallments(), false);

        assertEquals(1, loans.size());
        assertEquals(loan.getId().toString(), loans.getFirst().getId());
    }

    @Test
    void getInstallments_success() {
        final Customer customer = createCustomerEntity();
        final Loan loan = createLoanEntity(customer);

        when(customerService.findByUserId(any())).thenReturn(customer);
        when(loanRepository.findByIdAndCustomer(loan.getId(), customer)).thenReturn((Optional.of(loan)));

        final List<InstallmentDetail> installments = loanService.getInstallments(customer.getId(), loan.getId().toString());

        assertFalse(installments.isEmpty());
        assertEquals(6, installments.size());
    }

    @Test
    void getInstallments_loanNotFound() {
        final Customer customer = createCustomerEntity();

        when(customerService.findByUserId(any())).thenReturn(customer);
        when(loanRepository.findByIdAndCustomer(any(UUID.class), eq(customer))).thenReturn((Optional.empty()));

        final ServiceException serviceException = assertThrows(ServiceException.class,
                () -> loanService.getInstallments(customer.getId(), UUID.randomUUID().toString()));

        assertEquals(HttpStatus.BAD_REQUEST, serviceException.getHttpStatus());
        assertEquals("Loan not found", serviceException.getErrorMessage());
    }

    @Test
    void payLoan_success() {
        final Customer customer = createCustomerEntity();
        final Loan loan = createLoanEntity(customer);

        when(customerService.findByUserId(any())).thenReturn(customer);
        when(loanRepository.findByIdAndCustomer(loan.getId(), customer)).thenReturn((Optional.of(loan)));

        final PayLoanRequest request = new PayLoanRequest();

        request.setLoanId(loan.getId().toString());
        request.setAmount(BigDecimal.valueOf(165));

        loanService.payLoan(UUID.randomUUID(), request);

        verify(loanInstallmentRepository).saveAll(any());
    }

    private Customer createCustomerEntity() {
        final Customer customer = new Customer();

        customer.setId(UUID.randomUUID());
        customer.setName("testName");
        customer.setSurname("testSurname");
        customer.setCreditLimit(BigDecimal.valueOf(1000));
        customer.setUsedCreditLimit(BigDecimal.ZERO);

        return customer;
    }

    private Loan createLoanEntity(final Customer customer) {
        final Loan loan = new Loan();

        loan.setId(UUID.randomUUID());
        loan.setLoanAmount(BigDecimal.valueOf(330));
        loan.setNumberOfInstallments(6);
        loan.setCreateDate(LocalDate.now());
        loan.setIsPaid(false);
        loan.setCustomer(customer);

        final List<LoanInstallment> loanInstallments = new ArrayList<>();

        for (int i = 0; i < 6; i++) {
            LoanInstallment installment = new LoanInstallment();

            installment.setAmount(BigDecimal.valueOf(55));
            installment.setPaidAmount(BigDecimal.ZERO);
            installment.setDueDate(LocalDate.now().withDayOfMonth(1).plusMonths(1 + i));
            installment.setPaid(false);
            installment.setLoan(loan);

            loanInstallments.add(installment);
        }

        loan.setInstallments(loanInstallments);

        return loan;
    }
}