package com.inghubs.loanmanager.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inghubs.loanmanager.model.Loan;
import com.inghubs.loanmanager.repository.LoanRepository;
import com.inghubs.model.CreateLoanRequest;
import com.inghubs.model.LoanResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LoanIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LoanRepository loanRepository;

    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        authToken = obtainToken();
    }

    @Test
    void shouldCreateLoan_withValidParameters() throws Exception {
        final CreateLoanRequest request = new CreateLoanRequest();

        request.setAmount(BigDecimal.valueOf(300));
        request.setInterestRate(0.1);
        request.setNumberOfInstallments(6);

        String responseString = mockMvc.perform(post("/loan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Auth-Token", authToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.amount").value(BigDecimal.valueOf(330.0)))
                .andExpect(jsonPath("$.createDate").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.numberOfInstallments").value(6))
                .andExpect(jsonPath("$.installments.length()").value(6))
                .andExpect(jsonPath("$.installments[0].amount").value(55))
                .andReturn().getResponse().getContentAsString();

        LoanResponse loanResponse = objectMapper.readValue(responseString, LoanResponse.class);

        Optional<Loan> savedLoan = loanRepository.findById(UUID.fromString(loanResponse.getId()));
        assertTrue(savedLoan.isPresent());
    }

    private String obtainToken() throws Exception {
        return mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic YmVya2VAdGVzdC5jb206cGFzc3dvcmQ="))
                .andExpect(status().isOk())
                .andReturn().getResponse().getHeader("X-Auth-Token");
    }
}