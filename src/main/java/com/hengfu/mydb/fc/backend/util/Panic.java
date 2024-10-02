/**
 * Copyright (C), 2023-2024, 及众科技有限公司
 * FileName: Panic
 * Author:   25291
 * Date:     2024/10/2 13:49
 * Description: 处理错误、终止程序
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hengfu.mydb.fc.backend.util;

/**
 * 〈功能简述〉<br> 
 * 〈处理错误、终止程序〉
 *
 * @author 25291
 * @create 2024/10/2
 * @since 1.0.0
 */
public class Panic {
   public static void panic(Exception e){
      //记录错误信息
      System.out.println("ERROR: "+e.getMessage());
      //终止程序
      System.exit(1);
   }
}
