package com.yhq.demotest.scheduler.entity;

import lombok.Data;

@Data
public class DBInfo {

    //数据库连接
    private String url;
    //数据库用户名
    private String username;
    //数据库密码
    private String password;
    //数据库类型(对应mysql还是sqlserver)
    private String dbtype;
    //数据库驱动
    private String driver;

}
