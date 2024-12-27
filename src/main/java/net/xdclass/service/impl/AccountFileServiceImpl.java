package net.xdclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.controller.req.FileUpdateReq;
import net.xdclass.controller.req.FolderCreateReq;
import net.xdclass.dto.AccountFileDTO;
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

import java.util.List;
import java.util.Objects;

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
                accountFileDO.setFileName(accountFileDO.getFileName()+"(1)");
            }else {
                //处理重复文件名,提取文件拓展名
                String[] split = accountFileDO.getFileName().split("\\.");
                accountFileDO.setFileName(split[0]+"(1)."+split[1]);
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