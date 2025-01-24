package net.xdclass.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.component.StoreEngine;
import net.xdclass.config.MinioConfig;
import net.xdclass.controller.req.FileChunkInitTaskReq;
import net.xdclass.dto.FileChunkDTO;
import net.xdclass.enums.BizCodeEnum;
import net.xdclass.exception.BizException;
import net.xdclass.mapper.FileChunkMapper;
import net.xdclass.mapper.StorageMapper;
import net.xdclass.model.FileChunkDO;
import net.xdclass.model.StorageDO;
import net.xdclass.service.FileChunkService;
import net.xdclass.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 小滴课堂,愿景：让技术不再难学
 *
 * @Description
 * @Author 二当家小D
 * @Remark 有问题直接联系我，源码-笔记-技术交流群,官网 https://xdclass.net
 * @Version 1.0
 **/
@Service
@Slf4j
public class FileChunkServiceImpl implements FileChunkService {

    @Autowired
    private StorageMapper storageMapper;


    @Autowired
    private StoreEngine fileStoreEngine;

    @Autowired
    private FileChunkMapper fileChunkMapper;


    @Autowired
    private MinioConfig minioConfig;

    /**
     * * 检查存储空间是否够( 合并文件的时候进行校验更新存储空间)
     * * 根据文件名推断内容类型
     * * 初始化分片上传,获取上传ID
     * * 创建上传任务实体并设置相关属性
     * * 将任务插入数据库，构建并返回任务信息DTO
     * @param req
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileChunkDTO initFileChunkTask(FileChunkInitTaskReq req) {

        //检查存储空间是否够( 合并文件的时候进行校验更新存储空间)
        StorageDO storageDO = storageMapper.selectOne(new QueryWrapper<>(new StorageDO())
                .eq("account_id", req.getAccountId()));
        if(storageDO.getUsedSize() + req.getTotalSize() > storageDO.getTotalSize()){
            throw new BizException(BizCodeEnum.FILE_STORAGE_NOT_ENOUGH);
        }

        String objectKey = CommonUtil.getFilePath(req.getFilename());
        //获取文件类型
        String contentType = MediaTypeFactory.getMediaType(objectKey).orElse(MediaType.APPLICATION_OCTET_STREAM).toString();
        //配置下元数据
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);

        //初始化分片上传，获取ID
        InitiateMultipartUploadResult uploadResult = fileStoreEngine.initMultipartUploadTask(minioConfig.getBucketName(), objectKey, metadata);
        String uploadId = uploadResult.getUploadId();

        int chunkNum = (int)Math.ceil(req.getTotalSize() * 1.0 / req.getChunkSize());
        FileChunkDO task = new FileChunkDO();
        task.setBucketName(minioConfig.getBucketName())
                .setChunkNum(chunkNum)
                .setChunkSize(req.getChunkSize())
                .setTotalSize(req.getTotalSize())
                .setFileName(req.getFilename())
                .setIdentifier(req.getIdentifier())
                .setObjectKey(objectKey)
                .setUploadId(uploadId)
                .setAccountId(req.getAccountId());

        //保存到数据库
        fileChunkMapper.insert(task);


        return new FileChunkDTO(task).setFinished(false).setExitPartList(new ArrayList<>());
    }

    @Override
    public String genPreSignUploadUrl(Long accountId, String identifier, int partNumber) {

        FileChunkDO task = fileChunkMapper.selectOne(new QueryWrapper<FileChunkDO>()
                .eq("account_id", accountId)
                .eq("identifier", identifier));

        if(task == null){
            throw new BizException(BizCodeEnum.FILE_CHUNK_TASK_NOT_EXISTS);
        }

        //配置预签名过期时间
        DateTime expireTime = DateUtil.offsetMillisecond(new Date(), minioConfig.getPreSignUrlExpireTime().intValue());

        //生成签名URL
        Map<String,Object> params = new HashMap<>();
        params.put("partNumber",partNumber);
        params.put("uploadId",task.getUploadId());
        URL preSignedUrl = fileStoreEngine
                .genePreSignedUrl(task.getBucketName(), task.getObjectKey(), HttpMethod.PUT, expireTime, params);

        log.info("preSignedUrl:{}",preSignedUrl);

        return preSignedUrl.toString();
    }
}