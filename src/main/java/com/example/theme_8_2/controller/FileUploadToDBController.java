package com.example.theme_8_2.controller;

import com.example.theme_8_2.domains.Attachment;
import com.example.theme_8_2.domains.AttachmentContent;
import com.example.theme_8_2.repository.AttachmentContentRepository;
import com.example.theme_8_2.repository.AttachmentRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * @author Saydali Murodullayev, Sat 8:13 PM. 3/12/2022
 */
@RestController
@RequestMapping("/file")
public final class FileUploadToDBController {

    private final AttachmentRepository attachmentRepository;
    private final AttachmentContentRepository attachmentContentRepository;

    /**
     *
     */
    public FileUploadToDBController(AttachmentRepository attachmentRepository,
                                    AttachmentContentRepository attachmentContentRepository) {
        this.attachmentRepository = attachmentRepository;
        this.attachmentContentRepository = attachmentContentRepository;
    }


    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public void uploadFile(MultipartHttpServletRequest request, HttpServletResponse response) throws IOException {
        Iterator<String> fileNames = request.getFileNames();
        while (fileNames.hasNext()) {
            MultipartFile file = request.getFile(fileNames.next());

            if (file != null && !file.isEmpty()) {
                String originalFilename = Objects.requireNonNull(file).getOriginalFilename();
                long size = file.getSize();
                String contentType = file.getContentType();
                String filenameExtension = StringUtils.getFilenameExtension(originalFilename);

                Attachment attachment = new Attachment();
                attachment.setOriginalName(originalFilename);
                attachment.setSize(size);
                attachment.setContentType(contentType);
                attachment.setGeneratedName(System.nanoTime() + "." + filenameExtension);

                Attachment attachment1 = attachmentRepository.save(attachment);

                AttachmentContent attachmentContent = new AttachmentContent();

                attachmentContent.setAttachment(attachment1);
                attachmentContent.setContent(file.getBytes());

                AttachmentContent attachmentContent1 = attachmentContentRepository.save(attachmentContent);
            }
        }
    }

    @RequestMapping(value = "/infos", method = RequestMethod.GET)
    public List<Attachment> attachmentListInfo() {
        return attachmentRepository.findAll();
    }

    @RequestMapping(value = "/info/{id}", method = RequestMethod.GET)
    public Attachment attachmentInfo(@PathVariable Long id) {
        Optional<Attachment> byId = attachmentRepository.findById(id);
        return byId.orElse(null);
    }

    @RequestMapping(value = "/download/{id}", method = RequestMethod.GET)
    public void download(HttpServletResponse response, @PathVariable Long id) throws IOException {
        Optional<AttachmentContent> byId = attachmentContentRepository.findById(id);
        if (byId.isPresent()) {
            AttachmentContent attachmentContent = byId.get();
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" +
                            attachmentContent.getAttachment().getOriginalName() + "\"");

            response.setContentType(attachmentContent.getAttachment().getContentType());
            FileCopyUtils.copy(attachmentContent.getContent(), response.getOutputStream());
        }


    }

    public AttachmentRepository attachmentRepository() {
        return attachmentRepository;
    }

    public AttachmentContentRepository attachmentContentRepository() {
        return attachmentContentRepository;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (FileUploadToDBController) obj;
        return Objects.equals(this.attachmentRepository, that.attachmentRepository) &&
                Objects.equals(this.attachmentContentRepository, that.attachmentContentRepository);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attachmentRepository, attachmentContentRepository);
    }

    @Override
    public String toString() {
        return "FileUploadController[" +
                "attachmentRepository=" + attachmentRepository + ", " +
                "attachmentContentRepository=" + attachmentContentRepository + ']';
    }


}
