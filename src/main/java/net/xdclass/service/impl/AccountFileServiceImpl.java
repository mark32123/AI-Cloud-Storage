package net.xdclass.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.xdclass.controller.req.FolderCreateReq;
import net.xdclass.mapper.AccountFileMapper;
import net.xdclass.mapper.FileMapper;
import net.xdclass.service.AccountFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 小滴课堂,愿景：让技术不再难学
 *
 * @Description
 * @Author 二当家小D
 * @Remark 有问题直接联系我，源码-笔记-技术交流群,官网 https://xdclass.net
 * @Version 1.0
 **/
@Slf4j
@Service
public class AccountFileServiceImpl implements AccountFileService {

    @Autowired
    private AccountFileMapper accountFileMapper;

    @Autowired
    private FileMapper fileMapper;

    @Override
    public void createFolder(FolderCreateReq createRootFolderReq) {

    }
}