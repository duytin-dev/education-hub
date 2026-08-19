package com.iTech.education.support;

import com.iTech.education.entity.User;
import com.iTech.education.security.JwtTokenProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public final class TestJwtSupport {

    private TestJwtSupport() {
    }

    public static String bearerToken(JwtTokenProvider tokenProvider, User user) {
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                .build();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        return "Bearer " + tokenProvider.generateToken(authentication);
    }
}
