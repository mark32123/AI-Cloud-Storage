package net.xdclass.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.controller.req.FileUpdateReq;
import net.xdclass.controller.req.FolderCreateReq;
import net.xdclass.dto.AccountFileDTO;
import net.xdclass.dto.FolderTreeNodeDTO;
import net.xdclass.enums.BizCodeEnum;
import net.xdclass.enums.FolderFlagEnum;
import net.xdclass.exception.BizException;
import net.xdclass.mapper.AccountFileMapper;
import net.xdclass.mapper.FileMapper;
import net.xdclass.model.AccountFileDO;
import net.xdclass.service.AccountFileService;
import net.xdclass.util.SpringBeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

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

    /**
     * 获取文件列表接口
     * @param accountId
     * @param parentId
     * @return
     */
    @Override
    public List<AccountFileDTO> listFile(Long accountId, Long parentId) {

        List<AccountFileDO> accountFileDOList = accountFileMapper.selectList(new QueryWrapper<AccountFileDO>()
                .eq("account_id", accountId).eq("parent_id", parentId)
                .orderByDesc("is_dir")
                .orderByDesc("gmt_create")
        );

        return SpringBeanUtil.copyProperties(accountFileDOList, AccountFileDTO.class);
    }

    /**
     * 创建文件夹
     * @param req
     */
    @Override
    public Long createFolder(FolderCreateReq req) {

        AccountFileDTO accountFileDTO = AccountFileDTO.builder().accountId(req.getAccountId())
                .parentId(req.getParentId())
                .fileName(req.getFolderName())
                .isDir(FolderFlagEnum.YES.getCode())
                .build();

        return saveAccountFile(accountFileDTO);

    }

    /**
     * 重命名文件
     * 1、检查ID是否存在
     * 2、新旧文件名称不能一样
     * 3、同层文件名称不能一样
     * @param req
     */
    @Override
    public void renameFile(FileUpdateReq req) {
        //检查ID是否存在
        AccountFileDO accountFileDO = accountFileMapper.selectOne(new QueryWrapper<AccountFileDO>()
                .eq("id", req.getFileId()).eq("account_id", req.getAccountId()));

        if(accountFileDO == null){
            log.error("文件不存在,{}",req);
            throw new BizException(BizCodeEnum.FILE_NOT_EXISTS);
        }else {
            //新旧文件名称不能一样
            if(Objects.equals(accountFileDO.getFileName(), req.getNewFilename())){
                log.error("文件名称重复,{}",req);
                throw new BizException(BizCodeEnum.FILE_RENAME_REPEAT);
            }
            //同层文件名称不能一样
            Long selectCount = accountFileMapper.selectCount(new QueryWrapper<AccountFileDO>()
                    .eq("account_id", req.getAccountId())
                    .eq("parent_id", accountFileDO.getParentId())
                    .eq("file_name", req.getNewFilename()));
            if(selectCount>0){
                log.error("文件名称重复,{}",req);
                throw new BizException(BizCodeEnum.FILE_RENAME_REPEAT);
            }else {
                accountFileDO.setFileName(req.getNewFilename());
                accountFileMapper.updateById(accountFileDO);
            }
        }

    }

    /**
     * 查询文件树接口 （非递归方式）
     * 1、查询用户全部文件夹
     * 2、拼装文件树
     * @param accountId
     * @return
     */
    @Override
    public List<FolderTreeNodeDTO> folderTree(Long accountId) {
        //查询用户全部文件夹
        List<AccountFileDO> folderList = accountFileMapper.selectList(new QueryWrapper<AccountFileDO>()
                .eq("account_id", accountId)
                .eq("is_dir", FolderFlagEnum.YES.getCode())
        );

        if(CollectionUtils.isEmpty(folderList)){
            return List.of();
        }
        //构建一个map， key是文件ID，value是文件对象 相当于一个数据源
        Map<Long, FolderTreeNodeDTO> folderMap = folderList.stream()
                .collect(Collectors.toMap(AccountFileDO::getId, accountFileDO ->
                FolderTreeNodeDTO.builder()
                        .id(accountFileDO.getId())
                        .parentId(accountFileDO.getParentId())
                        .label(accountFileDO.getFileName())
                        .children(new ArrayList<>())
                        .build()
        ));

        //构建文件树，遍历数据源，为每个文件夹找到子文件夹
        for (FolderTreeNodeDTO node : folderMap.values()) {
            Long parentId = node.getParentId();

            if(parentId!=null && folderMap.containsKey(parentId)){
                //获取父文件
                FolderTreeNodeDTO parentNode = folderMap.get(parentId);
                //获取父文件夹的子节点位置
                List<FolderTreeNodeDTO> children = parentNode.getChildren();
                //将当前节点添加到对应的文件夹里面
                children.add(node);
            }

        }

        //过滤根节点，即parentID是0的
        List<FolderTreeNodeDTO> rootFolderList = folderMap.values().stream()
                .filter(node -> Objects.equals(node.getParentId(), 0L))
                .collect(Collectors.toList());


        return rootFolderList;

    }

    /**
     * 查询文件树接口 （非递归方式）
     * 1、查询用户全部文件夹
     * 2、拼装文件树
     * @param accountId
     * @return
     */
    @Override
    public List<FolderTreeNodeDTO> folderTreeV2(Long accountId) {
        //查询用户全部文件夹
        List<AccountFileDO> folderList = accountFileMapper.selectList(new QueryWrapper<AccountFileDO>()
                .eq("account_id", accountId)
                .eq("is_dir", FolderFlagEnum.YES.getCode())
        );

        if(CollectionUtils.isEmpty(folderList)){
            return List.of();
        }

        List<FolderTreeNodeDTO> folderTreeNodeDTOList = folderList.stream().map(file -> {
            return FolderTreeNodeDTO.builder()
                    .id(file.getId())
                    .parentId(file.getParentId())
                    .label(file.getFileName())
                    .children(new ArrayList<>())
                    .build();
        }).toList();

        //根据父文件ID进行分组，key是当前文件夹ID，value是对应的子文件夹列表，也是数据源
        Map<Long, List<FolderTreeNodeDTO>> folderMap = folderTreeNodeDTOList
                .stream().collect(Collectors.groupingBy(FolderTreeNodeDTO::getParentId));


        //处理拼装文件树
        for(FolderTreeNodeDTO node : folderTreeNodeDTOList){
            List<FolderTreeNodeDTO> children = folderMap.get(node.getId());
            //判断是否为空
            if(!CollectionUtils.isEmpty(children)){
                node.getChildren().addAll(children);
            }
        }

        //过滤根节点，即parentID是0的
        List<FolderTreeNodeDTO> folderTreeNodeDTOS = folderTreeNodeDTOList.stream().filter(node -> Objects.equals(node.getParentId(), 0L))
                .collect(Collectors.toList());

        return folderTreeNodeDTOS;

    }

    /**
     * 处理用户和文件的关系，存储文件和文件夹都是可以的
     *
     * 1、检查父文件是否存在
     * 2、检查文件是否重复
     * 3、保存相关文件关系
     *
     * @param accountFileDTO
     * @return
     */
    private Long saveAccountFile(AccountFileDTO accountFileDTO) {
        //检查父文件是否存在
        checkParentFileId(accountFileDTO);

        AccountFileDO accountFileDO = SpringBeanUtil.copyProperties(accountFileDTO, AccountFileDO.class);

        //检查文件是否重复 aa  aa(1) aa(2)
        processFileNameDuplicate(accountFileDO);

        //保存相关文件关系
        accountFileMapper.insert(accountFileDO);

        return accountFileDO.getId();
    }

    /**
     * 处理文件是否重复,
     *  文件夹重复和文件名重复处理规则不一样
     * @param accountFileDO
     */
    private void processFileNameDuplicate(AccountFileDO accountFileDO) {

        Long selectCount = accountFileMapper.selectCount(new QueryWrapper<AccountFileDO>()
                .eq("account_id", accountFileDO.getAccountId())
                .eq("parent_id", accountFileDO.getParentId())
                .eq("is_dir", accountFileDO.getIsDir())
                .eq("file_name", accountFileDO.getFileName()));

        if(selectCount>0){
            //处理重复文件夹
            if(Objects.equals(accountFileDO.getIsDir(), FolderFlagEnum.YES.getCode())){
                accountFileDO.setFileName(accountFileDO.getFileName()+"_"+System.currentTimeMillis());
            }else {
                //处理重复文件名,提取文件拓展名
                String[] split = accountFileDO.getFileName().split("\\.");
                accountFileDO.setFileName(split[0]+"_"+System.currentTimeMillis()+"."+split[1]);
            }
        }


    }

    /**
     * 检查父文件是否存在
     * @param accountFileDTO
     */
    private void checkParentFileId(AccountFileDTO accountFileDTO) {
        if(accountFileDTO.getParentId()!=0){
            AccountFileDO accountFileDO = accountFileMapper.selectOne(
                    new QueryWrapper<AccountFileDO>()
                            .eq("id", accountFileDTO.getParentId())
                            .eq("account_id", accountFileDTO.getAccountId()));

            if(accountFileDO == null){
                throw new BizException(BizCodeEnum.FILE_NOT_EXISTS);
            }
        }

    }
}