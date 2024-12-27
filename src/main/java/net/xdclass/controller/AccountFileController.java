package net.xdclass.controller;

import net.xdclass.controller.req.FileUpdateReq;
import net.xdclass.controller.req.FolderCreateReq;
import net.xdclass.dto.AccountFileDTO;
import net.xdclass.interceptor.LoginInterceptor;
import net.xdclass.service.AccountFileService;
import net.xdclass.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
public class AccountFileController {

    @Autowired
    private AccountFileService accountFileService;


    /**
     * 查询文件列表接口
     */
    @GetMapping("list")
    public JsonData list(@RequestParam(value = "parent_id")Long parentId){
        Long accountId = LoginInterceptor.threadLocal.get().getId();
        List<AccountFileDTO> list = accountFileService.listFile(accountId,parentId);
        return JsonData.buildSuccess(list);
    }


    /**
     * 创建文件夹
     */
    @PostMapping("create_folder")
    public JsonData createFolder(@RequestBody FolderCreateReq req){
        Long accountId = LoginInterceptor.threadLocal.get().getId();
        req.setAccountId(accountId);
        accountFileService.createFolder(req);
        return JsonData.buildSuccess();
    }


    /**
     * 文件重命名
     */
    @PostMapping("rename_file")
    public JsonData renameFile(@RequestBody FileUpdateReq req){
        Long accountId = LoginInterceptor.threadLocal.get().getId();
        req.setAccountId(accountId);
        accountFileService.renameFile(req);
        return JsonData.buildSuccess();
    }



}