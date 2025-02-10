package com.yupi.project.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.project.model.entity.InterfaceInfo;
import com.yupi.project.model.entity.UserInterfaceInfo;

/**
* @author jjw
* @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Service
* @createDate 2025-02-09 09:39:03
*/
public interface UserInterfaceInfoService extends IService<UserInterfaceInfo> {
    void validInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add) ;

    /**
     * 调用接口统计
     * @param interfaceInfoId
     * @param userId
     * @return
     */
    boolean invokeCount(long interfaceInfoId, long userId);

}
