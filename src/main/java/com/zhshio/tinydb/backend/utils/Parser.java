package com.zhshio.tinydb.backend.utils;/**
 * @Auther: 张帅
 * @Date: 2023/12/10 - 12 - 10 - 10:41
 * @Description: com.zhshio.tinydb.backend.utils
 * @version: 1.0
 */

import java.nio.ByteBuffer;

/**
 * @description:
 * @author: zs
 * @time: 2023/12/10 10:41
 */

public class Parser {
    public static byte[] long2Byte(long value) {
        return ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(value).array();
    }

    public static long parseLong(byte[] buf) {
        ByteBuffer buffer = ByteBuffer.wrap(buf, 0, 8);
        return buffer.getLong();
    }

}
