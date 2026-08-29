package com.raulbolivar.securitytoken.helpers;

public class Constants {

    // ─── AES-CBC-DECRYPTOR ────────────────────────────────────────────────
    public static final String CIPHER_ALG           = "AES/CBC/PKCS5Padding";
    public static final String PBKDF2_ALG           = "PBKDF2WithHmacSHA1";
    public static final int PBKDF2_ITERATIONS       = 10_000;
    public static final int KEY_SIZE_BITS           = 256;
    public static final int SALT_HEX_LENGTH         = 32;
    public static final int IV_LENGTH_BYTES         = 16;
    public static final int LEGACY_KEY_LENGTH_BYTES = 32;
}
