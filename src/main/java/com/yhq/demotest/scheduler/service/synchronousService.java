package com.yhq.demotest.scheduler.service;

import com.yhq.demotest.scheduler.entity.DBInfo;
import com.yhq.demotest.scheduler.entity.JobInfo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public interface synchronousService {

    /**
    * @Description:  初始化job.xml文件
    * @Param: []
    * @return: java.util.Map<java.lang.String,java.lang.Object>
    */
    public Map<String,Object> initSyncXml();

    /**
    * @Description:  数据库连接
    * @Param: [db]
    * @return: java.sql.Connection
    */
    public Connection createConnection(DBInfo db);

    /**
    * @Description:  关闭并销毁数据库连接
    * @Param: [conn]
    * @return: void
    */
    public void destoryConnection(Connection conn);

    /**
    * @Description:  组装sql
    * @Param:
    * @return:
    */
    public String assembleSQL(String srcSql, Connection conn, JobInfo jobInfo) throws SQLException;

    /**
    * @Description:  执行sql
    * @Param:
    * @return:
    */
    public void executeSQL(String sql, Connection conn) throws SQLException;
}
