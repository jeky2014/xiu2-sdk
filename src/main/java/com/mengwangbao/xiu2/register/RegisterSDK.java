package com.mengwangbao.xiu2.register;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mengwangbao.xiu2.util.SignatureParamBuilder;
import com.mengwangbao.xiu2.util.Ed25519Utils;
import java.nio.charset.StandardCharsets;

/**
 * 设备注册SDK类
 * 提供设备注册相关功能的客户端实现
 */
public class RegisterSDK {
    
    /**
     * ObjectMapper实例，用于JSON序列化
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 根据DeviceRegisterRequest获取参数拼接字符串
     * @param request 设备注册请求对象
     * @return 参数拼接后的字符串
     * @throws JsonProcessingException 当JSON处理失败时
     */
    public String buildSignString(DeviceRegisterRequest request) throws JsonProcessingException {
        return SignatureParamBuilder.buildSignString(request.toMap());
    }

    /**
     * 根据拼接串和私钥获取签名
     * @param message 拼接后的字符串
     * @param privateKeyPem 私钥PEM格式的字符串
     * @return 签名结果
     * @throws Exception 当签名失败时
     */
    public String generateSignature(String message, String privateKeyPem) throws Exception {
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        return Ed25519Utils.signMessage(messageBytes, privateKeyPem);
    }

    /**
     * 验证签名是否有效
     * @param message 原始的拼接字符串
     * @param signature 需要验证的签名
     * @param publicKeyPem 公钥PEM格式的字符串
     * @return 签名验证结果，true表示签名有效，false表示无效
     * @throws Exception 当验证过程中发生错误时
     */
    public boolean verifySignature(String message, String signature, String publicKeyPem) throws Exception {
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        return Ed25519Utils.verifySignature(messageBytes, signature, publicKeyPem);
    }
    
    /**
     * 将DeviceRegisterRequest对象序列化为JSON字符串
     * @param request 设备注册请求对象
     * @return 序列化后的JSON字符串
     * @throws JsonProcessingException 当JSON序列化失败时
     */
    public String serializeRequestToJson(DeviceRegisterRequest request) throws JsonProcessingException {
        return objectMapper.writeValueAsString(request);
    }

}
