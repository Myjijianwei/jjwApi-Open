package com.jjw.jjwapicommon.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.jjw.jjwapicommon.model.entity.InterfaceInfo;
import com.jjw.jjwapicommon.model.entity.User;
import com.jjw.jjwapicommon.model.entity.UserInterfaceInfo;

/**
* @author jjw
* @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Service
* @createDate 2025-02-09 09:39:03
*/
public interface InnerUserInterfaceInfoService extends IService<UserInterfaceInfo> {

//    /**
//     *  数据库中查是否已分配给用户密钥（accessKey、secretKey）
//     * @param accessKey
//     * @param secretKey
//     * @return
//     */
//    User getInvokeUser(String accessKey,String secretKey);
//
//    /**
//     * 从数据库中查询模拟接口是否存在（请求路径、请求方法、请求参数）
//     * @param path
//     * @param method
//     * @return
//     */
//    InterfaceInfo getInterFaceInfo(String path,String method);

    /**
     * 调用接口统计
     * @param interfaceInfoId
     * @param userId
     * @return
     */
    boolean invokeCount(long interfaceInfoId, long userId);

}
