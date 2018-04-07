package guru.h4t_eng.util;

import org.apache.commons.codec.Charsets;
import org.apache.commons.lang3.RandomStringUtils;

import static guru.h4t_eng.util.SecurityEncodeUtil.*;

/**
 * SimpleEncryptUtil.
 *
 * Use it only for LOW RISK DATA, with low damage if it will be decipher.
 *
 * Use SecurityEncodeUtil for HIGH RISK DATA!
 *
 * Created by aalexeenka on 22.11.2016.
 */
public final class SimpleEncryptUtil {

    private SimpleEncryptUtil() {
    }

    private final static byte[] keyBytes = RandomStringUtils.random(59).getBytes();

    private final static String CONST_ENCODE_KEY = "7KiH555F5rCn4ZqX77yy7baw77W077y65ZiZ74Cl6bm55qOU646x7qa64Z6P7o2b5IiP7ZCd4qaZ7J" +
            "-S76KJ8a2hnOCysOC3s-O3me-ij-6aaaarvua5pu6yg-iChPCRu7Pnn7bwl5yV5KWq64qA5Jyf4L-d7rOt57a764mv7KWW7ZWx7pWt7qm" +
            "-5IWg55ey76Od4r6T56KK6q2G7Ky54oSc8JqmmeakrKkBBBB";

    private final static byte[] CONST_ENCODE_KEY_BYTES = getDecodedBytesBase64(CONST_ENCODE_KEY);

    public static String encode(String s) {
        String encode64 = encodeBase64(s);
        final byte[] bytes = xorWithKey(encode64.getBytes(), keyBytes);
        return encodeBase64(bytes);
    }

    public static String decode(String s) {
        final byte[] decodedBytesBase64 = getDecodedBytesBase64(s);
        final byte[] bytes = xorWithKey(decodedBytesBase64, keyBytes);
        return decodeBase64(new String(bytes, Charsets.UTF_8));
    }

    public static String constEncode(String s) {
        String encode64 = encodeBase64(s);
        final byte[] bytes = xorWithKey(encode64.getBytes(), CONST_ENCODE_KEY_BYTES);
        return encodeBase64(bytes);
    }

    public static String constDecode(String s) {
        final byte[] decodedBytesBase64 = getDecodedBytesBase64(s);
        final byte[] bytes = xorWithKey(decodedBytesBase64, CONST_ENCODE_KEY_BYTES);
        return decodeBase64(new String(bytes, Charsets.UTF_8));
    }

    private static byte[] xorWithKey(byte[] input, byte[] keyBytes) {
        byte[] out = new byte[input.length];
        for (int i = 0; i < input.length; i++) {
            out[i] = (byte) (input[i] ^ keyBytes[i%keyBytes.length]);
        }
        return out;
    }
}
