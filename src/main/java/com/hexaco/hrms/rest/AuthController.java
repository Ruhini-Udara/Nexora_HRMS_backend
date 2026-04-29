package com.hexaco.hrms.rest;

import com.hexaco.hrms.config.JwtUtils;
import com.hexaco.hrms.dto.JwtResponse;
import com.hexaco.hrms.dto.LoginRequest;
import com.hexaco.hrms.models.UserAccount;
import com.hexaco.hrms.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

        private final AuthenticationManager authenticationManager;
        private final UserAccountRepository userAccountRepository;
        private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        // Rationale: We use Spring Security's AuthenticationManager to handle the multi-step 
        // authentication process (checking password hashes, account status, etc.)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        // Rationale: Setting the authentication in the SecurityContext allows the application 
        // to recognize the user for the duration of this request.
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserAccount userAccount = userAccountRepository.findByEmail(loginRequest.getEmail()).orElseThrow();
        
        // Rationale: We inject role and employeeId into JWT claims so the frontend 
        // can make authorization decisions without querying the database every time.
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", userAccount.getRole().getRoleName());
        claims.put("employeeId", userAccount.getEmployee().getId());

                String jwt = jwtUtils.generateToken(userAccount.getEmail(), claims);

                return ResponseEntity.ok(JwtResponse.builder()
                                .token(jwt)
                                .email(userAccount.getEmail())
                                .id(userAccount.getEmployee().getId())
                                .role(userAccount.getRole().getRoleName())
                                .name(userAccount.getEmployee().getFullName())
                                .designation(userAccount.getEmployee().getDesignation() != null
                                                ? userAccount.getEmployee().getDesignation().getDesignationName()
                                                : "N/A")
                                .epfNumber(userAccount.getEmployee().getEpfNumber())
                                .department(userAccount.getEmployee().getDepartment())
                                .build());
        }
}
