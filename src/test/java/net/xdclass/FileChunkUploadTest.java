package net.xdclass;

import lombok.extern.slf4j.Slf4j;
import net.xdclass.service.FileChunkService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 小滴课堂,愿景：让技术不再难学
 *
 * @Description
 * @Author 二当家小D
 * @Remark 有问题直接联系我，源码-笔记-技术交流群,官网 https://xdclass.net
 * @Version 1.0
 **/
@SpringBootTest
@Slf4j
public class FileChunkUploadTest {

    @Autowired
    private FileChunkService fileChunkService;

    private Long accountId = 1877928107881652225L;

    private String identifier = "fdsfadsfadsfasd";

    /**
     * 存储分片后端文件路径
     */
    private final List<String> chunkFilePaths = new ArrayList<>();

    /**
     * 存储分片上传的临时签名地址
     */
    private final List<String> chunkUploadUrls = new ArrayList<>();

    /**
     * 上传ID
     */
    private String uploadId;

    /**
     * 分片大小 5MB
     */
    private final int chunkSize = 1024 * 1024 * 5;


    /**
     * 文件分片处理，生成小的chunk文件
     */
    @Test
    public void testCreateFileChunk() {
        //将文件分片并存储
        String filepath = "/Users/xdclass/Desktop/chunk/es_note.pdf";
        File file = new File(filepath);
        long filesize = file.length();

        //计算分片数量
        int chunkNum = (int) Math.ceil(filesize * 1.0 / chunkSize);
        log.info("分片数量:{}",chunkNum);
        try(FileInputStream fis = new FileInputStream(file)){
            byte[] buffer = new byte[chunkSize];
            for(int i = 0; i<chunkNum ; i++){
                String chunkFileName = filepath + ".part"+(i+1);
                try(FileOutputStream fos = new FileOutputStream(chunkFileName)){
                    int bytesRead = fis.read(buffer);
                    fos.write(buffer,0,bytesRead);
                    log.info("创建分片文件:{}，大小{}",chunkFileName,bytesRead);
                    chunkFilePaths.add(chunkFileName);
                    log.info("分片文件存储路径:{}",chunkFilePaths);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }



}