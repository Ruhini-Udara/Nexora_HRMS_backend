package com.hexaco.hrms.dto; //this class belongs to dto package

//this file defines the login request body

import lombok.*; //import lombok annotations

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest { // Defines request DTO class.
    private String email; // Stores email from frontend login form.
    private String password; // Stores password from frontend login form.
}

// LoginRequest is the DTO used by AuthController to receive login credentials
// from the frontend.