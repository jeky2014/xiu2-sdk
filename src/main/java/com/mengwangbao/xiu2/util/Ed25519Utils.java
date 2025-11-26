package com.mengwangbao.xiu2.util;

import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.edec.EdECObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.SecureRandom;
import java.security.Security;

/**
 * Ed25519 工具类
 * 与 Python cryptography 库完全兼容
 * 生成标准的 RFC 8410 格式密钥
 */
public class Ed25519Utils {

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * 生成 Ed25519 密钥对
     */
    public static KeyPairResult generateKeyPair() throws Exception {
        SecureRandom random = new SecureRandom();
        Ed25519PrivateKeyParameters privateKeyParams = new Ed25519PrivateKeyParameters(random);
        Ed25519PublicKeyParameters publicKeyParams = privateKeyParams.generatePublicKey();

        String privatePem = encodePrivateKeyToPem(privateKeyParams);
        String publicPem = encodePublicKeyToPem(publicKeyParams);

        return new KeyPairResult(privatePem, publicPem);
    }

    /**
     * 使用私钥对消息进行签名
     */
    public static String signMessage(byte[] message, String privateKeyPem) throws Exception {
        Ed25519PrivateKeyParameters privateKey = decodePrivateKeyFromPem(privateKeyPem);

        org.bouncycastle.crypto.signers.Ed25519Signer signer = new org.bouncycastle.crypto.signers.Ed25519Signer();
        signer.init(true, privateKey);
        signer.update(message, 0, message.length);
        byte[] signatureBytes = signer.generateSignature();

        return java.util.Base64.getEncoder().encodeToString(signatureBytes);
    }

    /**
     * 使用公钥验证签名
     */
    public static boolean verifySignature(byte[] message, String signatureB64, String publicKeyPem) throws Exception {
        Ed25519PublicKeyParameters publicKey = decodePublicKeyFromPem(publicKeyPem);

        byte[] signatureBytes = java.util.Base64.getDecoder().decode(signatureB64);

        org.bouncycastle.crypto.signers.Ed25519Signer verifier = new org.bouncycastle.crypto.signers.Ed25519Signer();
        verifier.init(false, publicKey);
        verifier.update(message, 0, message.length);
        return verifier.verifySignature(signatureBytes);
    }

    /**
     * 将私钥编码为 PEM 格式
     * 生成符合 RFC 8410 标准的 PKCS#8 格式
     */
    private static String encodePrivateKeyToPem(Ed25519PrivateKeyParameters privateKey) throws IOException {
        // 获取 32 字节的私钥数据
        byte[] privateKeyBytes = privateKey.getEncoded();

        // 按照 RFC 8410 标准构造 PKCS#8:
        // PrivateKeyInfo ::= SEQUENCE {
        //   version                   Version,
        //   privateKeyAlgorithm       PrivateKeyAlgorithmIdentifier,
        //   privateKey                OCTET STRING,
        //   attributes           [0]  IMPLICIT Attributes OPTIONAL
        // }

        // 1. 创建 AlgorithmIdentifier (OID: 1.3.101.112 for Ed25519)
        AlgorithmIdentifier algorithmIdentifier = new AlgorithmIdentifier(EdECObjectIdentifiers.id_Ed25519);

        // 2. 将私钥包装为 OCTET STRING
        DEROctetString privateKeyOctetString = new DEROctetString(privateKeyBytes);

        // 3. 创建 PrivateKeyInfo
        PrivateKeyInfo privateKeyInfo = new PrivateKeyInfo(algorithmIdentifier, privateKeyOctetString);

        // 4. 编码为 PEM
        byte[] encoded = privateKeyInfo.getEncoded("DER");

        StringWriter stringWriter = new StringWriter();
        try (PemWriter pemWriter = new PemWriter(stringWriter)) {
            pemWriter.writeObject(new PemObject("PRIVATE KEY", encoded));
        }
        return stringWriter.toString();
    }

    /**
     * 将公钥编码为 PEM 格式
     * 生成符合 RFC 8410 标准的 X.509 SubjectPublicKeyInfo 格式
     */
    private static String encodePublicKeyToPem(Ed25519PublicKeyParameters publicKey) throws IOException {
        // 获取 32 字节的公钥数据
        byte[] publicKeyBytes = publicKey.getEncoded();

        // 按照 RFC 8410 标准构造 SubjectPublicKeyInfo:
        // SubjectPublicKeyInfo ::= SEQUENCE {
        //   algorithm         AlgorithmIdentifier,
        //   subjectPublicKey  BIT STRING
        // }

        // 1. 创建 AlgorithmIdentifier
        AlgorithmIdentifier algorithmIdentifier = new AlgorithmIdentifier(EdECObjectIdentifiers.id_Ed25519);

        // 2. 创建 SubjectPublicKeyInfo
        SubjectPublicKeyInfo subjectPublicKeyInfo = new SubjectPublicKeyInfo(algorithmIdentifier, publicKeyBytes);

        // 3. 编码为 PEM
        byte[] encoded = subjectPublicKeyInfo.getEncoded("DER");

        StringWriter stringWriter = new StringWriter();
        try (PemWriter pemWriter = new PemWriter(stringWriter)) {
            pemWriter.writeObject(new PemObject("PUBLIC KEY", encoded));
        }
        return stringWriter.toString();
    }

    /**
     * 从 PEM 格式解码私钥
     */
    private static Ed25519PrivateKeyParameters decodePrivateKeyFromPem(String privateKeyPem) throws Exception {
        try (PemReader pemReader = new PemReader(new StringReader(privateKeyPem))) {
            PemObject pemObject = pemReader.readPemObject();
            if (pemObject == null) {
                throw new IllegalArgumentException("无效的 PEM 格式");
            }

            // 解析 PKCS#8 PrivateKeyInfo
            PrivateKeyInfo privateKeyInfo = PrivateKeyInfo.getInstance(pemObject.getContent());

            // 验证算法 OID
            if (!EdECObjectIdentifiers.id_Ed25519.equals(privateKeyInfo.getPrivateKeyAlgorithm().getAlgorithm())) {
                throw new IllegalArgumentException("不是 Ed25519 私钥");
            }

            // 提取私钥数据 (OCTET STRING 包装的 32 字节)
            ASN1Encodable privateKeyData = privateKeyInfo.parsePrivateKey();
            byte[] privateKeyBytes;

            if (privateKeyData instanceof ASN1OctetString) {
                privateKeyBytes = ((ASN1OctetString) privateKeyData).getOctets();
            } else {
                throw new IllegalArgumentException("无效的私钥数据格式");
            }

            // Ed25519 私钥必须是 32 字节
            if (privateKeyBytes.length != 32) {
                throw new IllegalArgumentException("Ed25519 私钥长度必须是 32 字节，实际: " + privateKeyBytes.length);
            }

            return new Ed25519PrivateKeyParameters(privateKeyBytes, 0);
        }
    }

    /**
     * 从 PEM 格式解码公钥
     */
    private static Ed25519PublicKeyParameters decodePublicKeyFromPem(String publicKeyPem) throws Exception {
        try (PemReader pemReader = new PemReader(new StringReader(publicKeyPem))) {
            PemObject pemObject = pemReader.readPemObject();
            if (pemObject == null) {
                throw new IllegalArgumentException("无效的 PEM 格式");
            }

            // 解析 SubjectPublicKeyInfo
            SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(pemObject.getContent());

            // 验证算法 OID
            if (!EdECObjectIdentifiers.id_Ed25519.equals(publicKeyInfo.getAlgorithm().getAlgorithm())) {
                throw new IllegalArgumentException("不是 Ed25519 公钥");
            }

            // 提取公钥数据 (BIT STRING 的 32 字节)
            byte[] publicKeyBytes = publicKeyInfo.getPublicKeyData().getBytes();

            // Ed25519 公钥必须是 32 字节
            if (publicKeyBytes.length != 32) {
                throw new IllegalArgumentException("Ed25519 公钥长度必须是 32 字节，实际: " + publicKeyBytes.length);
            }

            return new Ed25519PublicKeyParameters(publicKeyBytes, 0);
        }
    }

    /**
     * 验证私钥格式
     */
    public static boolean validatePrivateKeyFormat(String privateKeyPem) {
        try {
            decodePrivateKeyFromPem(privateKeyPem);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证公钥格式
     */
    public static boolean validatePublicKeyFormat(String publicKeyPem) {
        try {
            decodePublicKeyFromPem(publicKeyPem);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 密钥对结果类
     */
    public static class KeyPairResult {
        private final String privateKeyPem;
        private final String publicKeyPem;

        public KeyPairResult(String privateKeyPem, String publicKeyPem) {
            this.privateKeyPem = privateKeyPem;
            this.publicKeyPem = publicKeyPem;
        }

        public String getPrivateKeyPem() {
            return privateKeyPem;
        }

        public String getPublicKeyPem() {
            return publicKeyPem;
        }

        @Override
        public String toString() {
            return "KeyPairResult{\n" +
                    "privateKeyPem=\n" + privateKeyPem +
                    "\npublicKeyPem=\n" + publicKeyPem +
                    '}';
        }
    }
}
