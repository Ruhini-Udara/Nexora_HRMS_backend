package com.hexaco.hrms.service;

import com.hexaco.hrms.models.UserAccount;
import com.hexaco.hrms.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserAccount userAccount = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with email: " + email));

        String role = (userAccount.getRole() != null && userAccount.getRole().getRoleName() != null) 
                ? userAccount.getRole().getRoleName() : "ROLE_EMPLOYEE";
        
        java.util.List<org.springframework.security.core.GrantedAuthority> authorities = new java.util.ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(role));
        if (!role.startsWith("ROLE_")) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        } else {
            authorities.add(new SimpleGrantedAuthority(role.substring(5)));
        }

        return new User(
                userAccount.getEmail(),
                userAccount.getPasswordHash(),
                userAccount.isActive(),
                true,
                true,
                true,
                authorities);
    }
}
