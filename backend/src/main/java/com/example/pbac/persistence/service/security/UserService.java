package com.example.pbac.persistence.service.security;

import com.example.pbac.persistence.model.security.User;
import com.example.pbac.persistence.model.security.Role;
import com.example.pbac.persistence.repository.security.UserRepository;
import com.example.pbac.web.dto.NewUserDto;
import com.example.pbac.web.security.Error;
import com.example.pbac.web.security.ErrorKind;
import com.example.pbac.web.security.Result;
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

    public Result<User, Error> findById(Long id) {
        Result<User, Error> result = new Result<>();
        Optional<User> _user = repository.findById(id);
        if (_user.isEmpty()) {
            result.setErr(new Error(String.format("No se encontró al usuario con id: %d", id),
                    String.format("Cannot find user with id: %d", id), ErrorKind.RepositoryError));
            return result;
        }

        result.setOk(_user.get());
        return result;
    }

    public Result<User, Error> create(NewUserDto _newUser) {
        Result<User, Error> result = new Result<>();
        User newUser = new User();
        newUser.setUsername(_newUser.getUsername());
        newUser.setPassword(passwordEncoderProvider.passwordEncoder().encode(_newUser.getPassword()));
        newUser.setEmail(_newUser.getEmail());
        newUser.setFirstname(_newUser.getFirstname());
        newUser.setLastname(_newUser.getLastname());
        newUser.setAddress(_newUser.getAddress());
        newUser.setNumber(_newUser.getNumber());
        newUser.setProfileImage(_newUser.getProfileImage());
        newUser.setActive(true);

        try {
            repository.save(newUser);
        } catch (IllegalArgumentException err) {
            result.setErr(new Error("Error al registrar el nuevo usuario", "Error in UserService.create",
                    ErrorKind.RepositoryError));
        } finally {
            result.setOk(newUser);
        }

        return result;
    }

    public Result<User, Error> update(User user) {
        Result<User, Error> result = new Result<>();
        Optional<User> _user = repository.findById(user.getId());
        if (_user.isEmpty()) {
            result.setErr(new Error("Error al actualizar el usuario, no existe", "Error in UserService.update",
                    ErrorKind.RepositoryError));
            return result;
        }

        user = _user.get();

        try {
            user.setPassword(passwordEncoderProvider.passwordEncoder().encode(user.getPassword()));
            repository.save(user);
        } catch (IllegalArgumentException err) {
            result.setErr(new Error("Error al actualizar el usuario", "Error in UserService.update",
                    ErrorKind.RepositoryError));
        } finally {
            result.setOk(user);
        }

        return result;
    }

    public Result<Void, Error> delete(Long id) {
        Result<Void, Error> result = new Result<>();
        Optional<User> _user = repository.findById(id);
        if (_user.isEmpty()) {
            result.setErr(new Error("Error al actualizar el usuario, no existe", "Error in UserService.update",
                    ErrorKind.RepositoryError));
            return result;
        }

        User user = _user.get();
        user.setActive(!user.isActive());

        try {
            repository.save(user);
        } catch (IllegalArgumentException err) {
            result.setErr(new Error("Error al eliminar el usuario", "Error in UserService.delete",
                    ErrorKind.RepositoryError));
        }

        return result;
    }

    public Optional<User> findByUsername(String username) {
        return repository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email);
    }

}
