package com.example.pbac.web.security.model;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.pbac.persistence.model.security.User;
import com.example.pbac.persistence.model.security.Permission;
import com.example.pbac.persistence.model.security.Role;

public class UserFactory implements UserDetails {

    private String username;
    private String password;
    private boolean enabled;
    private Collection<GrantedAuthority> authorities;

    public UserFactory(User user) {
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.enabled = user.isActive();
        this.authorities = new ArrayList<>();
        for (Role role : user.getRoles()) {
            authorities.add(new SimpleGrantedAuthority(String.format("ROLE_%s", role.getName())));
            for (Permission permission : role.getPermissions()) {
                authorities
                        .add(new SimpleGrantedAuthority(permission.getName()));
            }
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.enabled;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.enabled;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.enabled;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

}
