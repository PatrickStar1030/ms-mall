package com.dilemma.upload.controller;

import com.dilemma.upload.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("upload")
public class UploadController {
    @Autowired
    private UploadService uploadService;
    @RequestMapping("image")
    public ResponseEntity<String> uploadImage(MultipartFile file){
        return ResponseEntity.ok(uploadService.uploadImage(file));
    }
}
