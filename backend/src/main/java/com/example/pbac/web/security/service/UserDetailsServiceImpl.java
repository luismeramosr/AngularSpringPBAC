package com.example.pbac.web.security.service;

import com.example.pbac.persistence.model.security.User;
import com.example.pbac.persistence.service.security.UserService;
import com.example.pbac.web.security.model.UserFactory;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserService service;

    @Override
    public UserFactory loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = service.findByEmail(username);
        if (user.isPresent()) {
            return new UserFactory(user.get());
        }
        throw new UsernameNotFoundException(String.format("No se pudo encontrar al usuario \"%s\".", username));
    }

}
