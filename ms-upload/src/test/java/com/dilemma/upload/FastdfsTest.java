package com.dilemma.upload;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.domain.ThumbImageConfig;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class FastdfsTest {
    @Autowired
    private FastFileStorageClient storageClient;
    @Autowired
    private ThumbImageConfig thumbImageConfig;

    /**
     * 测试使用fastdfsclient上传图片
     * @throws FileNotFoundException
     */
    @Test
    public void testUpload() throws FileNotFoundException {
        File file = new File("D:/static/background/1.jpg");
        StorePath storePath = this.storageClient.uploadFile(
                new FileInputStream(file),file.length(),"jpg",null);
        System.out.println(storePath.getFullPath());
        System.out.println(storePath.getPath());
    }


    @Test
    public void testUploadAndCreateThumb() throws FileNotFoundException {
        File file = new File("D:/static/background/Capture001.png");
        StorePath storePath = this.storageClient.uploadImageAndCrtThumbImage(new FileInputStream(file),file.length(),"png",null);
        //带分组路径
        System.out.println(storePath.getFullPath());
        //不带分组路径
        System.out.println(storePath.getPath());
        String thumbImagePath = thumbImageConfig.getThumbImagePath(storePath.getPath());
        System.out.println(thumbImagePath);
    }
    @Test
    public void testReadImg() throws IOException {
        File file = new File("D:/static/background/钢之炼金术师.jpg");
        BufferedImage read = ImageIO.read(new FileInputStream(file));
        if (read!=null){
            System.out.println("读取成功！！！");
        }
    }


}
