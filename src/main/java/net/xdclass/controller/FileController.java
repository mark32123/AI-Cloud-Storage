package net.xdclass.controller;

import net.xdclass.dto.AccountFileDTO;
import net.xdclass.interceptor.LoginInterceptor;
import net.xdclass.service.FileService;
import net.xdclass.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 小滴课堂,愿景：让技术不再难学
 *
 * @Description
 * @Author 二当家小D
 * @Remark 有问题直接联系我，源码-笔记-技术交流群,官网 https://xdclass.net
 * @Version 1.0
 **/
@RestController
@RequestMapping("/api/file/v1")
public class FileController {

    @Autowired
    private FileService fileService;


    /**
     * 查询文件列表接口
     */
    @GetMapping("list")
    public JsonData list(@RequestParam(value = "parent_id")Long parentId){
        Long accountId = LoginInterceptor.threadLocal.get().getId();
        List<AccountFileDTO> list = fileService.listFile(accountId,parentId);
        return JsonData.buildSuccess(list);
    }


}