package com.raulbolivar.securitytoken.helpers;

import com.raulbolivar.securitytoken.exception.DecryptionException;
import com.raulbolivar.securitytoken.model.ChannelCryptoConfig;
import com.raulbolivar.securitytoken.ports.out.CredentialDecryptorPort;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

import static com.raulbolivar.securitytoken.helpers.Constants.*;

/**
 * Replica el esquema de cifrado descrito en el documento de diseño:
 * <p>
 *   Cipher: AES-256, Mode: CBC, Encoding: base64
 *   Key size: 32 bytes, IV size: 16 bytes, Salt size: 32 (hex chars = 16 bytes)
 * <p>
 * Formato "con salt" (nuevo, ver ejemplo Postman del canal):
 *   payload = saltHex(32 chars) + base64(AES-CBC(texto, key derivada, iv))
 *   key derivada = PBKDF2WithHmacSHA1(llave_canal, salt, 10000 iteraciones, 256 bits)
 *   — así el token viaja distinto en cada request aunque el texto plano sea igual.
 * <p>
 * Formato "sin salt" (legacy, mantenido solo por interoperabilidad):
 *   payload = base64(AES-CBC(texto, llave_canal_como_bytes(32), iv))
 *   — la llave del canal se usa directamente como clave: AES-256 (por eso mide 32 chars).
 * <p>
 * IMPORTANTE — supuesto de diseño: el documento no especifica un flag explícito
 * para distinguir ambos formatos en el request. Aquí se resuelve con un intento
 * "con salt primero, legacy como fallback": si los primeros 32 caracteres son hex
 * válido Y el resto decodifica y desencripta sin error de padding, se usa ese
 * resultado; si no, se reintenta en modo legacy. Si ambos fallan, se lanza
 * DecryptionException. En un ambiente real, confirma con el equipo que genera el
 * payload (hoy la colección Postman/Fuse) si existe un flag de versión más explícito.
 */
public final class AesCbcDecryptor implements CredentialDecryptorPort {

    private final HexFormat hex = HexFormat.of();

    public String decrypt(String fieldName,
                          String cipherPayload,
                          ChannelCryptoConfig cfg) {
        if (cipherPayload == null || cipherPayload.isBlank()) {
            throw new DecryptionException(fieldName, new IllegalArgumentException("payload vacio"));
        }

        // 1) Intento modo "con salt"
        if (cipherPayload.length() > SALT_HEX_LENGTH && isHex(cipherPayload.substring(0, SALT_HEX_LENGTH))) {
            try {
                return decryptWithSalt(cipherPayload, cfg);
            } catch (Exception ignoredFallback) {
                // no es fatal todavia: puede ser legacy con coincidencia casual de hex al inicio
            }
        }

        // 2) Fallback modo legacy sin salt
        try {
            return decryptLegacy(cipherPayload, cfg);
        } catch (Exception e) {
            throw new DecryptionException(fieldName, e);
        }
    }

    private String decryptWithSalt(String payload,
                                   ChannelCryptoConfig cfg) throws Exception {
        String saltHex = payload.substring(0, SALT_HEX_LENGTH);
        String cipherTextB64 = payload.substring(SALT_HEX_LENGTH);

        byte[] salt = hex.parseHex(saltHex);
        byte[] derivedKey = deriveKeyPbkdf2(cfg.llave(), salt);
        byte[] iv = ivBytes(cfg.iv());
        byte[] cipherBytes = Base64.getDecoder().decode(cipherTextB64);

        return aesCbcDecrypt(cipherBytes, derivedKey, iv);
    }

    private String decryptLegacy(String payload,
                                 ChannelCryptoConfig cfg) throws Exception {
        byte[] key = legacyKeyBytes(cfg.llave());
        byte[] iv = ivBytes(cfg.iv());
        byte[] cipherBytes = Base64.getDecoder().decode(payload);

        return aesCbcDecrypt(cipherBytes, key, iv);
    }

    private String aesCbcDecrypt(byte[] cipherBytes,
                                 byte[] keyBytes,
                                 byte[] ivBytes) throws Exception {
        Cipher cipher = Cipher.getInstance(CIPHER_ALG);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBytes, "AES"), new IvParameterSpec(ivBytes));

        byte[] plainBytes = cipher.doFinal(cipherBytes);

        return new String(plainBytes, StandardCharsets.UTF_8);
    }

    private byte[] deriveKeyPbkdf2(String masterKey,
                                   byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALG);
        PBEKeySpec spec = new PBEKeySpec(masterKey.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_SIZE_BITS);

        return factory.generateSecret(spec).getEncoded();
    }

    private byte[] legacyKeyBytes(String llave) {
        byte[] raw = llave.getBytes(StandardCharsets.ISO_8859_1);

        if (raw.length != LEGACY_KEY_LENGTH_BYTES) {
            throw new IllegalStateException(
                    "La llave del canal debe medir exactamente 32 caracteres para modo legacy (AES-256), mide " + raw.length);
        }

        return raw;
    }

    private byte[] ivBytes(String iv) {
        byte[] raw = iv.getBytes(StandardCharsets.ISO_8859_1);

        if (raw.length != IV_LENGTH_BYTES) {
            throw new IllegalStateException("El IV del canal debe medir exactamente 16 caracteres, mide " + raw.length);
        }

        return raw;
    }

    private boolean isHex(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (Character.digit(s.charAt(i), 16) == -1) {
                return false;
            }
        }

        return true;
    }

    // --- Utilidad de solo-pruebas: genera un payload "con salt" ---------------
    // Util para construir el curl de ejemplo del README sin depender de un script
    // Python aparte. No se usa en el flujo de producción del servicio.
    public static String encryptWithSaltForTesting(String plainText,
                                                   String masterKey,
                                                   String iv) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] saltBytes = new byte[16];
            random.nextBytes(saltBytes);
            String saltHex = HexFormat.of().formatHex(saltBytes);

            SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALG);
            PBEKeySpec spec = new PBEKeySpec(masterKey.toCharArray(), saltBytes, PBKDF2_ITERATIONS, KEY_SIZE_BITS);
            byte[] derivedKey = factory.generateSecret(spec).getEncoded();

            byte[] ivBytes = iv.getBytes(StandardCharsets.ISO_8859_1);

            Cipher cipher = Cipher.getInstance(CIPHER_ALG);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(derivedKey, "AES"), new IvParameterSpec(ivBytes));
            byte[] cipherBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            return saltHex + Base64.getEncoder().encodeToString(cipherBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error generando payload de prueba", e);
        }
    }
}
