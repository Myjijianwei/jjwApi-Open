package com.jjw.project.service.impl;



import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jjw.project.common.ErrorCode;
import com.jjw.project.exception.BusinessException;
import com.jjw.project.mapper.UserInterfaceInfoMapper;
import com.jjw.project.model.entity.UserInterfaceInfo;
import com.jjw.project.service.UserInterfaceInfoService;
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
     * TODO 用户可能会瞬间调用大量接口，所以后续需要优化，加上事务和锁，
     * TODO 如果是分布式环境，那么就要用分布式锁实现
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
        UserInterfaceInfo userInterfaceInfo = this.getOne(new UpdateWrapper<UserInterfaceInfo>()
                .eq("interfaceInfoId", interfaceInfoId)
                .eq("userId", userId)
                .last("FOR UPDATE"));
        if (userInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户接口信息记录不存在");
        }
        if (userInterfaceInfo.getLeftNum() <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "调用次数已用完");
        }
        // 更新 leftNum 和 totalNum
        userInterfaceInfo.setLeftNum(userInterfaceInfo.getLeftNum() - 1);
        userInterfaceInfo.setTotalNum(userInterfaceInfo.getTotalNum() + 1);
        // 执行更新操作
        return this.updateById(userInterfaceInfo);
    }

}




