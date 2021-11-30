package com.keeper.image.manager.model;

import com.keeper.image.manager.model.enums.ImageStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

/**
 * Entity class for storing information about image
 */
@Entity
@SequenceGenerator(
        name = "images_sequence",
        sequenceName = "images_sequence",
        initialValue = 100000000,
        allocationSize = 1
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table
public class ImageData {

    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "images_sequence"
    )
    @Id
    private Long id;

    @Column(length = 100,
            nullable = true)
    private String title;

    @Column(length = 255,
            nullable = true)
    private String description;

    @Column(nullable = false)
    private Date creationTime;

    @Column(nullable = false)
    private Date lastUpdateTime;

    @Enumerated(EnumType.STRING)
    private ImageStatus status;

    @Column(nullable = false,
            length = 36,
            unique = true)
    private String imageUid;

    @Column(length = 255)
    private String originalFileName;

    @ManyToOne
    @JoinColumn(name="user_id",
            nullable=false)
    private UserData user;

    public ImageData(String title, String description, Date creationTime, Date lastUpdateTime, UserData user, ImageStatus status, String imageUid) {
        this.title = title;
        this.description = description;
        this.creationTime = creationTime;
        this.lastUpdateTime = lastUpdateTime;
        this.user = user;
        this.status = status;
        this.imageUid = imageUid;
    }

}
