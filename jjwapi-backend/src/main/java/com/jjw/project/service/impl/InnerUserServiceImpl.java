package com.jjw.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jjw.jjwapicommon.model.entity.User;
import com.jjw.jjwapicommon.service.InnerUserService;
import com.jjw.project.common.ErrorCode;
import com.jjw.project.exception.BusinessException;
import com.jjw.project.mapper.UserMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

@DubboService
public class InnerUserServiceImpl extends ServiceImpl<UserMapper, User> implements InnerUserService {

    @Resource
    private UserMapper userMapper;

    /**
     * @param accessKey
     * @return
     */
    @Override
    public User getInvokeUser(String accessKey) {
        Logger logger = LoggerFactory.getLogger(InnerUserServiceImpl.class);
        logger.info("getInvokeUser 方法被调用，传入的 accessKey: {}", accessKey);
        // 参数校验
        if (StringUtils.isAnyBlank(accessKey)) {
            System.out.println(accessKey);
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 创建查询条件包装器
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("accessKey", accessKey);

        // 使用 UserMapper 的 selectOne 方法查询用户信息
        return userMapper.selectOne(queryWrapper);
    }
}