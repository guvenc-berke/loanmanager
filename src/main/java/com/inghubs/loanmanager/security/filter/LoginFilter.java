package com.inghubs.loanmanager.security.filter;

import com.inghubs.loanmanager.model.User;
import com.inghubs.loanmanager.security.service.AuthenticationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import java.io.IOException;

public class LoginFilter extends AbstractAuthenticationProcessingFilter {

    private final AuthenticationService authenticationService;

    public LoginFilter(String loginPath, AuthenticationService authenticationService) {
        super(loginPath);
        this.authenticationService = authenticationService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        return authenticationService.login(request);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {

        User user = (User) authResult.getDetails();

        response.setHeader("X-Auth-Token", user.getToken());
    }


}
