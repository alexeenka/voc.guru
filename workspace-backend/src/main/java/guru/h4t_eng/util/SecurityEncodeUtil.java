package guru.h4t_eng.util;

import guru.h4t_eng.logs.AppLoggerFactory;
import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * SecurityEncodeUtil.
 * <p>
 * Created by aalexeenka on 27.04.2016.
 */
public class SecurityEncodeUtil {

    private static final Logger LOG = AppLoggerFactory.getH4TLog(SecurityEncodeUtil.class);

    /**
     * Encode string. String converted to byte array. Then 4 bytes, representing
     * original array length added to this array. After that array encoded base64 with url-safe flag and
     * converted back to string.
     *
     * @param toEncode string need to be encoded
     * @return encoded string
     */
    public static String encodeBase64(String toEncode) {
        byte[] stringBytes = toEncode.getBytes(Charsets.UTF_8);
        return encodeBase64(stringBytes);
    }

    public static String encodeBase64(byte[] input) {
        int length = input.length;
        byte[] bytes = ArrayUtils.addAll(input, (byte) (length), (byte) (length >> 8), (byte) (length >> 16),
                (byte) (length >> 24));

        return new String(Base64.encodeBase64(bytes, false, true), Charsets.UTF_8);
    }

    /**
     * Decode string. Algorithm reverted from encode method. Input string
     * decoded to byte array using base64. Last 4
     * bytes chunked and collected to int value. This assumed to be rest array
     * length. Method throws runtime exception in case of invalid input string.
     *
     * @param toDecode String to be decoded
     * @return decoded string
     */
    public static String decodeBase64(String toDecode) {
        byte[] bytes = getDecodedBytesBase64(toDecode);
        return new String(bytes, Charsets.UTF_8);
    }

    public static byte[] getDecodedBytesBase64(String toDecode) {
        if (toDecode == null) {
            String message = "Origin value is null";
            LOG.error(message);
            throw new RuntimeException(message);
        }

        byte[] decodedBytes;
        try {
            decodedBytes = Base64.decodeBase64(toDecode);
        } catch (Exception e) {
            String message = "Unable to decode base64. Input string: " + toDecode;
            LOG.error(message);
            throw new RuntimeException(message, e);
        }

        if (decodedBytes.length < 4) {
            String message = "Encoded string is invalid. Expected length at least 4, got " + decodedBytes.length;
            LOG.error(message);
            throw new RuntimeException(message);
        }

        int length = ((0xFF & decodedBytes[decodedBytes.length - 1]) << 24)
                | ((0xFF & decodedBytes[decodedBytes.length - 2]) << 16)
                | ((0xFF & decodedBytes[decodedBytes.length - 3]) << 8)
                | (0xFF & decodedBytes[decodedBytes.length - 4]);

        if (length != decodedBytes.length - 4) {
            String message = "Encoded string is invalid. Expected length: " + length + ". Actual length: " + decodedBytes.length;
            LOG.error(message);
            throw new RuntimeException(message);
        }

        return ArrayUtils.subarray(decodedBytes, 0, length);
    }


    // *** Next, you'll need the key and initialization vector bytes
    private static final String CIPHER_ALG = "RC4";
    private static final SecretKeySpec cipherKey = new SecretKeySpec(getRandBytes(), CIPHER_ALG);

    /**
     * Example: http://stackoverflow.com/questions/1205135/how-to-encrypt-string-in-java
     *
     * Thread safe cipher
     */
    private static final ThreadLocal<Cipher> threadLocalEncodeCipher = new ThreadLocal<Cipher>() {
        @Override
        protected Cipher initialValue() {
            try {
                // http://www.javamex.com/tutorials/cryptography/ciphers.shtml performance
                Cipher instance = Cipher.getInstance(CIPHER_ALG);
                instance.init(Cipher.ENCRYPT_MODE, cipherKey);
                return instance;
            } catch (Exception e) {
                LOG.error("threadLocalEncodeCipher", e);
                throw new RuntimeException(e);
            }
        }
    };

    private static final ThreadLocal<Cipher> threadLocalDecodeCipher = new ThreadLocal<Cipher>() {
        @Override
        protected Cipher initialValue() {
            try {
                Cipher instance = Cipher.getInstance(CIPHER_ALG);
                instance.init(Cipher.DECRYPT_MODE, cipherKey);
                return instance;
            } catch (Exception e) {
                LOG.error("threadLocalDecodeCipher", e);
                throw new RuntimeException(e);
            }
        }
    };

    private static byte[] getRandBytes() {
        java.security.SecureRandom random = new java.security.SecureRandom();
        byte bytes[] = new byte[16]; // max for RC4 algorith
        random.nextBytes(bytes);
        return bytes;
    }

    public static String cipherEncode(String orig) {
        try {
            byte[] input = orig.getBytes(Charsets.UTF_8);

            Cipher cipher = threadLocalEncodeCipher.get();

            byte[] encrypted = new byte[cipher.getOutputSize(input.length)];
            int enc_len = cipher.update(input, 0, input.length, encrypted, 0);

            //noinspection UnusedAssignment
            enc_len += cipher.doFinal(encrypted, enc_len);

            return encodeBase64(encrypted);
        } catch (Throwable ex) {
            LOG.error("cipherEncode", ex);
            throw new RuntimeException(ex);
        }
    }

    public static String cipherDecode(String decoded) {
        try {
            byte[] encrypted = getDecodedBytesBase64(decoded);
            int enc_len = encrypted.length;

            Cipher cipher = threadLocalDecodeCipher.get();
            byte[] decrypted = new byte[cipher.getOutputSize(enc_len)];
            int dec_len = cipher.update(encrypted, 0, enc_len, decrypted, 0);

            //noinspection UnusedAssignment
            dec_len += cipher.doFinal(decrypted, dec_len);

            return new String(decrypted, Charsets.UTF_8);
        } catch (Throwable ex) {
            LOG.error("cipherDecode", ex);
            throw new RuntimeException(ex);
        }
    }
}
