package com.zhshio.tinydb.backend.common;/**
 * @Auther: 张帅
 * @Date: 2023/12/10 - 12 - 10 - 10:04
 * @Description: com.zhshio.tinydb.backend.common
 * @version: 1.0
 */

/**
 * @description:
 * @author: zs
 * @time: 2023/12/10 10:04
 */

public class Error {

    // 通用异常
    public static final Exception CacheFullException = new RuntimeException("Cache is full!");
    public static final Exception FileExistsException = new RuntimeException("File already exists!");
    public static final Exception FileNotExistsException = new RuntimeException("File does not exists!");
    public static final Exception FileCannotRWException = new RuntimeException("File cannot read or write!");

    // 事务管理模块异常
    public static final Exception BadXIDFileException = new RuntimeException("Bad XID file!");
}
