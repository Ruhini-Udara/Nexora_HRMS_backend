package com.hexaco.hrms.rest; //this class belongs to rest package

import com.hexaco.hrms.config.JwtUtils; //generate JWT token and validate JWT token
import com.hexaco.hrms.dto.JwtResponse; //sends token and user data to frontend
import com.hexaco.hrms.dto.LoginRequest; //receives login request email and password
import com.hexaco.hrms.models.UserAccount; //database user account entity
import com.hexaco.hrms.repository.UserAccountRepository; // finds user by email from database
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
        // These are dependencies:
        private final AuthenticationManager authenticationManager; // verifies login credentials
        private final UserAccountRepository userAccountRepository;// loads user data from DB
        private final JwtUtils jwtUtils; // creates token

        @PostMapping("/login") // defines login endpoint
        public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {// This method receives JSON
                                                                                           // body and converts it to
                                                                                           // LoginRequest
                // Rationale: We use Spring Security's AuthenticationManager to handle the
                // multi-step
                // authentication process (checking password hashes, account status, etc.)
                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(),
                                                loginRequest.getPassword()));

                // Rationale: Setting the authentication in the SecurityContext allows the
                // application
                // to recognize the user for the duration of this request.
                SecurityContextHolder.getContext().setAuthentication(authentication);

                UserAccount userAccount = userAccountRepository.findByEmail(loginRequest.getEmail()).orElseThrow(); // After
                                                                                                                    // credentials
                                                                                                                    // are
                                                                                                                    // valid,
                                                                                                                    // backend
                                                                                                                    // loads
                                                                                                                    // the
                                                                                                                    // full
                                                                                                                    // UserAccount
                                                                                                                    // from
                                                                                                                    // DB
                                                                                                                    // again
                                                                                                                    // so
                                                                                                                    // it
                                                                                                                    // can
                                                                                                                    // access
                                                                                                                    // role,
                                                                                                                    // employee,
                                                                                                                    // name,
                                                                                                                    // designation,
                                                                                                                    // etc.

                // Rationale: We inject role and employeeId into JWT claims so the frontend
                // can make authorization decisions without querying the database every time.
                Map<String, Object> claims = new HashMap<>(); // Creates JWT custom claims map.
                claims.put("role", userAccount.getRole().getRoleName()); // Adds role into token, like: ROLE_HR,
                                                                         // ROLE_ADMIN, ROLE_DIRECTOR
                claims.put("employeeId", userAccount.getEmployee().getId()); // Adds employeeId into token, like: 1, 2,
                                                                             // 3

                String jwt = jwtUtils.generateToken(userAccount.getEmail(), claims); // Generates JWT token, Email
                                                                                     // becomes token subject. Role and
                                                                                     // employeeId become token claims.

                return ResponseEntity.ok(JwtResponse.builder() // Returns HTTP 200 OK.
                                .token(jwt) // Sends generated token to frontend.
                                .email(userAccount.getEmail()) // Sends logged-in user email.
                                .id(userAccount.getEmployee().getId()) // Sends logged-in employee ID.
                                .role(userAccount.getRole().getRoleName()) // Sends logged-in employee role.
                                .name(userAccount.getEmployee().getFullName()) // Sends logged-in employee full name.
                                .designation(userAccount.getEmployee().getDesignation() != null
                                                ? userAccount.getEmployee().getDesignation().getDesignationName() // If
                                                                                                                  // employee
                                                                                                                  // has
                                                                                                                  // designation,
                                                                                                                  // send
                                                                                                                  // designation
                                                                                                                  // name.
                                                                                                                  // Otherwise
                                                                                                                  // send
                                                                                                                  // "N/A"
                                                : "N/A")
                                .epfNumber(userAccount.getEmployee().getEpfNumber()) // Sends EPF number.
                                .department(userAccount.getEmployee().getDepartment()) // Sends department name.
                                .branch(userAccount.getEmployee().getBranch()) // Sends branch name.
                                .build()); // Finishes building JwtResponse.
        }
}
