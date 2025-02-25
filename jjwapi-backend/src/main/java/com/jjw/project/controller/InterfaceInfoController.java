package com.jjw.project.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.jjw.jjwapicommon.model.entity.InterfaceInfo;
import com.jjw.jjwapicommon.model.entity.User;
import com.jjwapi.jjwapiclientsdk.client.JjwApiClient;
import com.jjw.project.annotation.AuthCheck;
import com.jjw.project.common.*;
import com.jjw.project.constant.CommonConstant;
import com.jjw.project.exception.BusinessException;
import com.jjw.project.model.dto.interfaceinfo.InterfaceInfoAddRequest;
import com.jjw.project.model.dto.interfaceinfo.InterfaceInfoInvokeRequest;
import com.jjw.project.model.dto.interfaceinfo.InterfaceInfoQueryRequest;
import com.jjw.project.model.dto.interfaceinfo.InterfaceInfoUpdateRequest;
import com.jjw.project.model.enums.InterfaceInfoStatusEnum;
import com.jjw.project.service.InterfaceInfoService;
import com.jjw.project.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 接口管理
 *
 * @author <a href="https://github.com/lijjw">程序员鱼皮</a>
 * @from <a href="https://jjw.icu">编程导航知识星球</a>
 */
@RestController
@RequestMapping("/interfaceinfo")
@Slf4j
public class InterfaceInfoController {

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private UserService userService;

    @Resource
    private JjwApiClient jjwApiClient;

    // region 增删改查

    /**
     * 创建
     *
     * @param interfaceInfoAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addInterfaceInfo(@RequestBody InterfaceInfoAddRequest interfaceInfoAddRequest, HttpServletRequest request) {
        if (interfaceInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoAddRequest, interfaceInfo);
        // 校验
        interfaceInfoService.validInterfaceInfo(interfaceInfo, true);
        User loginUser = userService.getLoginUser(request);
        interfaceInfo.setUserId(loginUser.getId());
        boolean result = interfaceInfoService.save(interfaceInfo);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        long newInterfaceInfoId = interfaceInfo.getId();
        return ResultUtils.success(newInterfaceInfoId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteInterfaceInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可删除
        if (!oldInterfaceInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = interfaceInfoService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param interfaceInfoUpdateRequest
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateInterfaceInfo(@RequestBody InterfaceInfoUpdateRequest interfaceInfoUpdateRequest,
                                                     HttpServletRequest request) {
        if (interfaceInfoUpdateRequest == null || interfaceInfoUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoUpdateRequest, interfaceInfo);
        // 参数校验
        interfaceInfoService.validInterfaceInfo(interfaceInfo, false);
        User user = userService.getLoginUser(request);
        long id = interfaceInfoUpdateRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可修改
        if (!oldInterfaceInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = interfaceInfoService.updateById(interfaceInfo);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<InterfaceInfo> getInterfaceInfoById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        return ResultUtils.success(interfaceInfo);
    }

    /**
     * 分页获取列表（仅管理员）
     *
     * @param interfaceInfoQueryRequest
     * @return
     */
    @AuthCheck(mustRole = "admin")
    @GetMapping("/list")
    public BaseResponse<List<InterfaceInfo>> listInterfaceInfo(InterfaceInfoQueryRequest interfaceInfoQueryRequest) {
        InterfaceInfo interfaceInfoQuery = new InterfaceInfo();
        if (interfaceInfoQueryRequest != null) {
            BeanUtils.copyProperties(interfaceInfoQueryRequest, interfaceInfoQuery);
        }
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>(interfaceInfoQuery);
        List<InterfaceInfo> interfaceInfoList = interfaceInfoService.list(queryWrapper);
        return ResultUtils.success(interfaceInfoList);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param interfaceInfoQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<InterfaceInfo>> listInterfaceInfoByPage(InterfaceInfoQueryRequest interfaceInfoQueryRequest, HttpServletRequest request) {
        if (interfaceInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfoQuery = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoQueryRequest, interfaceInfoQuery);
        long current = interfaceInfoQueryRequest.getCurrent();
        long size = interfaceInfoQueryRequest.getPageSize();
        String sortField = interfaceInfoQueryRequest.getSortField();
        String sortOrder = interfaceInfoQueryRequest.getSortOrder();
        String description = interfaceInfoQuery.getDescription();
        // description 需支持模糊搜索
        interfaceInfoQuery.setDescription(null);
        // 限制爬虫
        if (size > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>(interfaceInfoQuery);
        queryWrapper.like(StringUtils.isNotBlank(description), "description", description);
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        Page<InterfaceInfo> interfaceInfoPage = interfaceInfoService.page(new Page<>(current, size), queryWrapper);
        return ResultUtils.success(interfaceInfoPage);
    }

//    /**
//     * 分页获取当前用户创建的资源列表
//     *
//     * @param interfaceinfoQueryRequest
//     * @param request
//     * @return
//     */
//    @PostMapping("/my/list/page/vo")
//    public BaseResponse<Page<InterfaceInfoVO>> listMyInterfaceInfoVOByPage(@RequestBody InterfaceInfoQueryRequest interfaceinfoQueryRequest,
//            HttpServletRequest request) {
//        if (interfaceinfoQueryRequest == null) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        User loginUser = userService.getLoginUser(request);
//        interfaceinfoQueryRequest.setUserId(loginUser.getId());
//        long current = interfaceinfoQueryRequest.getCurrent();
//        long size = interfaceinfoQueryRequest.getPageSize();
//        // 限制爬虫
//        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
//        Page<InterfaceInfo> interfaceinfoPage = interfaceInfoService.page(new Page<>(current, size),
//                interfaceInfoService.getQueryWrapper(interfaceinfoQueryRequest));
//        return ResultUtils.success(interfaceInfoService.getInterfaceInfoVOPage(interfaceinfoPage, request));
//    }
//
    // endregion
    /**
     * 发布（仅管理员）
     *
     * @param idRequest
     * @return
     * 校验该接口是否存在
     * 判断该接口是否可以调用
     * 修改接口数据库中的状态为1
     */
    @PostMapping("/online")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> onlineInterfaceInfo(@RequestBody IdRequest idRequest,
                                                     HttpServletRequest request) {
        //如果id为null或者id小于0
        if(idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //校验该接口是否存在
        Long id = idRequest.getId();
        //根据Id查询接口信息数据
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        //判断该接口是否可以调用
        //模拟
        com.jjwapi.jjwapiclientsdk.model.User user = new com.jjwapi.jjwapiclientsdk.model.User();
        user.setUserName("zhangyu");
        String username = jjwApiClient.getUserNameByPost(user);
        if (StringUtils.isBlank(username)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"接口验证失败");
        }
        InterfaceInfo interfaceInfo1 = new InterfaceInfo();
        interfaceInfo1.setId(id);
        interfaceInfo1.setStatus(InterfaceInfoStatusEnum.ONLINE.getValue());
        boolean result = interfaceInfoService.updateById(interfaceInfo1);
        return ResultUtils.success(result);
    }
    /**
     * 下线（仅管理员）
     *
     * @param idRequest
     * @return
     */
    @PostMapping("/offline")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> offlineInterfaceInfo(@RequestBody IdRequest idRequest,
                                                     HttpServletRequest request) {
            //如果id为null或者id小于0
            if(idRequest == null || idRequest.getId() <= 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            //校验该接口是否存在
            Long id = idRequest.getId();
            //根据Id查询接口信息数据
            InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
            if (interfaceInfo == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
            }
            //判断该接口是否可以调用
            //模拟
            com.jjwapi.jjwapiclientsdk.model.User user = new com.jjwapi.jjwapiclientsdk.model.User();
            user.setUserName("章鱼小丸子儿~");
            String username = jjwApiClient.getUserNameByPost(user);
            if (StringUtils.isBlank(username)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            InterfaceInfo interfaceInfo1 = new InterfaceInfo();
            interfaceInfo1.setId(id);
            interfaceInfo1.setStatus(InterfaceInfoStatusEnum.OFFLINE.getValue());
            boolean result = interfaceInfoService.updateById(interfaceInfo1);
            return ResultUtils.success(result);
    }

//    /**
//     * 测试调用
//     *
//     * @param interfaceInfoInvokeRequest
//     * @param request
//     * @return
//     */
//    @PostMapping("/invoke")
//    @AuthCheck(mustRole = "admin")
//    public BaseResponse<Object> invokeInterfaceInfo1(@RequestBody InterfaceInfoInvokeRequest interfaceInfoInvokeRequest,
//                                                    HttpServletRequest request) {
//        if (interfaceInfoInvokeRequest == null || interfaceInfoInvokeRequest.getId() <= 0) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        long id = interfaceInfoInvokeRequest.getId();
//        String userRequestParams = interfaceInfoInvokeRequest.getUserRequestParams();
//        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
//        if (oldInterfaceInfo == null) {
//            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
//        }
//        if (oldInterfaceInfo.getStatus() == InterfaceInfoStatusEnum.OFFLINE.getValue()) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口已关闭");
//        }
//        // 获取当前登录用户的ak和sk，这样相当于用户自己的这个身份去调用，
//        // 也不会担心它刷接口，因为知道是谁刷了这个接口，会比较安全
//        User loginUser = userService.getLoginUser(request);
//        String accessKey = loginUser.getAccessKey();
//        String secretKey = loginUser.getSecretKey();
//        JjwApiClient jjwApiClient = new JjwApiClient(accessKey, secretKey);
//        // 我们只需要进行测试调用，所以我们需要解析传递过来的参数。
//        Gson gson = new Gson();
//        com.jjwapi.jjwapiclientsdk.model.User user = gson.fromJson(userRequestParams, com.jjwapi.jjwapiclientsdk.model.User.class);
//        // 调用JjwApiClient的getUsernameByPost方法，传入用户对象，获取用户名
//        String usernameByPost = jjwApiClient.getUserNameByPost(user);
//        // 返回成功响应，并包含调用结果
//        return ResultUtils.success(usernameByPost);
//    }

    /**
     * 测试调用
     *
     * @param interfaceInfoInvokeRequest 接口调用请求参数
     * @param request HTTP 请求对象
     * @return 接口调用结果
     */
    @PostMapping("/invoke")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Object> invokeInterfaceInfo(@RequestBody InterfaceInfoInvokeRequest interfaceInfoInvokeRequest,
                                                    HttpServletRequest request) {
        // 更严格的参数校验
        if (interfaceInfoInvokeRequest == null || interfaceInfoInvokeRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口ID或请求参数不能为空");
        }

        long id = interfaceInfoInvokeRequest.getId();
        String userRequestParams = interfaceInfoInvokeRequest.getUserRequestParams();

        // 根据id获取接口信息
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到对应的接口信息");
        }

        if (oldInterfaceInfo.getStatus() == InterfaceInfoStatusEnum.OFFLINE.getValue()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口已关闭");
        }

        // 获取当前登录用户的ak和sk
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null || loginUser.getAccessKey() == null || loginUser.getSecretKey() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户信息或密钥信息不完整");
        }
        String accessKey = loginUser.getAccessKey();
        String secretKey = loginUser.getSecretKey();
        System.out.println(accessKey+" "+secretKey);
        JjwApiClient jjwApiClient = new JjwApiClient(accessKey, secretKey);

        // 解析传递过来的参数，增加异常处理
        Gson gson = new Gson();

        try {
            // 获取接口信息中的方法名
            String methodName = oldInterfaceInfo.getName();
            System.out.println("调用的方法名: " + methodName);

            // 判断方法是否需要参数
            Method method = getMethod(jjwApiClient, methodName);
            if (method == null) {
                throw new NoSuchMethodException("未找到匹配的方法");
            }

            // 调用方法
            Object result;
            System.out.println(method.getParameterCount());
            if (method.getParameterCount() == 0) {
                // 如果方法没有参数，直接调用
                result = invokeMethodWithoutParams(method, jjwApiClient);
            } else {
                // 如果方法有参数，解析参数并调用
                Class<?> paramClass = method.getParameterTypes()[0]; // 获取方法的第一个参数类型
                System.out.println("方法的参数类型: " + paramClass.getName());

                // 检查 userRequestParams 是否为空
                if (userRequestParams.trim().isEmpty()) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数不能为空");
                }

                // 打印待解析的 JSON 字符串
                System.out.println("待解析的 JSON 字符串: " + userRequestParams);

                // 修正 JSON 字符串格式（确保使用英文双引号）
                userRequestParams = userRequestParams.replace("“", "\"").replace("”", "\"");
                System.out.println("修正后的 JSON 字符串: " + userRequestParams);

                // 解析 JSON 字符串
                Object paramObject = gson.fromJson(userRequestParams, paramClass);

                // 检查解析后的参数对象
                if (paramObject == null) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "解析后的参数对象为 null");
                }
                System.out.println("解析后的参数对象: " + paramObject);

                // 调用方法
                result = invokeMethodWithParams(method, jjwApiClient, paramObject);
            }

            return ResultUtils.success(result);
        } catch (JsonSyntaxException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数JSON格式错误");
        } catch (NoSuchMethodException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "未找到对应的方法");
        } catch (Exception e) {
            // 处理反射调用过程中的异常
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口调用失败，" + e.getMessage());
        }
    }

    // 根据方法名获取Method对象
    private Method getMethod(JjwApiClient jjwApiClient, String methodName) {
        Method[] methods = jjwApiClient.getClass().getMethods();
        for (Method m : methods) {
            if (m.getName().equals(methodName)) {
                return m;
            }
        }
        return null;
    }

    // 调用无参方法
    private Object invokeMethodWithoutParams(Method method, JjwApiClient jjwApiClient) throws Exception {
        if (method == null || jjwApiClient == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "方法或客户端实例为空");
        }
        method.setAccessible(true);
        return method.invoke(jjwApiClient);
    }

    // 调用有参方法
    private Object invokeMethodWithParams(Method method, JjwApiClient jjwApiClient, Object paramObject) throws Exception {
        if (method == null || jjwApiClient == null || paramObject == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "方法、客户端实例或参数对象为空");
        }
        System.out.println("实际传递的参数对象: " + paramObject);
        return method.invoke(jjwApiClient, paramObject);
    }

//    /**
//     * 测试调用
//     *
//     * @param interfaceInfoInvokeRequest
//     * @param request
//     * @return
//     */
//    @PostMapping("/invoke")
//    @AuthCheck(mustRole = "admin")
//    public BaseResponse<Object> invokeInterfaceInfo(@RequestBody InterfaceInfoInvokeRequest interfaceInfoInvokeRequest,
//                                                    HttpServletRequest request) {
//        if (interfaceInfoInvokeRequest == null || interfaceInfoInvokeRequest.getId() <= 0) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        long id = interfaceInfoInvokeRequest.getId();
//        String userRequestParams = interfaceInfoInvokeRequest.getUserRequestParams();
//
//        // 根据id获取接口信息
//        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
//        if (oldInterfaceInfo == null) {
//            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
//        }
//
//        if (oldInterfaceInfo.getStatus() == InterfaceInfoStatusEnum.OFFLINE.getValue()) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口已关闭");
//        }
//
//        // 获取当前登录用户的ak和sk
//        User loginUser = userService.getLoginUser(request);
//        String accessKey = loginUser.getAccessKey();
//        String secretKey = loginUser.getSecretKey();
//
//        JjwApiClient jjwApiClient = new JjwApiClient(accessKey, secretKey);
//
//        // 解析传递过来的参数
//        Gson gson = new Gson();
//        com.jjwapi.jjwapiclientsdk.model.User user = gson.fromJson(userRequestParams, com.jjwapi.jjwapiclientsdk.model.User.class);
//        System.out.println(user.getUsername());
//        try {
//            // 获取接口信息中的方法名
//            String methodName = oldInterfaceInfo.getName();
//            // 通过反射调用JjwApiClient的相应方法
//            Method method = jjwApiClient.getClass().getMethod(methodName, com.jjwapi.jjwapiclientsdk.model.User.class);
//            Object result = method.invoke(jjwApiClient, user);
//            return ResultUtils.success(result);
//        } catch (Exception e) {
//            // 处理反射调用过程中的异常
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口调用失败，方法反射调用出现异常");
//        }
//    }
//
//    /**
//     * 分页搜索（从 ES 查询，封装类）
//     *
//     * @param interfaceinfoQueryRequest
//     * @param request
//     * @return
//     */
//    @PostMapping("/search/page/vo")
//    public BaseResponse<Page<InterfaceInfoVO>> searchInterfaceInfoVOByPage(@RequestBody InterfaceInfoQueryRequest interfaceinfoQueryRequest,
//            HttpServletRequest request) {
//        long size = interfaceinfoQueryRequest.getPageSize();
//        // 限制爬虫
//        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
//        Page<InterfaceInfo> interfaceinfoPage = interfaceInfoService.searchFromEs(interfaceinfoQueryRequest);
//        return ResultUtils.success(interfaceInfoService.getInterfaceInfoVOPage(interfaceinfoPage, request));
//    }


}
