package com.example.theme_8_2.domains;

import lombok.*;

import javax.persistence.*;

/**
 * @author Saydali Murodullayev, Sat 8:14 PM. 3/12/2022
 */

@Getter
@Setter
@Entity
public class AttachmentContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private byte[] content;

    @OneToOne(fetch = FetchType.EAGER)
    private Attachment attachment;

}
