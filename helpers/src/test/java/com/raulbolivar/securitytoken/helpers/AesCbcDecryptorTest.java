package com.raulbolivar.securitytoken.helpers;

import com.raulbolivar.securitytoken.exception.DecryptionException;
import com.raulbolivar.securitytoken.model.ChannelCryptoConfig;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AesCbcDecryptorTest {

    private static final String KEY_32 = "12345678901234567890123456789012";
    private static final String IV_16 = "1234567890123456";

    private final AesCbcDecryptor decryptor = new AesCbcDecryptor();

    @Test
    void decryptShouldHandleSaltPayloadGeneratedByHelper() {
        ChannelCryptoConfig cfg = new ChannelCryptoConfig("8", KEY_32, IV_16);
        String payload = AesCbcDecryptor.encryptWithSaltForTesting("my-client", KEY_32, IV_16);

        String plain = decryptor.decrypt("client_id", payload, cfg);

        assertEquals("my-client", plain);
    }

    @Test
    void decryptShouldHandleLegacyPayload() throws Exception {
        ChannelCryptoConfig cfg = new ChannelCryptoConfig("8", KEY_32, IV_16);
        String payload = encryptLegacy("secret-value", KEY_32, IV_16);

        String plain = decryptor.decrypt("client_secret", payload, cfg);

        assertEquals("secret-value", plain);
    }

    @Test
    void decryptShouldTrySaltThenFallbackAndFailWithMeaningfulException() {
        ChannelCryptoConfig cfg = new ChannelCryptoConfig("8", KEY_32, IV_16);

        DecryptionException exception = assertThrows(
                DecryptionException.class,
                () -> decryptor.decrypt("scope", "0123456789abcdef0123456789abcdef###", cfg)
        );

        assertEquals("No fue posible descifrar el campo 'scope'", exception.getMessage());
    }

    @Test
    void decryptShouldRejectBlankPayload() {
        ChannelCryptoConfig cfg = new ChannelCryptoConfig("8", KEY_32, IV_16);

        DecryptionException exception = assertThrows(
                DecryptionException.class,
                () -> decryptor.decrypt("grant_type", " ", cfg)
        );

        assertEquals("No fue posible descifrar el campo 'grant_type'", exception.getMessage());
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
    }

    @Test
    void decryptShouldFailWhenLegacyKeyLengthIsInvalid() {
        ChannelCryptoConfig cfg = new ChannelCryptoConfig("8", "short", IV_16);
        String payload = Base64.getEncoder().encodeToString("bytes".getBytes(StandardCharsets.UTF_8));

        DecryptionException exception = assertThrows(
                DecryptionException.class,
                () -> decryptor.decrypt("client_id", payload, cfg)
        );

        assertTrue(exception.getCause() instanceof IllegalStateException);
    }

    @Test
    void encryptWithSaltForTestingShouldWrapErrors() {
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> AesCbcDecryptor.encryptWithSaltForTesting("value", KEY_32, "short-iv")
        );

        assertEquals("Error generando payload de prueba", exception.getMessage());
    }

    @Test
    void constantsShouldExposeExpectedValues() {
        assertEquals("AES/CBC/PKCS5Padding", Constants.CIPHER_ALG);
        assertEquals("PBKDF2WithHmacSHA1", Constants.PBKDF2_ALG);
        assertEquals(10_000, Constants.PBKDF2_ITERATIONS);
        assertEquals(256, Constants.KEY_SIZE_BITS);
        assertEquals(32, Constants.SALT_HEX_LENGTH);
        assertEquals(16, Constants.IV_LENGTH_BYTES);
        assertEquals(32, Constants.LEGACY_KEY_LENGTH_BYTES);
    }

    private static String encryptLegacy(String plainText, String key, String iv) throws Exception {
        Cipher cipher = Cipher.getInstance(Constants.CIPHER_ALG);
        cipher.init(
                Cipher.ENCRYPT_MODE,
                new SecretKeySpec(key.getBytes(StandardCharsets.ISO_8859_1), "AES"),
                new IvParameterSpec(iv.getBytes(StandardCharsets.ISO_8859_1))
        );
        byte[] cipherBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(cipherBytes);
    }
}
