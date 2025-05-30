package ru.iu3.backend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "museums")
public class Museum {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "location", length = 128)
    private String location;

    @OneToMany(mappedBy = "museum")
    private List<Painting> paintings;

    @ManyToMany(mappedBy = "museums")
    @JsonIgnore
    private Set<User> users;
}
