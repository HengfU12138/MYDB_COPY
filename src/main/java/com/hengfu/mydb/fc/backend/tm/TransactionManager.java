/**
 * Copyright (C), 2023-2024, 及众科技有限公司
 * FileName: TransactionManager
 * Author:   25291
 * Date:     2024/10/2 13:33
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hengfu.mydb.fc.backend.tm;

import com.hengfu.mydb.fc.backend.util.Panic;
import com.hengfu.mydb.fc.common.Error;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 〈功能简述〉<br> 
 * 〈事务管理器接口〉
 *
 * @author 25291
 * @create 2024/10/2
 * @since 1.0.0
 */
public interface TransactionManager {
    long begin();
    void commit(long xid);
    void abort(long xid);
    boolean isActive(long xid);
    boolean isCommitted(long xid);
    boolean isAborted(long xid);
    void close();

    public static TransactionManagerImpl create(String path) {

        File file = new File(path + TransactionManagerImpl.XID_SUFFIX);
		try {
			if (!file.createNewFile()) {
				Panic.panic(Error.FileExistsException);
			}
		} catch (IOException e) {
			Panic.panic(e);
		}

        if (!file.canRead()||!file.canWrite()) {
            Panic.panic(Error.FileCannotRWException);
        }

        FileChannel fileChannel = null;
        RandomAccessFile randomAccessFile = null;

		try {
			randomAccessFile = new RandomAccessFile(file,"rw");
            fileChannel = randomAccessFile.getChannel();
        } catch (FileNotFoundException e) {
            Panic.panic(e);
        }

        //初始化文件头
        ByteBuffer buf = ByteBuffer.wrap(new byte[TransactionManagerImpl.LEN_XID_HEADER_LENGTH]);

		try {
			fileChannel.position(0);
            fileChannel.write(buf);
        } catch (IOException e) {
            Panic.panic(e);
        }

        return new TransactionManagerImpl(randomAccessFile,fileChannel);
    }


    public static TransactionManagerImpl open(String path){
        File file = new File(path + TransactionManagerImpl.XID_SUFFIX);
        if (!file.exists()){
            Panic.panic(Error.FileNotExistsException);
        }

        if(!file.canRead() || !file.canWrite()){
            Panic.panic(Error.FileCannotRWException);
        }

        FileChannel fileChannel = null;
        RandomAccessFile randomAccessFile = null;

		try {
			randomAccessFile = new RandomAccessFile(file,"rw");
            fileChannel = randomAccessFile.getChannel();
        } catch (FileNotFoundException e) {
            Panic.panic(e);
        }

        return new TransactionManagerImpl(randomAccessFile,fileChannel);

    }

}
