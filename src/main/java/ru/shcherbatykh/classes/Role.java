package ru.shcherbatykh.classes;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.Set;

public enum Role {
    ADMIN, USER;

    public Set<SimpleGrantedAuthority> getAuthorities() {
        Set<SimpleGrantedAuthority> s = Collections.singleton(new SimpleGrantedAuthority(this.name()));
        return Collections.singleton(new SimpleGrantedAuthority(this.name()));
    }
}