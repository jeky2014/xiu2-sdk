package com.mengwangbao.xiu2.util;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * Ed25519Utils 单元测试
 * 验证与 Python cryptography 库的兼容性
 */
public class Ed25519UtilsTest {

    @Test
    public void testGenerateKeyPair() throws Exception {
        System.out.println("=== 测试生成密钥对 ===");

        Ed25519Utils.KeyPairResult keyPair = Ed25519Utils.generateKeyPair();

        assertNotNull("私钥不应为空", keyPair.getPrivateKeyPem());
        assertNotNull("公钥不应为空", keyPair.getPublicKeyPem());

        assertTrue("私钥应包含 BEGIN PRIVATE KEY", keyPair.getPrivateKeyPem().contains("BEGIN PRIVATE KEY"));
        assertTrue("公钥应包含 BEGIN PUBLIC KEY", keyPair.getPublicKeyPem().contains("BEGIN PUBLIC KEY"));

        System.out.println("私钥 (PKCS8 PEM):");
        System.out.println(keyPair.getPrivateKeyPem());
        System.out.println("公钥 (X.509 SubjectPublicKeyInfo PEM):");
        System.out.println(keyPair.getPublicKeyPem());

        // 验证格式
        assertTrue("私钥格式应该有效", Ed25519Utils.validatePrivateKeyFormat(keyPair.getPrivateKeyPem()));
        assertTrue("公钥格式应该有效", Ed25519Utils.validatePublicKeyFormat(keyPair.getPublicKeyPem()));
    }

    @Test
    public void testSignAndVerify() throws Exception {
        System.out.println("\n=== 测试签名和验证 ===");

        // 生成密钥对
        Ed25519Utils.KeyPairResult keyPair = Ed25519Utils.generateKeyPair();

        // 待签名的消息
        String message = "Hello, Ed25519!";
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);

        // 签名
        String signature = Ed25519Utils.signMessage(messageBytes, keyPair.getPrivateKeyPem());
        System.out.println("原始消息: " + message);
        System.out.println("签名 (Base64): " + signature);
        System.out.println("签名长度: " + signature.length());

        assertNotNull("签名不应为空", signature);
        assertEquals("Ed25519 签名 Base64 长度应该是 88", 88, signature.length());

        // 验证签名
        boolean isValid = Ed25519Utils.verifySignature(messageBytes, signature, keyPair.getPublicKeyPem());
        System.out.println("签名验证结果: " + isValid);
        assertTrue("签名应该验证成功", isValid);
    }

    @Test
    public void testVerifyTamperedMessage() throws Exception {
        System.out.println("\n=== 测试篡改消息验证 ===");

        Ed25519Utils.KeyPairResult keyPair = Ed25519Utils.generateKeyPair();

        // 原始消息签名
        String originalMessage = "Hello, Ed25519!";
        String signature = Ed25519Utils.signMessage(
                originalMessage.getBytes(StandardCharsets.UTF_8),
                keyPair.getPrivateKeyPem()
        );

        // 篡改消息
        String tamperedMessage = "Hello, Ed25519! (tampered)";
        boolean isValid = Ed25519Utils.verifySignature(
                tamperedMessage.getBytes(StandardCharsets.UTF_8),
                signature,
                keyPair.getPublicKeyPem()
        );

        System.out.println("篡改消息验证结果: " + isValid);
        assertFalse("篡改的消息应该验证失败", isValid);
    }

    @Test
    public void testMultipleSignatures() throws Exception {
        System.out.println("\n=== 测试多次签名 ===");

        Ed25519Utils.KeyPairResult keyPair = Ed25519Utils.generateKeyPair();
        String message = "Test message";
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);

        // 生成多个签名，验证它们都是相同的（Ed25519 是确定性的）
        String sig1 = Ed25519Utils.signMessage(messageBytes, keyPair.getPrivateKeyPem());
        String sig2 = Ed25519Utils.signMessage(messageBytes, keyPair.getPrivateKeyPem());

        System.out.println("签名1: " + sig1);
        System.out.println("签名2: " + sig2);

        assertEquals("相同消息的签名应该相同（Ed25519 是确定性的）", sig1, sig2);
    }

    /**
     * 生成密钥对用于 Python 互操作性测试
     * 运行此测试，复制输出的密钥到 Python 脚本中测试
     */
    @Test
    public void generateKeysForPythonTest() throws Exception {
        System.out.println("\n=== 生成密钥对用于 Python 测试 ===");
        System.out.println("请将下面的密钥复制到 Python 脚本中测试：\n");

        Ed25519Utils.KeyPairResult keyPair = Ed25519Utils.generateKeyPair();

        System.out.println("# Java 生成的私钥");
        System.out.println("private_key_pem = '''");
        System.out.print(keyPair.getPrivateKeyPem());
        System.out.println("'''");

        System.out.println("\n# Java 生成的公钥");
        System.out.println("public_key_pem = '''");
        System.out.print(keyPair.getPublicKeyPem());
        System.out.println("'''");

        // 生成一个示例签名
        String message = "Hello, Ed25519!";
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        String signature = Ed25519Utils.signMessage(messageBytes, keyPair.getPrivateKeyPem());

        System.out.println("\n# 测试消息和签名");
        System.out.println("message = b'" + message + "'");
        System.out.println("signature = '" + signature + "'");

        System.out.println("\n# Python 验证代码：");
        System.out.println("from ed25519_utils import Ed25519Utils");
        System.out.println("is_valid = Ed25519Utils.verify_signature(message, signature, public_key_pem)");
        System.out.println("print(f'验证结果: {is_valid}')");
    }
}
