package com.jjw.project.service.impl;



import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jjw.project.common.ErrorCode;
import com.jjw.project.exception.BusinessException;
import com.jjw.project.mapper.UserInterfaceInfoMapper;
import com.jjw.jjwapicommon.model.entity.UserInterfaceInfo;
import com.jjw.project.service.UserInterfaceInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author jjw
 * @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Service实现
 * @createDate 2025-02-09 09:39:03
 */
@Service
public class UserInterfaceInfoServiceImpl extends ServiceImpl<UserInterfaceInfoMapper, UserInterfaceInfo>
        implements UserInterfaceInfoService {

    private static final Logger logger = LoggerFactory.getLogger(UserInterfaceInfoServiceImpl.class);
    // 初始调用次数常量
    private static final int INITIAL_LEFT_NUM = 100;

    private final UserInterfaceInfoMapper userInterfaceInfoMapper;

    public UserInterfaceInfoServiceImpl(UserInterfaceInfoMapper userInterfaceInfoMapper) {
        this.userInterfaceInfoMapper = userInterfaceInfoMapper;
    }


    @Override
    public void validInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add) {
        if (userInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (add) {
            if (userInterfaceInfo.getInterfaceInfoId() <= 0 || userInterfaceInfo.getUserId() <= 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口或用户不存在");
            }
        }
        if (userInterfaceInfo.getLeftNum() < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"剩余次数不能小于0");
        }
    }

    /**
     * 调用次数管理
     * @param interfaceInfoId
     * @param userId
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean invokeCount(long interfaceInfoId, long userId) {
        // 判断(其实这里还应该校验存不存在，这里就不用校验了，因为它不存在，也更新不到那条记录)
        if (interfaceInfoId <= 0 || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 先查询记录并加悲观锁（行级锁）
        UserInterfaceInfo userInterfaceInfo = userInterfaceInfoMapper.selectOne(new UpdateWrapper<UserInterfaceInfo>()
                .eq("interfaceInfoId", interfaceInfoId)
                .eq("userId", userId)
                .last("FOR UPDATE"));

        if (userInterfaceInfo == null) {
            // 如果记录不存在，插入一条新记录
            userInterfaceInfo = createNewUserInterfaceInfo(interfaceInfoId, userId);
            try {
                userInterfaceInfoMapper.insert(userInterfaceInfo);
                logger.info("成功插入新的用户接口信息记录，interfaceInfoId: {}, userId: {}", interfaceInfoId, userId);
            } catch (Exception e) {
                logger.error("插入用户接口信息记录失败，interfaceInfoId: {}, userId: {}", interfaceInfoId, userId, e);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "插入用户接口信息记录失败");
            }
        }

        if (userInterfaceInfo.getLeftNum() <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "调用次数已用完");
        }

        // 更新 leftNum 和 totalNum
        userInterfaceInfo.setLeftNum(userInterfaceInfo.getLeftNum() - 1);
        userInterfaceInfo.setTotalNum(userInterfaceInfo.getTotalNum() + 1);

        // 执行更新操作
        try {
            boolean result = userInterfaceInfoMapper.updateById(userInterfaceInfo) > 0;
            if (result) {
                logger.info("成功更新用户接口信息记录，interfaceInfoId: {}, userId: {}", interfaceInfoId, userId);
            } else {
                logger.warn("更新用户接口信息记录失败，interfaceInfoId: {}, userId: {}", interfaceInfoId, userId);
            }
            return result;
        } catch (Exception e) {
            logger.error("更新用户接口信息记录失败，interfaceInfoId: {}, userId: {}", interfaceInfoId, userId, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新用户接口信息记录失败");
        }
    }

    private UserInterfaceInfo createNewUserInterfaceInfo(long interfaceInfoId, long userId) {
        UserInterfaceInfo userInterfaceInfo = new UserInterfaceInfo();
        userInterfaceInfo.setInterfaceInfoId(interfaceInfoId);
        userInterfaceInfo.setUserId(userId);
        userInterfaceInfo.setLeftNum(INITIAL_LEFT_NUM);
        userInterfaceInfo.setTotalNum(0);
        return userInterfaceInfo;
    }


}




