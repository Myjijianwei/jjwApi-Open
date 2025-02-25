package com.jjw.jjwapigateway;

import com.jjw.jjwapicommon.model.entity.InterfaceInfo;
import com.jjw.jjwapicommon.model.entity.User;
import com.jjw.jjwapicommon.service.InnerInterfaceInfoService;
import com.jjw.jjwapicommon.service.InnerUserInterfaceInfoService;
import com.jjw.jjwapicommon.service.InnerUserService;
import com.jjwapi.jjwapiclientsdk.utils.SignUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 全局过滤
 */
@Component
public class CustomGlobalFilter implements GlobalFilter, Ordered {

    @DubboReference
    private InnerUserService innerUserService;

    @DubboReference
    private InnerInterfaceInfoService innerInterfaceInfoService;

    @DubboReference
    private InnerUserInterfaceInfoService innerUserInterfaceInfoService;

    private static final Logger log = LoggerFactory.getLogger(CustomGlobalFilter.class);
    private static final List<String> IP_WHITE_LIST = Arrays.asList("127.0.0.1");
    private static final String INTERFACE_HOST = "http://localhost:8123";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. 用户发送请求到API网关
        // 2. 请求日志
        ServerHttpRequest request = exchange.getRequest();
        log.info("原始请求路径：" + request.getPath().value());
        String path = INTERFACE_HOST + request.getPath().value();
        log.info("拼接后的请求路径：" + path);
        String method = request.getMethod().toString();
        log.info("******请求唯一标识：" + request.getId());
        log.info("******请求路径：" + path);
        log.info("******请求方法：" + method);
        log.info("******请求参数：" + request.getQueryParams());
        String sourceAddress = request.getLocalAddress() != null ? request.getLocalAddress().getHostString() : null;
        log.info("******请求来源ip地址：" + sourceAddress);
        log.info("******请求来源地址：" + request.getRemoteAddress());

        // 3. (黑白名单)
        ServerHttpResponse response = exchange.getResponse();
        if (sourceAddress == null || !IP_WHITE_LIST.contains(sourceAddress)) {
            // 设置响应状态码403，禁止访问
            response.setStatusCode(HttpStatus.FORBIDDEN);
            // 返回处理完成的响应
            return response.setComplete();
        }

        // 4. 用户鉴权(判断 ak、sk 是否合法)
        HttpHeaders headers = request.getHeaders();
        String accessKey = headers.getFirst("accessKey");
        log.info("获取到的 accessKey: " + accessKey);
        if (accessKey == null) {
            log.error("accessKey 为空，拒绝请求");
            return handleNoAuth(response);
        }
        String nonce = headers.getFirst("nonce");
        String timestamp = headers.getFirst("timestamp");
        String signature = headers.getFirst("signature");
        // 获取请求体内容
        String body = headers.getFirst("body");
        User invokeUser = null;
        try {
            invokeUser = innerUserService.getInvokeUser(accessKey);
        } catch (Exception e) {
            log.error("getInvokeUser error", e);
            return handleNoAuth(response);
        }
        if (invokeUser == null) {
            // 如果用户信息为空，处理未授权情况并且返回响应
            log.error("未找到对应的用户信息，拒绝请求");
            return handleNoAuth(response);
        }

        if (nonce == null || Long.parseLong(nonce) > 10000L) {
            log.error("nonce 无效，拒绝请求");
            return handleNoAuth(response);
        }
        // 时间和当前时间不能超过 5 分钟
        // 首先,获取当前时间的时间戳,以秒为单位
        // System.currentTimeMillis()返回当前时间的毫秒数，除以1000后得到当前时间的秒数。
        Long currentTime = System.currentTimeMillis() / 1000;
        // 定义一个常量FIVE_MINUTES,表示五分钟的时间间隔(乘以60,将分钟转换为秒,得到五分钟的时间间隔)。
        final Long FIVE_MINUTES = 60 * 5L;
        // 判断当前时间与传入的时间戳是否相差五分钟或以上
        // Long.parseLong(timestamp)将传入的时间戳转换成长整型
        // 然后计算当前时间与传入时间戳之间的差值(以秒为单位),如果差值大于等于五分钟,则返回true,否则返回false
        if (timestamp == null || (currentTime - Long.parseLong(timestamp)) >= FIVE_MINUTES) {
            // 如果时间戳与当前时间相差五分钟或以上，调用handleNoAuth(response)方法进行处理
            log.error("时间戳无效，拒绝请求");
            return handleNoAuth(response);
        }


        String secretKey = invokeUser.getSecretKey();
        String serverSign = SignUtils.genSign(body, secretKey);
        if (serverSign == null || !serverSign.equals(signature)) {
            // 如果签名为空或者签名不一致，返回处理未授权的响应
            log.error("签名验证失败，拒绝请求");
            return handleNoAuth(response);
        }

        // 5. 请求的模拟接口是否存在?
        // 初始化一个InterfaceInfo对象，用于存储查询结果
        InterfaceInfo interfaceInfo = null;
        try {
            // 尝试从内部接口信息服务获取指定路径和方法接口信息
            interfaceInfo = innerInterfaceInfoService.getInterfaceInfo(path, method);
        } catch (Exception e) {
            log.error("getInterfaceInfo error", e);
            return handleNoAuth(response);
        }
        if (interfaceInfo == null) {
            // 如果未获取到接口信息，返回处理未授权的响应
            log.error("未找到对应的接口信息，拒绝请求");
            return handleNoAuth(response);
        }

        // 7. 响应日志
        return handleResponse(exchange, chain, interfaceInfo.getId(), invokeUser.getId());
    }

    @Override
    public int getOrder() {
        return -1;
    }

    public Mono<Void> handleNoAuth(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        return response.setComplete();
    }

    /**
     * 处理响应
     *
     * @param exchange
     * @param chain
     * @return
     */
    public Mono<Void> handleResponse(ServerWebExchange exchange, GatewayFilterChain chain, long interfaceId, long userId) {
        try {
            //获取原始的响应对象
            ServerHttpResponse originalResponse = exchange.getResponse();
            //获取数据缓冲工厂
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();
            //获取响应的状态码
            HttpStatus statusCode = originalResponse.getStatusCode();

            //判断响应的状态码是否为200
            if (statusCode == HttpStatus.OK) {
                //创建一个装饰后的响应对象（开始穿装备，增强能力）
                ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {

                    //重写writeWith方法，用于处理响应体的数据
                    @Override
                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                        log.info("body instanceof Flux: {}", (body instanceof Flux));
                        //判断响应体是否是Flux类型
                        if (body instanceof Flux) {
                            Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                            //返回一个处理后的响应体
                            return super.writeWith(fluxBody.map(dataBuffer -> {
                                //读取响应体的内容并且转换成为字节数组
                                //调用成功，接口调用次数+1 invokeCount
                                try {
                                    //调用内部用户接口信息服务，记录接口调用次数
                                    innerUserInterfaceInfoService.invokeCount(interfaceId, userId);
                                } catch (Exception e) {
                                    log.error("invokeCount error" + e.getMessage());
                                }
                                byte[] content = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(content);
                                DataBufferUtils.release(dataBuffer);//释放掉内存
                                // 构建日志
                                StringBuilder sb2 = new StringBuilder(200);
                                sb2.append("<--- {} {} \n");
                                List<Object> rspArgs = new ArrayList<>();
                                rspArgs.add(originalResponse.getStatusCode());
                                //rspArgs.add(requestUrl);
                                String data = new String(content, StandardCharsets.UTF_8);//data
                                sb2.append(data);
                                log.info("响应结果：" + data);
                                return bufferFactory.wrap(content);
                            }));
                        } else {
                            log.error("<--- {} 响应code异常", getStatusCode());
                        }
                        return super.writeWith(body);
                    }
                };
                return chain.filter(exchange.mutate().response(decoratedResponse).build());
            }
            return chain.filter(exchange);//降级处理返回数据
        } catch (Exception e) {
            log.error("gateway log exception.\n" + e);
            return chain.filter(exchange);
        }

    }
}
