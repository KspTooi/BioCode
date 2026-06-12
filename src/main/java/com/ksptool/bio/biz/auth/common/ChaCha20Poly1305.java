package com.ksptool.bio.biz.auth.common;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

/**
 * ChaCha20-Poly1305 AEAD 加密工具类
 * <p>
 * 基于 Java 内置 SunJCE 实现，兼容 JDK 11+，无需 BouncyCastle。
 */
public final class ChaCha20Poly1305 {

    private static final String CIPHER_ALGORITHM = "ChaCha20-Poly1305";
    private static final String KEY_ALGORITHM = "ChaCha20";
    private static final int KEY_SIZE_BITS = 256;
    private static final int NONCE_LENGTH_BYTES = 12;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private ChaCha20Poly1305() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 生成 256 位的随机密钥
     */
    public static SecretKey generateKey() {
        byte[] keyBytes = new byte[KEY_SIZE_BITS / 8];
        SECURE_RANDOM.nextBytes(keyBytes);
        return new SecretKeySpec(keyBytes, KEY_ALGORITHM);
    }

    /**
     * 将字节数组包装为 SecretKey
     */
    public static SecretKey getSecretKey(byte[] keyBytes) {
        validateKeyLength(keyBytes);
        return new SecretKeySpec(keyBytes, KEY_ALGORITHM);
    }

    /**
     * 生成 12 字节 (96位) 的安全随机 IV (兼容旧命名)
     */
    public static byte[] generateIV() {
        byte[] iv = new byte[NONCE_LENGTH_BYTES];
        SECURE_RANDOM.nextBytes(iv);
        return iv;
    }

    /**
     * 生成 12 字节 (96位) 的安全随机 nonce (新命名)
     */
    public static byte[] generateNonce() {
        byte[] nonce = new byte[NONCE_LENGTH_BYTES];
        SECURE_RANDOM.nextBytes(nonce);
        return nonce;
    }

    /**
     * 基础加密方法 (字节数组)
     *
     * @param pt  明文
     * @param key 密钥 (256位)
     * @param iv  随机数 (12字节)
     * @param aad 附加认证数据 (可选，传 null 即可)
     * @return 密文 (原文长度 + 16字节的Tag)
     */
    public static byte[] encrypt(byte[] pt, SecretKey key, byte[] iv, byte[] aad) throws GeneralSecurityException {
        Objects.requireNonNull(pt, "明文不能为空");
        Objects.requireNonNull(key, "密钥不能为空");
        validateNonceLength(iv);

        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));

        if (aad != null && aad.length > 0) {
            cipher.updateAAD(aad);
        }

        return cipher.doFinal(pt);
    }

    /**
     * 基础解密方法 (字节数组)
     *
     * @param ct  密文 (包含16字节Tag)
     * @param key 密钥 (256位)
     * @param iv  随机数 (12字节)
     * @param aad 附加认证数据 (必须与加密时一致，无则传 null)
     * @return 解密后的明文
     */
    public static byte[] decrypt(byte[] ct, SecretKey key, byte[] iv, byte[] aad) throws GeneralSecurityException {
        Objects.requireNonNull(ct, "密文不能为空");
        Objects.requireNonNull(key, "密钥不能为空");
        validateNonceLength(iv);

        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));

        if (aad != null && aad.length > 0) {
            cipher.updateAAD(aad);
        }

        return cipher.doFinal(ct);
    }

    /**
     * 加密字符串并返回 Base64 格式的密文 (兼容旧命名)
     */
    public static String encryptString(String pt, SecretKey key, byte[] iv) throws GeneralSecurityException {
        byte[] cipherBytes = encrypt(pt.getBytes(StandardCharsets.UTF_8), key, iv, null);
        return Base64.getEncoder().encodeToString(cipherBytes);
    }

    /**
     * 解密 Base64 格式的密文并返回字符串 (兼容旧命名)
     */
    public static String decryptString(String ct, SecretKey key, byte[] iv) throws GeneralSecurityException {
        byte[] cipherBytes = Base64.getDecoder().decode(ct);
        byte[] plainBytes = decrypt(cipherBytes, key, iv, null);
        return new String(plainBytes, StandardCharsets.UTF_8);
    }

    /**
     * 加密字符串并返回 Base64 格式的密文 (新命名)
     */
    public static String encryptToBase64(String pt, SecretKey key, byte[] nonce) throws GeneralSecurityException {
        return encryptString(pt, key, nonce);
    }

    /**
     * 解密 Base64 格式的密文并返回字符串 (新命名)
     */
    public static String decryptFromBase64(String base64Ciphertext, SecretKey key, byte[] nonce) throws GeneralSecurityException {
        return decryptString(base64Ciphertext, key, nonce);
    }

    private static void validateNonceLength(byte[] nonce) {
        Objects.requireNonNull(nonce, "Nonce 不能为空");
        if (nonce.length != NONCE_LENGTH_BYTES) {
            throw new IllegalArgumentException("ChaCha20-Poly1305 requires exactly 12 bytes for nonce. Provided: " + nonce.length);
        }
    }

    private static void validateKeyLength(byte[] keyBytes) {
        Objects.requireNonNull(keyBytes, "密钥字节数组不能为空");
        if (keyBytes.length != KEY_SIZE_BITS / 8) {
            throw new IllegalArgumentException("ChaCha20 requires exactly 32 bytes (256 bits) for key. Provided: " + keyBytes.length);
        }
    }
}