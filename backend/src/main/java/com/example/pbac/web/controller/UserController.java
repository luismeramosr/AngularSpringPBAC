package com.example.pbac.web.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.pbac.persistence.model.security.User;
import com.example.pbac.persistence.service.security.UserService;
import com.example.pbac.web.security.Result;

import jakarta.websocket.server.PathParam;

import com.example.pbac.web.dto.NewUserDto;
import com.example.pbac.web.security.Error;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService service;

    @PreAuthorize("hasAuthority('user_findAll')")
    @GetMapping("/findAll")
    public ResponseEntity<Result<List<User>, Error>> findAll() {
        Result<List<User>, Error> result = new Result<>();
        result.setOk(service.findAll());

        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAuthority('user_findById')")
    @GetMapping("/{id}")
    public ResponseEntity<Result<User, Error>> findById(@PathVariable("id") Long id) {
        Result<User, Error> result = service.findById(id);

        if (result.isErr()) {
            return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAuthority('user_create')")
    @PostMapping("/create")
    public ResponseEntity<Result<User, Error>> create(@RequestBody NewUserDto newUser) {
        Result<User, Error> result = service.create(newUser);

        if (result.isErr()) {
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAuthority('user_update')")
    @PutMapping("/update")
    public ResponseEntity<Result<User, Error>> update(@RequestBody User user) {
        Result<User, Error> result = service.update(user);

        if (result.isErr()) {
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAuthority('user_delete')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Result<Void, Error>> delete(@PathVariable("id") Long id) {
        Result<Void, Error> result = service.delete(id);

        if (result.isErr()) {
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok(result);
    }

}
