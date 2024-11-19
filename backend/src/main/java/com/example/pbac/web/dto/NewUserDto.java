package com.example.pbac.web.dto;

import lombok.Data;

@Data
public class NewUserDto {
    private String username;
    private String email;
    private String password;
    private String firstname;
    private String lastname;
    private String address;
    private Integer number;
    private String profileImage;
}
