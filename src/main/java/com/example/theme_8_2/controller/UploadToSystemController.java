package com.example.theme_8_2.controller;

import com.example.theme_8_2.domains.Attachment;
import com.example.theme_8_2.repository.AttachmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * @author Saydali Murodullayev, Tue 9:44 AM. 3/15/2022
 */
@Slf4j
@RestController
@RequestMapping(value = "/system")
public class UploadToSystemController {

    final
    AttachmentRepository repository;
    //    @Autowired
    private final ServletContext servletContext;


    public static final String URL = "D:\\system/files";
    public static final Path PATH = Paths.get(URL);

    public UploadToSystemController(AttachmentRepository repository,
                                    ServletContext servletContext) {
        this.repository = repository;
        this.servletContext = servletContext;
    }

    @PostConstruct
    public void init() {
        if (!Files.exists(PATH)) {
            try {
                Files.createDirectories(PATH);
            } catch (IOException e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
        }
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public HttpResponse<?> fileUploadSystem(MultipartHttpServletRequest request,
                                            HttpServletResponse response) {
        Iterator<String> fileNames = request.getFileNames();
        while (fileNames.hasNext()) {
            MultipartFile file = request.getFile(fileNames.next());

            if (!Objects.requireNonNull(file).isEmpty()) {
                String originalFilename = file.getOriginalFilename();
                long size = file.getSize();
                String contentType = file.getContentType();
                String filenameExtension = StringUtils.getFilenameExtension(originalFilename);

                Attachment attachment = new Attachment();
                attachment.setOriginalName(originalFilename);
                attachment.setSize(size);
                attachment.setContentType(contentType);

                String uuid = String.valueOf(UUID.randomUUID());
                String generatedName = "%s.%s".formatted(uuid, filenameExtension);
                attachment.setGeneratedName(generatedName);
                repository.save(attachment);

                Path rootPath = Paths.get(URL, generatedName);
                try {
                    Files.copy(file.getInputStream(), rootPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @RequestMapping(value = "/attachment", method = RequestMethod.GET)
    public List<Attachment> attachmentList() {
        List<Attachment> all = repository.findAll();
        return all;
    }


    @RequestMapping(value = "/download/{id}", method = RequestMethod.GET)
    public ResponseEntity<?> downloadFile(@PathVariable Long id) {
        Optional<Attachment> byId = repository.findById(id);
        if (byId.isPresent()) {
            Attachment attachment = byId.get();
            String contentType = attachment.getContentType();
            String generatedName = attachment.getGeneratedName();
            String originalName = attachment.getOriginalName();
            Long size = attachment.getSize();


            MediaType mediaTypeForFileName = getMediaTypeForFileName(this.servletContext, originalName);


            File file = new File(URL + "/" + generatedName);
            InputStreamResource resource = null;
            
            try {
                resource = new InputStreamResource(new FileInputStream(file));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + originalName)
                    .contentType(MediaType.valueOf(contentType))
                    .contentLength(size)
                    .body(resource);

        } else
            return null;
    }

    private MediaType getMediaTypeForFileName(ServletContext servletContext, String originalName) {
        String mimeType = servletContext.getMimeType(originalName);
        try {
            return MediaType.parseMediaType(mimeType);
        } catch (Exception e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }

    }


}
