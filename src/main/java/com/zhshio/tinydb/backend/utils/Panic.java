package com.zhshio.tinydb.backend.utils;/**
 * @Auther: 张帅
 * @Date: 2023/12/10 - 12 - 10 - 10:05
 * @Description: com.zhshio.tinydb.backend.utils
 * @version: 1.0
 */

/**
 * @description:
 * @author: zs
 * @time: 2023/12/10 10:05
 */

//全局异常
public class Panic {
    public static void panic(Exception err) {
        err.printStackTrace();
        System.exit(1);
    }
}
