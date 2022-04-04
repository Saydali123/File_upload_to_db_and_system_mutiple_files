package com.example.theme_8_2.repository;

import com.example.theme_8_2.domains.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Saydali Murodullayev, Sat 8:21 PM. 3/12/2022
 */
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
}
