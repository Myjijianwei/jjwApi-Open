package com.jjw.jjwapicommon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jjw.jjwapicommon.model.entity.InterfaceInfo;


/**
 * @author jjw
 * @description 针对表【interface_info(接口信息)】的数据库操作Service
 * @createDate 2025-01-14 17:32:26
 */
public interface InnerInterfaceInfoService extends IService<InterfaceInfo> {

    /**
     * 从数据库中查询模拟接口是否存在
     * @param path
     * @param method
     * @return
     */
    InterfaceInfo getInterfaceInfo(String path, String method);
}
