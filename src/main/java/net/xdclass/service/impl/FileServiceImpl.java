package net.xdclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.controller.req.FolderCreateReq;
import net.xdclass.dto.AccountFileDTO;
import net.xdclass.enums.BizCodeEnum;
import net.xdclass.enums.FolderFlagEnum;
import net.xdclass.exception.BizException;
import net.xdclass.mapper.AccountFileMapper;
import net.xdclass.model.AccountFileDO;
import net.xdclass.service.FileService;
import net.xdclass.util.SpringBeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.util.List;

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
public class FileServiceImpl implements FileService {

}