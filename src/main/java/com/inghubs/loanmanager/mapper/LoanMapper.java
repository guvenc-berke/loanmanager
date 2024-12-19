package com.inghubs.loanmanager.mapper;

import com.inghubs.loanmanager.model.Loan;
import com.inghubs.loanmanager.model.LoanInstallment;
import com.inghubs.model.InstallmentDetail;
import com.inghubs.model.InstallmentItem;
import com.inghubs.model.LoanResponse;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Objects;

@UtilityClass
public class LoanMapper {

    public LoanResponse map(final Loan loan) {

        final List<InstallmentItem> installmentItems = loan.getInstallments().stream()
                .map(installment ->
                        new InstallmentItem()
                                .amount(installment.getAmount())
                                .dueDate(installment.getDueDate().toString())
                                .isPaid(installment.isPaid()))
                .toList();

        return new LoanResponse()
                .id(loan.getId().toString())
                .amount(loan.getLoanAmount())
                .createDate(loan.getCreateDate().toString())
                .numberOfInstallments(loan.getNumberOfInstallments())
                .isPaid(loan.getIsPaid())
                .installments(installmentItems);
    }

    public LoanResponse map(final Loan loan, final List<LoanInstallment> installments) {

        final List<InstallmentItem> installmentItems = installments.stream()
                .map(installment ->
                        new InstallmentItem()
                                .amount(installment.getAmount())
                                .dueDate(installment.getDueDate().toString())
                                .isPaid(installment.isPaid()))
                .toList();

        return new LoanResponse()
                .id(loan.getId().toString())
                .amount(loan.getLoanAmount())
                .createDate(loan.getCreateDate().toString())
                .numberOfInstallments(loan.getNumberOfInstallments())
                .isPaid(loan.getIsPaid())
                .installments(installmentItems);
    }

    public InstallmentDetail map(final LoanInstallment installment) {
        InstallmentDetail installmentDetail = new InstallmentDetail()
                .paidAmount(installment.getPaidAmount())
                .dueDate(installment.getDueDate().toString())
                .amount(installment.getAmount())
                .isPaid(installment.isPaid());

        if (Objects.nonNull(installment.getPaymentDate())) {
            installmentDetail.setPaymentDate(installment.getPaymentDate().toString());
        }

        return installmentDetail;
    }
}
