package com.jjw.jjwapicommon.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.jjw.jjwapicommon.model.entity.User;


/**
 * 用户服务
 * @author jjw
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2025-01-17 09:59:13
 */
public interface InnerUserService extends IService<User> {

    /**
     * 数据库中查是否已分配给用户秘钥（accessKey）
     * @param accessKey
     * @return
     */
    User getInvokeUser(String accessKey);


}
