package com.example.theme_8_2.domains;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * @author Saydali Murodullayev, Sat 8:15 PM. 3/12/2022
 */
@Data
@NoArgsConstructor
@Entity
public class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originalName;

    private String contentType;

    private Long size;

    private String generatedName;
}
