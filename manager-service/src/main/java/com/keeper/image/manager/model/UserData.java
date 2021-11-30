package com.keeper.image.manager.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

/**
 * Entity class for storing information about user
 */
@Table
@Entity
@SequenceGenerator(
        name = "users_sequence",
        sequenceName = "users_sequence",
        initialValue = 100000000,
        allocationSize = 1
)
@NoArgsConstructor
@Getter
@Setter
public class UserData {

    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "users_sequence"
    )
    @Id
    private Long id;

    @Column(length = 100,
            nullable = false,
            unique = true)
    private String name;

    @OneToMany(mappedBy="user",
            fetch = FetchType.EAGER,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private Set<ImageData> images;

    public UserData(String name) {
        this.name = name;
    }
}
