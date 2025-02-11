package net.xdclass.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.xdclass.dto.AccountFileDTO;
import net.xdclass.mapper.AccountFileMapper;
import net.xdclass.model.AccountFileDO;
import net.xdclass.service.RecycleService;
import net.xdclass.util.SpringBeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
public class RecycleServiceImpl implements RecycleService {

    @Autowired
    private AccountFileMapper accountFileMapper;

    @Override
    public List<AccountFileDTO> listRecycleFiles(Long accountId) {
        List<AccountFileDO> recycleList =  accountFileMapper.selectRecycleFiles(accountId,null);

        //如果是文件夹，就只显示文件夹，不显示里面的其他子文件
        List<Long> fileIds = recycleList.stream().map(AccountFileDO::getId).toList();

        //需要提取全部删除文件的ID，然后过滤下，如果某个文件的的父ID在这个文件ID集合里面，则不显示
        List<AccountFileDO> accountFileDOS = recycleList.stream()
                .filter(accountFileDO -> !fileIds.contains(accountFileDO.getParentId()))
                .collect(Collectors.toList());

        return SpringBeanUtil.copyProperties(accountFileDOS, AccountFileDTO.class);
    }
}