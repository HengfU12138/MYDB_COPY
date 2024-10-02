/**
 * Copyright (C), 2023-2024, 及众科技有限公司
 * FileName: Parser
 * Author:   25291
 * Date:     2024/10/2 15:46
 * Description: io类型转换工具类
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hengfu.mydb.fc.backend.util;

import com.google.common.primitives.Bytes;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * 〈功能简述〉<br> 
 * 〈io类型转换工具类〉
 *
 * @author 25291
 * @create 2024/10/2
 * @since 1.0.0
 */
public class Parser {
   public static Long parseLong(byte[] buf) {
    ByteBuffer buffer = ByteBuffer.wrap(buf, 0, 8);
    return buffer.getLong();
   }
   public static byte[] long2Byte(long value){
       return ByteBuffer.allocate(Long.SIZE/Byte.SIZE).putLong(value).array();
   }
}
