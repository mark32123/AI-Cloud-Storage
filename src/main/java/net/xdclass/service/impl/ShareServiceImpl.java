package net.xdclass.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.config.AccountConfig;
import net.xdclass.controller.req.ShareCreateReq;
import net.xdclass.dto.AccountDTO;
import net.xdclass.dto.ShareDTO;
import net.xdclass.enums.ShareDayTypeEnum;
import net.xdclass.enums.ShareStatusEnum;
import net.xdclass.enums.ShareTypeEnum;
import net.xdclass.interceptor.LoginInterceptor;
import net.xdclass.mapper.ShareFileMapper;
import net.xdclass.mapper.ShareMapper;
import net.xdclass.model.ShareDO;
import net.xdclass.model.ShareFileDO;
import net.xdclass.service.AccountFileService;
import net.xdclass.service.ShareService;
import net.xdclass.util.SpringBeanUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
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
public class ShareServiceImpl implements ShareService {

    @Autowired
    private ShareMapper shareMapper;

    @Autowired
    private ShareFileMapper shareFileMapper;

    @Autowired
    private AccountFileService fileService;

    @Override
    public List<ShareDTO> listShare() {
        AccountDTO accountDTO = LoginInterceptor.threadLocal.get();

        List<ShareDO> shareDOList = shareMapper.selectList(new QueryWrapper<ShareDO>()
                .eq("account_id", accountDTO.getId()).orderByDesc("gmt_create"));

        return SpringBeanUtil.copyProperties(shareDOList, ShareDTO.class);
    }

    /**
     * * 检查分享文件的权限
     * * 生成分享链接和持久化数据库
     * * 生成分享详情和持久化数据库
     * @param req
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShareDTO createShare(ShareCreateReq req) {

        //1、检查分享文件的权限
        List<Long> fileIds = req.getFileIds();
        fileService.checkFileIdLegal(fileIds, req.getAccountId());

        //2、生成分享链接和持久化数据库
        Integer dayType = req.getShareDayType();
        Integer shareDays = ShareDayTypeEnum.getDaysByType(dayType);
        Long shareId = IdUtil.getSnowflakeNextId();
        //生成分享链接
        String shareUrl = AccountConfig.PAN_FRONT_DOMAIN_SHARE_API + shareId;
        log.info("shareUrl:{}",shareUrl);

        ShareDO shareDO = ShareDO.builder()
                .id(shareId)
                .shareName(req.getShareName())
                .shareType(ShareTypeEnum.valueOf(req.getShareType()).name())
                .shareDayType(dayType)
                .shareDay(shareDays)
                .shareUrl(shareUrl)
                .shareStatus(ShareStatusEnum.USED.name())
                .accountId(req.getAccountId()).build();

        if(ShareDayTypeEnum.PERMANENT.getDayType().equals(dayType)){
            shareDO.setShareEndTime(Date.from(LocalDate.of(9999,12,31)
                    .atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }else {
            shareDO.setShareEndTime(new Date(System.currentTimeMillis() + shareDays * 24 * 3600 * 1000L));
        }
        if(ShareTypeEnum.NEED_CODE.name().equalsIgnoreCase(req.getShareType())){
            //生成提取码 6位
            String shareCode = RandomStringUtils.randomAlphabetic(6).toUpperCase();
            shareDO.setShareCode(shareCode);
        }
        shareMapper.insert(shareDO);

        //3、生成分享详情和持久化数据库
        List<ShareFileDO> shareFileDOS = new ArrayList<>();
        fileIds.forEach(fileId -> {
            ShareFileDO shareFileDO = ShareFileDO.builder()
                    .shareId(shareId)
                    .accountFileId(fileId)
                    .accountId(req.getAccountId())
                    .build();
            shareFileDOS.add(shareFileDO);
        });
        shareFileMapper.insertBatch(shareFileDOS);
        return SpringBeanUtil.copyProperties(shareDO, ShareDTO.class);
    }
}