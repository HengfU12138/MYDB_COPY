/**
 * Copyright (C), 2023-2024, 及众科技有限公司
 * FileName: Error
 * Author:   25291
 * Date:     2024/10/2 13:57
 * Description: 错误信息
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hengfu.mydb.fc.common;

/**
 * 〈功能简述〉<br> 
 * 〈错误信息〉
 *
 * @author 25291
 * @create 2024/10/2
 * @since 1.0.0
 */
public class Error {
	public static final Exception BadXIDFileException = new RuntimeException("Invalid target XID file");
	public static final Exception FileExistsException = new RuntimeException("File already exists");
	public static final Exception FileNotExistsException = new RuntimeException("File does not exists");
	public static final Exception FileCannotRWException = new RuntimeException("File cannot read or write");
}
