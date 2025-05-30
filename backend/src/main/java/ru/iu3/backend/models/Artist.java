package ru.iu3.backend.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "artists")
public class Artist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 128, unique = true)
    private String name;

    @ManyToOne
    @JoinColumn(name = "countryId", referencedColumnName = "id")
    @JsonIgnore
    private Countries country;

    @Column(name = "age", length = 45)
    private String age;

    @OneToMany(mappedBy = "artist")
    @JsonIgnore
    private List<Painting> paintings;

}
