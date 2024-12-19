package com.inghubs.loanmanager.security.service;

import com.inghubs.loanmanager.model.User;
import com.inghubs.loanmanager.repository.UserRepository;
import com.inghubs.loanmanager.security.model.CustomUserDetails;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    @Value("${jwt.secretKey}")
    private String secretKey;

    @Value("${jwt.expirationTime}")
    private long expirationTime;

    private static final String TOKEN_HEADER = "X-Auth-Token";

    private final UserRepository userRepository;
    private final CustomUserDetailsService userDetailsService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Authentication login(final HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");

        final String[] credentials = getCredentials(authHeader);
        final String username = credentials[0];
        final String password = credentials[1];

        if (StringUtils.isAnyEmpty(username, password)) {
            throw new UsernameNotFoundException("Credentials are empty!");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new UsernameNotFoundException("Invalid username or password!");
        }

        final User user = ((CustomUserDetails) userDetails).getUser();
        final String token = user.getToken();

        if (isTokenExpired(token)) {
            user.setToken(generateToken(username));
        }

        final UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, null);
        authentication.setDetails(user);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        userRepository.save(user);

        return authentication;
    }

    public Authentication validateRequest(final HttpServletRequest request) {
        final String token = request.getHeader(TOKEN_HEADER);
        String username;

        if (StringUtils.isNotEmpty(token)) {
            try {
                username = Jwts.parser()
                        .setSigningKey(secretKey)
                        .parseClaimsJws(token)
                        .getBody()
                        .getSubject();
            } catch (final Exception ex) {
                return null;
            }

            final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            final User user = ((CustomUserDetails) userDetails).getUser();
            final String userToken = user.getToken();

            if (StringUtils.isNotEmpty(userToken) && token.equals(userToken)) {
                final UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, null);
                authentication.setDetails(user);

                return authentication;
            }
        }

        return null;
    }

    private String[] getCredentials(final String authHeader) {
        final String base64Credentials = authHeader.substring("Basic".length()).trim();
        final String credentialsStr = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);

        return credentialsStr.split(":", 2);
    }

    private boolean isTokenExpired(final String token) {
        if (StringUtils.isNotEmpty(token)) {
                Date expiration = Jwts.parser()
                        .setSigningKey(secretKey)
                        .parseClaimsJws(token)
                        .getBody()
                        .getExpiration();


            return  !expiration.before(new Date());
        } else {
            return true;
        }
    }

    private String generateToken(final String username) {
        final Date now = new Date();
        final Date expiryDate = new Date(now.getTime() + expirationTime);

        final Map<String, Object> claims = new HashMap<>();
        claims.put("type", "LOGIN_TOKEN");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }
}

