package com.ecommerce.store.security;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class AuthUser implements UserDetails {

    private final Long id;
    private final String email;
    private final String type; // customer | admin
    private final String role;
    private final String passwordHash;

    public AuthUser(Long id, String email, String type, String role, String passwordHash) {
        this.id = id;
        this.email = email;
        this.type = type;
        this.role = role;
        this.passwordHash = passwordHash;
    }

    public Long getId() { return id; }
    public String getType() { return type; }
    public String getRole() { return role; }
    public boolean isAdmin() { return "admin".equals(type); }
    public boolean isCustomer() { return "customer".equals(type); }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (isAdmin()) {
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_" + role));
        }
        return List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
    }

    @Override
    public String getPassword() { return passwordHash; }

    @Override
    public String getUsername() { return email; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
