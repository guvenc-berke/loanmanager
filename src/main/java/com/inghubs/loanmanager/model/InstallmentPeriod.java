package com.inghubs.loanmanager.model;

import com.inghubs.loanmanager.exception.ServiceException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum InstallmentPeriod {

    SIX_MONTHS(6),
    NINE_MONTHS(9),
    ONE_YEAR(12),
    TWO_YEARS(24);

    private final Integer numberOfInstallments;

    public static void validateNumberOfInstallments(final Integer numOfInstallments) {
        Arrays.stream(values())
                .filter(installmentPeriod -> installmentPeriod.numberOfInstallments.equals(numOfInstallments))
                .findFirst()
                .orElseThrow(() -> new ServiceException(HttpStatus.BAD_REQUEST, "Invalid number of installments!"));
    }
}
