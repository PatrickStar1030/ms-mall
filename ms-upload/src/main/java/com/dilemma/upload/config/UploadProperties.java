package com.dilemma.upload.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
@Data
@ConfigurationProperties(prefix = "mall.upload")
public class UploadProperties {
    private String BaseUrl;
    private List<String> allowTypes;
}
