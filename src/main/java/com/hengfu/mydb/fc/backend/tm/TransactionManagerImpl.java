/**
 * Copyright (C), 2023-2024, 及众科技有限公司
 * FileName: TransactionManagerImpl
 * Author:   25291
 * Date:     2024/10/2 13:35
 * Description: 事务管理器实现类
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hengfu.mydb.fc.backend.tm;

import com.hengfu.mydb.fc.backend.util.Panic;
import com.hengfu.mydb.fc.backend.util.Parser;
import com.hengfu.mydb.fc.common.Error;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 〈功能简述〉<br> 
 * 〈事务管理器实现类〉
 *
 * @author 25291
 * @create 2024/10/2
 * @since 1.0.0
 */
public class TransactionManagerImpl implements TransactionManager {

    // XID文件头长度 （文件头内容存放基本文件信息和用于对文件进行简单校验）
    static final int LEN_XID_HEADER_LENGTH = 8;
    // 每个事务的占用长度
    private static final int XID_FIELD_SIZE = 1;

    // 事务的三种状态
    private static final byte FIELD_TRAN_ACTIVE   = 0;
    private static final byte FIELD_TRAN_COMMITTED = 1;
    private static final byte FIELD_TRAN_ABORTED  = 2;

    // 超级事务，永远为commited状态
    public static final long SUPER_XID = 0;

    // XID文件后缀
    static final String XID_SUFFIX = ".xid";

    private FileChannel fileChannel;
    private long xidCounter;
    private RandomAccessFile file;
    private Lock counterLock;


    public TransactionManagerImpl( RandomAccessFile file,FileChannel fileChannel) {
        checkXIDCounter(file);
        this.fileChannel = fileChannel;
        this.file = file;
        counterLock = new ReentrantLock();

    }

    /**
     * 文件校验合法性
     * 简单说就是通过头文件中存放的事务数量反推文件长度并与实际文件长度进行比较
     */
    private void checkXIDCounter(RandomAccessFile file){
        long fileLen = 0;
		try {
			fileLen = file.length();
		} catch (IOException e) {
            Panic.panic(Error.BadXIDFileException);
		}

		if (fileLen<LEN_XID_HEADER_LENGTH){
            Panic.panic(Error.BadXIDFileException);
        }
        ByteBuffer buf = ByteBuffer.allocate(LEN_XID_HEADER_LENGTH);
		try {
			fileChannel.position(0);
            fileChannel.read(buf);
		} catch (IOException e) {
			Panic.panic(e);
		}
        this.xidCounter = Parser.parseLong(buf.array());
        long end = getXidPosition(this.xidCounter+1);
        if (end != fileLen){
            Panic.panic(Error.BadXIDFileException);
        }

    }

    /**
     * 根据xid得到对应存放的位置
     * 除去文件头（8字节）每条数据占1字节依次存放
     * @param xid
     * @return
     */
    private long getXidPosition(long xid){
        return LEN_XID_HEADER_LENGTH + (xid-1) * XID_FIELD_SIZE;
    }

    private void updateXID(long xid,byte status){
        long offset = getXidPosition(xid);
        byte[] tmp = new byte[XID_FIELD_SIZE];
        tmp[0] = status;
        ByteBuffer buf = ByteBuffer.wrap(tmp);
		try {
			fileChannel.position(offset);
            fileChannel.write(buf);
		} catch (IOException e) {
			Panic.panic(e);
		}

		try {
            //没有新增或删除数据无需刷新源数据
			fileChannel.force(false);
		} catch (IOException e) {
			Panic.panic(e);
		}

	}

    //xid自增，并更新XID Header
    private void incrXIDCounter(){
        this.xidCounter ++;
        ByteBuffer buf = ByteBuffer.wrap(Parser.long2Byte(this.xidCounter));

		try {
            fileChannel.position(0);
            fileChannel.write(buf);
        } catch (IOException e) {
            Panic.panic(e);
		}
		try {
			fileChannel.force(false);
		} catch (IOException e) {
			Panic.panic(e);
		}
	}

    @Override
    public long begin() {
        counterLock.lock();
        try {
            long xid = xidCounter +1;
            updateXID(xid,FIELD_TRAN_ACTIVE);
            incrXIDCounter();
            return  xid;
        }finally {
            counterLock.unlock();
        }
    }

    @Override
    public void commit(long xid) {
        updateXID(xid,FIELD_TRAN_COMMITTED);
    }

    @Override
    public void abort(long xid) {
        updateXID(xid,FIELD_TRAN_ABORTED);
    }

    private boolean checkXID(long xid,byte status){
        long offset = getXidPosition(xid);

        ByteBuffer buf = ByteBuffer.wrap(new byte[XID_FIELD_SIZE]);

		try {
            fileChannel.position(offset);
            fileChannel.read(buf);
        } catch (IOException e) {
			Panic.panic(e);
		}

        return buf.array()[0] == status;
	}

    @Override
    public boolean isActive(long xid) {
        if (xid == SUPER_XID){
            return false;
        }
        return checkXID(xid,FIELD_TRAN_ACTIVE);

    }

    @Override
    public boolean isCommitted(long xid) {
        if (xid == SUPER_XID){
            return false;
        }
        return checkXID(xid,FIELD_TRAN_COMMITTED);
    }

    @Override
    public boolean isAborted(long xid) {
        if (xid == SUPER_XID){
            return false;
        }
        return checkXID(xid,FIELD_TRAN_ABORTED);
    }


    @Override
    public void close() {
		try {
			fileChannel.close();
            file.close();
        } catch (IOException e) {
            Panic.panic(e);
        }
    }
}
