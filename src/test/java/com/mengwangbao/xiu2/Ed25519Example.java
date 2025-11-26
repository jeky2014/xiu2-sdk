package com.mengwangbao.xiu2;

import com.mengwangbao.xiu2.util.Ed25519Utils;

import java.nio.charset.StandardCharsets;

public class Ed25519Example {
    public static void main(String[] args) {
        try {
            System.out.println("=== Ed25519 工具类演示 ===\n");

            // 1. 生成密钥对
            System.out.println("1. 生成密钥对:");
            Ed25519Utils.KeyPairResult keyPair = Ed25519Utils.generateKeyPair();
            System.out.println("私钥 (PKCS8 PEM):");
            System.out.println(keyPair.getPrivateKeyPem());
            System.out.println("\n公钥 (X.509 SubjectPublicKeyInfo PEM):");
            System.out.println(keyPair.getPublicKeyPem());

            // 2. 签名
            System.out.println("\n2. 签名消息:");
            String message = "Hello, Ed25519!";
            byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
            String signature = Ed25519Utils.signMessage(messageBytes, keyPair.getPrivateKeyPem());
            System.out.println("原始消息: " + message);
            System.out.println("签名 (Base64): " + signature);

            // 3. 验证签名
            System.out.println("\n3. 验证签名:");
            boolean isValid = Ed25519Utils.verifySignature(messageBytes, signature, keyPair.getPublicKeyPem());
            System.out.println("签名验证结果: " + isValid);

            System.out.println("\n=== 演示完成 ===");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
