package com.inghubs.loanmanager.service;

import com.inghubs.loanmanager.model.Customer;

import java.util.UUID;

public interface CustomerService {

    void save(Customer customer);

    Customer findByUserId(UUID userId);
}
