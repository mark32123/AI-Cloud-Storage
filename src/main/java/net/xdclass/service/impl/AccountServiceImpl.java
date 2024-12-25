package net.xdclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.config.AccountConfig;
import net.xdclass.controller.req.AccountRegisterReq;
import net.xdclass.enums.AccountRoleEnum;
import net.xdclass.enums.BizCodeEnum;
import net.xdclass.exception.BizException;
import net.xdclass.mapper.AccountMapper;
import net.xdclass.model.AccountDO;
import net.xdclass.service.AccountService;
import net.xdclass.util.SpringBeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

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
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountMapper accountMapper;

    /**
     * 1、查询手机号是否重复
     * 2、加密密码
     * 3、插入数据库
     * 4、其他相关初始化操作
     * @param req
     */
    @Override
    public void register(AccountRegisterReq req) {

        //1、查询手机号是否重复
        List<AccountDO> accountDOList = accountMapper.selectList(new QueryWrapper<AccountDO>().eq("phone", req.getPhone()));
        if(!accountDOList.isEmpty()){
            throw new BizException(BizCodeEnum.ACCOUNT_REPEAT);
        }

        AccountDO accountDO = SpringBeanUtil.copyProperties(req, AccountDO.class);

        //加密密码
        String digestAsHex = DigestUtils.md5DigestAsHex((AccountConfig.ACCOUNT_SALT + req.getPassword()).getBytes());
        accountDO.setPassword(digestAsHex);
        accountDO.setRole(AccountRoleEnum.COMMON.name());
        accountMapper.insert(accountDO);

        //其他操作 TODO


    }
}