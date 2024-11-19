package com.example.pbac.persistence.service.security;

import com.example.pbac.persistence.model.security.User;
import com.example.pbac.persistence.model.security.Role;
import com.example.pbac.persistence.repository.security.UserRepository;
import com.example.pbac.web.security.model.UserFactory;
//import com.example.pbac.web.security.model.RegisterRequest;
import com.example.pbac.web.security.provider.PasswordEncoderProvider;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoderProvider passwordEncoderProvider;
    private final UserRepository repository;

    public UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                User user = repository.findByUsername(username)
                        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

                UserFactory userDetails = new UserFactory(user);

                return userDetails;
            }
        };
    }

    public List<User> findAll() {
        return repository.findAll();
    }

    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    // public User register(RegisterRequest request) {
    // User user = new User();
    // // Default role
    // Role default_role = Role.getDefault();
    // List<Role> roles = new ArrayList<>();
    // roles.add(default_role);
    //
    // user.setEmail(request.getEmail());
    // user.setFirstname(request.getFirstname());
    // user.setLastname(request.getLastname());
    // user.setRoles(roles);
    // user.setIsPremium(false);
    // user.setPassword(passwordEncoderProvider.passwordEncoder().encode(request.getPassword()));
    //
    // // WARN: por ahora para que sirva crear un usuario hay que agregar estos
    // datos
    // user.setActive(true);
    // user.setCreated_at(Date.from(Instant.now()));
    // user.setUpdated_at(Date.from(Instant.now()));
    // user.setCreated_by("SYSTEM");
    // user.setUpdated_by("SYSTEM");
    //
    // return repository.save(user);
    // }

    // public User updateUser(User user) {
    // var passwordEncoder = new BCryptPasswordEncoder();
    // user.setPassword(passwordEncoder.encode(user.getPassword()));
    // return repository.save(user);
    // }

    // public String updateAddressPhone(User user) {
    // Optional<User> userExist = repository.findById(user.getId());
    // if (userExist.isPresent()) {
    // User existingUser = userExist.get();
    // existingUser.setAddress(user.getAddress());
    // existingUser.setNumber(user.getNumber());
    // repository.save(existingUser);
    // return "Datos actualizados";
    // } else {
    // return "El usuario no existe";
    // }
    // }

}
