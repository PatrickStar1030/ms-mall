package com.dilemma.upload.service.impl;

import com.dilemma.common.enums.ExceptionEnum;
import com.dilemma.common.exception.MsException;
import com.dilemma.upload.config.UploadProperties;
import com.dilemma.upload.service.UploadService;
import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;

@Service
@Slf4j
@EnableConfigurationProperties(UploadProperties.class)
public class UploadServiceImpl implements UploadService {
    @Autowired
    private FastFileStorageClient fastFileStorageClient;

    @Autowired
    private UploadProperties prop;


    //private static final List<String> ALLOW_TYPES = Arrays.asList("image/jpeg","image/png","image/bmp");
    @Override
    public String uploadImage(MultipartFile file) {
        //获取根目录
        //String path = this.getClass().getClassLoader().getResource("").getFile();
        //非空判断
        try {
            //校验文件是否非法
            String contentType = file.getContentType();
            if (!prop.getAllowTypes().contains(contentType)) {
                log.info("文件类型不匹配");
                throw new MsException(ExceptionEnum.INVALID_FILE_TYPE);
            }
            //校验文件内容
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                log.info("不是图片文件！");
                throw new MsException(ExceptionEnum.INVALID_FILE_TYPE);
            }
            //上传到fastDfs
            String extension = StringUtils.substringAfterLast(file.getOriginalFilename(), ".");
            StorePath storePath = fastFileStorageClient.uploadFile(file.getInputStream(), file.getSize(), extension, null);
            return prop.getBaseUrl() + storePath.getFullPath();
        } catch (IOException e) {
            log.error("上传文件失败", e);
            throw new MsException(ExceptionEnum.UPLOAD_FILE_ERROR);
        }
    }
}
