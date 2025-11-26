package com.mengwangbao.xiu2.register;

import com.mengwangbao.xiu2.util.Ed25519Utils;

/**
 * RegisterSDK使用示例类
 * 展示如何使用设备注册SDK生成签名所需的请求字符串、签名及验证签名
 * <p>
 * 使用步骤：
 * 1. 创建RegisterSDK实例
 * 2. 构建DeviceRegisterRequest对象
 * 3. 使用buildSignString生成参数拼接字符串
 * 4. 使用generateSignature生成签名（需要Ed25519密钥对）
 * 5. 使用verifySignature验证签名有效性
 */
public class RegisterSDKExample {

    public static void main(String[] args) {
        demonstrateRegistration();
    }

    /**
     * 演示如何使用RegisterSDK生成参数拼接字符串、签名及验证签名
     */
    public static void demonstrateRegistration() {
        try {
            System.out.println("=== RegisterSDK 使用示例 ===");

            // 1. 初始化SDK
            RegisterSDK registerSDK = new RegisterSDK();
            System.out.println("1. 初始化SDK完成");

            // 2. 构建注册请求对象
            DeviceRegisterRequest request = new DeviceRegisterRequest();
            request.setSchemaVersion("1.0");  // 当前固定为1.0
            request.setMac("00:11:22:33:44:55");
            request.setImei("123456789012345");
            request.setIccid("89860123456789012345");
            request.setHardwareVersion("1.2.3");
            request.setSoftwareVersion("2.1.0");
            request.setLongitude("116.4074");
            request.setLatitude("39.9042");
            request.setDeviceSn("xxxx10000000000b001");
            System.out.println("2. 构建注册请求对象完成");

            // 3. 生成参数拼接字符串（用于签名）
            String signString = registerSDK.buildSignString(request);
            System.out.println("\n3. 生成的参数拼接字符串:");
            System.out.println(signString);

            // 4. 生成Ed25519密钥对（实际应用中可能是从配置或安全存储中获取）
            Ed25519Utils.KeyPairResult keyPair = Ed25519Utils.generateKeyPair();
            System.out.println("\n4. 生成Ed25519密钥对完成");

            // 5. 生成签名
            String signature = registerSDK.generateSignature(signString, keyPair.getPrivateKeyPem());
            System.out.println("\n5. 生成的签名:");
            System.out.println(signature);
            
            // 6. 验证签名
            boolean isValid = registerSDK.verifySignature(signString, signature, keyPair.getPublicKeyPem());
            System.out.println("\n6. 签名验证结果:");
            System.out.println("签名是否有效: " + isValid);
            
            // 7. 序列化请求对象为JSON（用于API调用）
            String requestJson = registerSDK.serializeRequestToJson(request);
            System.out.println("\n7. 序列化后的请求JSON:");
            System.out.println(requestJson);
            
            // 8. 使用说明
            System.out.println("\n=== 使用说明 ===");
            System.out.println("1. 使用serializeRequestToJson方法将DeviceRegisterRequest对象序列化为JSON作为请求体");
            System.out.println("2. 将生成的签名添加到请求头中（X-Device-Signature）");
            System.out.println("3. 发送POST请求到设备注册API端点，Content-Type设置为application/json");
            System.out.println("4. 服务端会使用相同的verifySignature方法验证签名的有效性");
        } catch (Exception e) {
            System.err.println("生成签名数据或验证签名失败!");
            e.printStackTrace();
        }
    }

}
