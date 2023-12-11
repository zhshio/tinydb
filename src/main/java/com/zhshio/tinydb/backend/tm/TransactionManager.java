package com.zhshio.tinydb.backend.tm;

import com.zhshio.tinydb.commob.Error;
import com.zhshio.tinydb.backend.utils.Panic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @Auther: 张帅
 * @Date: 2023/12/10 - 12 - 10 - 10:05
 * @Description: com.zhshio.tinydb.backend.tm
 * @version: 1.0
 */
public interface TransactionManager {

    //开启事务
    long begin();
    // 提交事务
    void commit(long xid);
    //回滚事务
    void abort(long xid);

    //查询事务是否处于正在进行状态
    boolean isActive(long xid);
    //查询事务是否处于已提交状态
    boolean isCommitted(long xid);
    //查询事务是否处于已回滚状态
    boolean isAborted(long xid);
    //关闭索引管理
    void close();

    public static TransactionManagerImpl create(String path) {
        File file = new File(path + TransactionManagerImpl.XID_SUFFIX);
        try {
            if (!file.createNewFile()) {
                Panic.panic(Error.FileExistsException);
            }
        } catch (Exception e) {
            Panic.panic(e);
        }

        if (!file.canRead() || !file.canWrite()) {
            Panic.panic(Error.FileCannotRWException);
        }

        FileChannel fileChannel = null;
        RandomAccessFile randomAccessFile = null;

        try {
            randomAccessFile = new RandomAccessFile(file, "rw");
            fileChannel = randomAccessFile.getChannel();
        } catch (FileNotFoundException e) {
            Panic.panic(e);
        }

        ByteBuffer buffer = ByteBuffer.wrap(new byte[TransactionManagerImpl.LEN_XID_HEADER_LENGTH]);

        try {
            fileChannel.position();
            fileChannel.write(buffer);
        } catch (IOException e) {
            Panic.panic(e);
        }

        return new TransactionManagerImpl(randomAccessFile, fileChannel);
    }

    public static TransactionManagerImpl open(String path) {
        File file = new File(path+TransactionManagerImpl.XID_SUFFIX);
        if(!file.exists()) {
            Panic.panic(Error.FileNotExistsException);
        }
        if(!file.canRead() || !file.canWrite()) {
            Panic.panic(Error.FileCannotRWException);
        }

        FileChannel fileChannel = null;
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(file, "rw");
            fileChannel = randomAccessFile.getChannel();
        } catch (FileNotFoundException e) {
            Panic.panic(e);
        }

        return new TransactionManagerImpl(randomAccessFile, fileChannel);
    }

}
