// roadmap/FestivalMap.java
package com.example.chookjibupuser.roadmap;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "festival_maps")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FestivalMap {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "public_id")
    private UUID publicId;

    @Column(name = "festival_id")
    private Long festivalId;

    @Column(name = "display_image_key")
    private String displayImageKey;
}