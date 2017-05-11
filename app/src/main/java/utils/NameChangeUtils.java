package utils;


import android.util.Base64;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * 字符串 加密 类
 */
public class NameChangeUtils {
    private static final boolean DEBUG = AppConfig.DEBUG;
    public static String KEY = "key";

    public static String decodeEncryptionSaltContent(String content) throws IOException {
        //2、base64 decode
        byte[] decode = decode(content);
        if (decode == null) {
            return null;
        }
        //3、加盐 反作用
        decode = addSalt(decode);
        return new String(decode);
    }

    public static byte[] addSalt(byte[] decode) throws UnsupportedEncodingException {
        if (KEY != null) {
            byte[] keys = KEY.getBytes("utf-8");
            for (int i = 0; i < keys.length; i++) {
                decode = xor(decode, keys[i]);
            }
        }
        return decode;
    }

    /**
     * 先^ 后 encode
     *
     * @return
     * @throws IOException
     */
    public static String encodeEncryptSaltContent(String content) throws IOException {
        //1.变字节数组 加盐
        byte[] bbs = content.getBytes("utf-8");
        bbs = addSalt(bbs);
        //2、base64 endcode
        String result = Base64.encodeToString(bbs, Base64.URL_SAFE).trim();
        return result;
    }

    /**
     * 先^ 后 encode
     *
     * @return
     * @throws IOException
     */
    public static String encodeEncryptSaltContent(byte[] content) throws IOException {
        content = addSalt(content);
        String result = Base64.encodeToString(content, Base64.URL_SAFE).trim();
        return result;
    }

    /**
     * 获取对应的base64吗
     *
     * @return
     * @throws IOException
     */
    public static byte[] decode(String content) throws IOException {
        byte[] bbs = null;
        bbs = Base64.decode(content, Base64.DEFAULT);
        return bbs;
    }

    /**
     * 编码
     *
     * @return
     * @throws IOException
     */
    public static String encode(String content) throws IOException {
        byte[] bbs = content.getBytes();
        return Base64.encodeToString(bbs, Base64.DEFAULT);
    }
    /**
     * 编码
     *
     * @return
     * @throws IOException
     */
    public static String encode(byte[] bbs) throws IOException {
        return Base64.encodeToString(bbs, Base64.DEFAULT);
    }

    public static byte[] xor(byte[] orgContent, byte k) {
        byte[] res = new byte[orgContent.length];
        for (int i = 0; i < orgContent.length; i++) {
            res[i] = (byte) (orgContent[i] ^ k);
        }
        return res;
    }
}
