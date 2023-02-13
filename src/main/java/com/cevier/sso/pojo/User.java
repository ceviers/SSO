package com.cevier.sso.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author: cevier.wei
 * @date: 2023/2/13 15:17
 */
@Data
@TableName("user")
public class User {
    private Long id;
    private String name;
}
