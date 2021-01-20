package com.yhq.demotest.scheduler.service.impl;

import com.yhq.demotest.scheduler.entity.DBInfo;
import com.yhq.demotest.scheduler.entity.JobInfo;
import com.yhq.demotest.scheduler.service.synchronousService;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Service
@Slf4j
public class synchronousServiceImpl implements synchronousService {

    @Override
    public Map<String, Object> initSyncXml() {

        SAXReader reader = new SAXReader();
        DBInfo srcDB = new DBInfo();
        DBInfo destDB = new DBInfo();
        ArrayList<JobInfo> jobList = new ArrayList<>();

        try {
            Element root = reader.read(synchronousServiceImpl.class.getResourceAsStream("/synchronous.xml")).getRootElement();
            Element src = root.element("source");
            Element dest = root.element("dest");
            Element jobs = root.element("jobs");

            elementInObject(src,srcDB);
            elementInObject(dest,destDB);

            // 遍历job即同步的表
            for (@SuppressWarnings("rawtypes")
                 Iterator it = jobs.elementIterator("job"); it.hasNext();) {
                jobList.add((JobInfo) elementInObject((Element) it.next(), new JobInfo()));
            }

        }catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage());
        }

        Map<String,Object> map = new HashMap<>();
        map.put("src",srcDB);
        map.put("dest",destDB);
        map.put("jobList",jobList);

        return map;
    }

    @Override
    public Connection createConnection(DBInfo db) {
        try {
            Class.forName(db.getDriver());
            Connection conn = DriverManager.getConnection(db.getUrl(), db.getUsername(), db.getPassword());
            conn.setAutoCommit(false);
            return conn;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    @Override
    public void destoryConnection(Connection conn) {
        try {
            if (conn != null) {
                conn.close();
                conn = null;
                log.info("数据库连接关闭");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String assembleSQL(String srcSql, Connection conn, JobInfo jobInfo) throws SQLException {

        String destTableKey = jobInfo.getDestTableKey();
        String destTable = jobInfo.getTableName();
        String destTableFields = jobInfo.getDestTableFields();
        String destTableUpdata = jobInfo.getDestTableUpdata();

        String[] field = destTableFields.split(",");
        String[] updata = destTableUpdata.split(",");

        Statement stat = conn.createStatement();
        ResultSet rs = stat.executeQuery(srcSql);
        StringBuffer sql = new StringBuffer();
        long count = 0;
        sql.append("insert into ").append(destTable).append("(").append(destTableFields).append(") values ");
        while (rs.next()) {
            sql.append("(");
            for (int i = 0; i < field.length; i++) {
                //数据为null的，转存为null，不能加''引号，除varchar以外会报错
                if(rs.getString(field[i])==null){
                    sql.append(rs.getString(field[i])).append(i == (field.length - 1) ? "" : ",");
                }else {
                    sql.append("'").append(rs.getString(field[i])).append(i == (field.length - 1) ? "'" : "',");
                }
            }
            sql.append("),");
            count++;
        }
        log.info("总共查询到 " + count + " 条记录");
        if (rs != null) {
            rs.close();
        }
        if (stat != null) {
            stat.close();
        }
        if (count > 0) {
            //删除最后一个逗号
            sql = sql.deleteCharAt(sql.length() - 1);
            if ((!destTableUpdata.equals("")) && (!destTableKey.equals(""))) {
                /*sql.append(" on duplicate key update ");
                for (int index = 0; index < updata.length; index++) {
                    sql.append(updata[index]).append("= values(").append(updata[index]).append(index == (updata.length - 1) ? ")" : "),");
                }*/
                //先删除表中的数据 全覆盖同步
                return new StringBuffer("truncate ").append(destTable).append("!!@@")
                        .append(sql.toString()).toString();
            }
            return sql.toString();
        }
        return null;
    }

    @Override
    public void executeSQL(String sql, Connection conn) throws SQLException {
        PreparedStatement pst = conn.prepareStatement("");
        String[] sqlList = sql.split("!!@@");
        for (int index = 0; index < sqlList.length; index++) {
            pst.addBatch(sqlList[index]);
        }
        pst.executeBatch();
        conn.commit();
        pst.close();
    }


    public Object elementInObject(Element e, Object o) throws IllegalArgumentException, IllegalAccessException {
        Field[] fields = o.getClass().getDeclaredFields();
        for (int index = 0; index < fields.length; index++) {
            fields[index].setAccessible(true);
            fields[index].set(o, e.element(fields[index].getName()).getTextTrim());
        }
        return o;
    }
}
