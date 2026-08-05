package com.iTech.education.security;

import com.iTech.education.entity.User;
import com.iTech.education.service.UserService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserService userService;
    public CustomUserDetailsService(UserService userService){
        this.userService = userService;
    }
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
          User user = this.userService.handleGetUserByUsername(email);
          return new org.springframework.security.core.userdetails.User(
                  user.getEmail(),user.getPassword(), Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
          );



    }


}
