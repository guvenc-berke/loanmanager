package com.inghubs.loanmanager.integration;

import com.inghubs.api.LoanApi;
import com.inghubs.loanmanager.model.User;
import com.inghubs.loanmanager.service.LoanService;
import com.inghubs.model.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class LoanController implements LoanApi {

    private final LoanService loanService;

    @Override
    public ResponseEntity<LoanResponse> createLoan(@Valid final CreateLoanRequest request, final String userId) {
        final UUID uuid = determineId(userId);

        return ResponseEntity.ok(loanService.createLoan(uuid, request));
    }

    @Override
    public ResponseEntity<List<LoanResponse>> listLoans(final String userId, final Integer numberOfInstallments, final Boolean isPaid) {
        final UUID uuid = determineId(userId);

        return ResponseEntity.ok(loanService.listLoans(uuid, numberOfInstallments, isPaid));
    }

    @Override
    public ResponseEntity<List<InstallmentDetail>> getInstallments(String loanId, String userId) {
        final UUID uuid = determineId(userId);

        return ResponseEntity.ok(loanService.getInstallments(uuid, loanId));
    }

    @Override
    public ResponseEntity<Void> payLoan(PayLoanRequest request, String userId) {
        final UUID uuid = determineId(userId);

        loanService.payLoan(uuid, request);

        return ResponseEntity.ok().build();
    }

    private UUID determineId(final String id) {
        final User user = (User) SecurityContextHolder.getContext().getAuthentication().getDetails();

        return switch (user.getRole()) {
            case "CUSTOMER" -> user.getId();
            case "ADMIN" -> StringUtils.isNotEmpty(id) ? UUID.fromString(id) : user.getId();
            default -> throw new IllegalArgumentException("Unexpected role: " + user.getRole());
        };

    }
}
