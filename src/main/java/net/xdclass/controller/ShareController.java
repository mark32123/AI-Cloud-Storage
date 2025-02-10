package net.xdclass.controller;

import lombok.AllArgsConstructor;
import net.xdclass.dto.ShareDTO;
import net.xdclass.service.ShareService;
import net.xdclass.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
@RequestMapping("/api/share/v1")
public class ShareController {


    @Autowired
    private ShareService shareService;

    /**
     * 获取我的个人分享列表接口
     */
    @GetMapping("list")
    public JsonData list(){
        List<ShareDTO> list = shareService.listShare();
        return JsonData.buildSuccess(list);
    }



}