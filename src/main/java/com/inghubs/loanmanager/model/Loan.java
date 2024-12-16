package com.inghubs.loanmanager.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "LOAN")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "LOAN_AMOUNT")
    private BigDecimal loanAmount;

    @Column(name = "NUMBER_OF_INSTALLMENTS")
    private Integer numberOfInstallments;

    @Column(name = "CREATE_DATE")
    private LocalDate createDate;

    @Column(name = "IS_PAID")
    private Boolean isPaid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CUSTOMER_ID")
    private Customer customer;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id")
    private List<LoanInstallment> installments;
}

