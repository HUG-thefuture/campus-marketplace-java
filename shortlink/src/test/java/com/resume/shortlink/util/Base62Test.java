package com.resume.shortlink.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Base62 编解码单元测试（纯逻辑，不依赖数据库）。
 */
class Base62Test {

    @Test
    void encode_zero() {
        assertEquals("0", Base62.encode(0));
    }

    @Test
    void encode_decode_roundtrip() {
        long[] cases = {1L, 62L, 3844L, 123456789L, Long.MAX_VALUE / 2};
        for (long n : cases) {
            assertEquals(n, Base62.decode(Base62.encode(n)), "roundtrip failed for " + n);
        }
    }

    @Test
    void encode_is_base62_charset() {
        String code = Base62.encode(123456789L);
        assertTrue(code.matches("[0-9A-Za-z]+"), "短码应只含 Base62 字符");
    }

    @Test
    void decode_rejects_illegal_char() {
        assertThrows(IllegalArgumentException.class, () -> Base62.decode("abc!"));
    }

    @Test
    void encode_rejects_negative() {
        assertThrows(IllegalArgumentException.class, () -> Base62.encode(-1));
    }
}