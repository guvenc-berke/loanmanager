package com.inghubs.loanmanager.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
@Table(name = "LOAN_INSTALLMENT")
public class LoanInstallment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "AMOUNT")
    private BigDecimal amount;

    @Column(name = "PAID_AMOUNT")
    private BigDecimal paidAmount;

    @Column(name = "DUE_DATE")
    private LocalDate dueDate;

    @Column(name = "PAYMENT_DATE")
    private LocalDate paymentDate;

    @Column(name = "IS_PAID")
    private boolean isPaid;
}
