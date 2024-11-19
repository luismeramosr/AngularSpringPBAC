package com.example.pbac.persistence.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String email;
    private String password;
    private String firstname;
    private String lastname;
    private String address;
    private Integer number;
    private String profileImage;
    private Boolean isPremium;
    private Double salaryPerHours;

    @NotBlank
    @Column(nullable = false)
    private Date created_at;

    @NotBlank
    @Column(nullable = false)
    private String created_by;

    @NotBlank
    @Column(nullable = false)
    private Date updated_at;

    @NotBlank
    @Column(nullable = false)
    private String updated_by;

    @NotBlank
    @Column(nullable = false)
    private boolean active;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "users_roles", joinColumns = @JoinColumn(name = "users_id"), inverseJoinColumns = @JoinColumn(name = "roles_id"))
    private List<Role> roles;

}
