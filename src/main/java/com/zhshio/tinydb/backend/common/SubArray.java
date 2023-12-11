package com.zhshio.tinydb.backend.common;/**
 * @Auther: 张帅
 * @Date: 2023/12/11 - 12 - 11 - 9:24
 * @Description: com.zhshio.tinydb.backend.common
 * @version: 1.0
 */

/**
 * @description:
 * @author: zs
 * @time: 2023/12/11 9:24
 */

public class SubArray {
    public byte[] raw;
    public int start;

    public int end;

    public SubArray(byte[] raw, int start, int end) {
        this.raw = raw;
        this.start = start;
        this.end = end;
    }
}
