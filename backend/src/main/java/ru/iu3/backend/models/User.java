package ru.iu3.backend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.*;


@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login", nullable = false, length = 45, unique = true)
    private String login;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "password",  length = 64)
    private String password;

    @Column(name = "email", nullable = false, length = 45, unique = true)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "salt", length = 64)
    private String salt;

    @Column(name = "token", length = 256)
    private String token;

    @Column(name = "activity")
    @Temporal(TemporalType.TIMESTAMP)
    private Date activity;

    @ManyToMany
    @JoinTable(
            name = "usersmuseums",
            joinColumns = @JoinColumn(name = "userid"),
            inverseJoinColumns = @JoinColumn(name = "museumid")
    )
    private Set<Museum> museums;


}
