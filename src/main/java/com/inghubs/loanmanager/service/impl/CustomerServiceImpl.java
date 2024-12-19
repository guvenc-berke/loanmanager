package com.inghubs.loanmanager.service.impl;

import com.inghubs.loanmanager.exception.ServiceException;
import com.inghubs.loanmanager.model.Customer;
import com.inghubs.loanmanager.repository.CustomerRepository;
import com.inghubs.loanmanager.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    public void save(final Customer customer) {
        customerRepository.save(customer);
    }

    @Override
    public Customer findByUserId(final UUID userId) {

        final Customer customer = customerRepository.findByUserId(userId);
        if (Objects.isNull(customer)) {
            throw new ServiceException(HttpStatus.NOT_FOUND, "Customer info not found!");
        }

        return customer;
    }
}
