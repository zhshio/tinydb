package com.zhshio.tinydb.backend.tm;/**
 * @Auther: 张帅
 * @Date: 2023/12/10 - 12 - 10 - 10:14
 * @Description: com.zhshio.tinydb.backend.tm
 * @version: 1.0
 */

import com.zhshio.tinydb.commob.Error;
import com.zhshio.tinydb.backend.utils.Panic;
import com.zhshio.tinydb.backend.utils.Parser;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @description:
 * @author: zs
 * @time: 2023/12/10 10:14
 */

public class TransactionManagerImpl implements TransactionManager{

    // XID文件头长度
    static final int LEN_XID_HEADER_LENGTH = 8;
    //事务占用长度
    private static final int XID_FIELD_SIZE = 1;
    //事务三种状态
    private static final byte FIELD_TRAN_ACTIVE = 0;
    private static final byte FIELD_TRAN_COMMITTED = 1;
    private static final byte FIELD_TRAN_ABORTED = 2;
    //超级事务(此状态事务永远为committed状态)
    public static final long SUPER_XID = 0;
    // XID文件后缀
    static final String XID_SUFFIX = ".xid";

    private RandomAccessFile file;

    private FileChannel fileChannel;

    private long xidCounter;

    private Lock counterLock;

    // 初始化事务管理器
    TransactionManagerImpl(RandomAccessFile file, FileChannel fileChannel) {
        this.file = file;
        this.fileChannel = fileChannel;
        counterLock = new ReentrantLock();
        checkXIDCounter();
    }

    // 开启事务, 并返回XID
    @Override
    public long begin() {
        counterLock.lock();
        try {
            long xid = xidCounter + 1;
            updateXID(xid, FIELD_TRAN_ACTIVE);
            incrXIDCounter();
            return xid;
        } finally {
            counterLock.unlock();
        }
    }

    //提交XID事务
    @Override
    public void commit(long xid) {
        updateXID(xid, FIELD_TRAN_COMMITTED);
    }

    // 回滚XID事务
    @Override
    public void abort(long xid) {
        updateXID(xid, FIELD_TRAN_ABORTED);
    }

    // 关闭事务, 释放资源
    @Override
    public void close() {
        try {
            fileChannel.close();
            file.close();
        } catch (IOException e) {
            Panic.panic(e);
        }
    }

    @Override
    public boolean isActive(long xid) {
        if (xid == SUPER_XID) {
            return false;
        }
        return checkXID(xid, FIELD_TRAN_ACTIVE);
    }

    @Override
    public boolean isCommitted(long xid) {
        if (xid == SUPER_XID) {
            return true;
        }
        return checkXID(xid, FIELD_TRAN_COMMITTED);
    }

    @Override
    public boolean isAborted(long xid) {
        if (xid == SUPER_XID) {
            return false;
        }
        return checkXID(xid, FIELD_TRAN_ABORTED);
    }


    // 检测XID事务是否处于status状态
    private boolean checkXID(long xid, byte status) {
        long offset = getXidPosition(xid);
        ByteBuffer buffer = ByteBuffer.wrap(new byte[XID_FIELD_SIZE]);
        try {
            fileChannel.position(offset);
            fileChannel.read(buffer);
        } catch (IOException e) {
            Panic.panic(e);
        }
        return buffer.array()[0] == status;
    }

    // 检查XID文件是否合法
    private void checkXIDCounter() {
        long fileLength = 0;
        try {
            fileLength = file.length();
        } catch (IOException e) {
            throw new RuntimeException(Error.BadXIDFileException);
        }

        if(fileLength < LEN_XID_HEADER_LENGTH) {
            Panic.panic(Error.BadXIDFileException);
        }

        ByteBuffer buffer = ByteBuffer.allocate(LEN_XID_HEADER_LENGTH);
        try {
            fileChannel.position(0);
            fileChannel.read(buffer);
        } catch (IOException e) {
            Panic.panic(e);
        }
        this.xidCounter = Parser.parseLong(buffer.array());
        long end = getXidPosition(this.xidCounter + 1);
        if (end != fileLength) {
            Panic.panic(Error.BadXIDFileException);
        }
    }

    // 根据事务xid取得其在xid文件中对应的位置
    private long getXidPosition(long xid) {
        return LEN_XID_HEADER_LENGTH + (xid-1)*XID_FIELD_SIZE;
    }

    //更新事务状态为status
    private void updateXID(long xid, byte status) {
        long offset = getXidPosition(xid);
        byte[] temp = new byte[XID_FIELD_SIZE];
        temp[0] = status;
        ByteBuffer buffer = ByteBuffer.wrap(temp);

        try {
            fileChannel.position(offset);
            fileChannel.write(buffer);
        } catch (IOException e) {
            Panic.panic(e);
        }

        try {
            fileChannel.force(false);
        } catch (IOException e) {
            Panic.panic(e);
        }

    }

    //将XID+1, 并更新XID Header
    private void incrXIDCounter() {
        xidCounter ++;
        ByteBuffer buffer = ByteBuffer.wrap(Parser.long2Byte(xidCounter));

        try {
            fileChannel.position(0);
            fileChannel.write(buffer);
        } catch (IOException e) {
            Panic.panic(e);
        }

        try {
            fileChannel.force(false);
        } catch (IOException e) {
            Panic.panic(e);
        }

    }

}
