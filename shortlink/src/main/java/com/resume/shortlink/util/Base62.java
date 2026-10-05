package com.resume.shortlink.util;

/**
 * Base62 编解码工具。
 *
 * <p><b>为什么用 Base62？</b>短码要「短、可读、可放进 URL」：
 * 62 个字符（0-9 a-z A-Z）不含任何需要转义的符号，比 Base64 少了 `+/=` 等 URL 不友好字符。
 * 62^6 ≈ 568 亿，6 位短码足够日常业务使用。</p>
 */
public final class Base62 {

    /** 62 个字符的索引表 */
    private static final String ALPHABET =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = ALPHABET.length();

    private Base62() {
    }

    /**
     * 把非负十进制数编码为 Base62 字符串。
     *
     * @param num 非负整数
     * @return （可能含前导 0）的 Base62 字符串
     */
    public static String encode(long num) {
        if (num < 0) {
            throw new IllegalArgumentException("不支持负数");
        }
        if (num == 0) {
            return String.valueOf(ALPHABET.charAt(0));
        }
        StringBuilder sb = new StringBuilder();
        while (num > 0) {
            int rem = (int) (num % BASE);
            sb.append(ALPHABET.charAt(rem));
            num = num / BASE;
        }
        return sb.reverse().toString();
    }

    /**
     * 把 Base62 字符串解码回十进制数（用于测试/校验）。
     */
    public static long decode(String code) {
        long num = 0;
        for (int i = 0; i < code.length(); i++) {
            int value = ALPHABET.indexOf(code.charAt(i));
            if (value < 0) {
                throw new IllegalArgumentException("非法 Base62 字符: " + code.charAt(i));
            }
            // 防溢出检查
            if (num > (Long.MAX_VALUE - value) / BASE) {
                throw new IllegalArgumentException("数值溢出");
            }
            num = num * BASE + value;
        }
        return num;
    }
}