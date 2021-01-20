package com.yhq.demotest.scheduler.schedul;


import com.yhq.demotest.scheduler.entity.DBInfo;
import com.yhq.demotest.scheduler.entity.JobInfo;
import com.yhq.demotest.scheduler.service.synchronousService;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

@Component
@Slf4j
public class synchronousData {

    /*private Map<String,Object> initInfo;

    public void getInitInfo(){

    }*/

    @Autowired
    private synchronousService synchronousService;

    @Async
    @Scheduled(cron = "0 30/59 * * * ?")   // 30min/1次
    public void synchronousDepart(){
        Map<String,Object> initMap = synchronousService.initSyncXml();
        //源连接、目标连接、初始化
        Connection inConn = null;
        Connection outConn = null;
        DBInfo srcDb = new DBInfo();
        DBInfo destDb = new DBInfo();
        JSONArray jobList = new JSONArray();
        //得到数据
        srcDb = (DBInfo) initMap.get("src");
        destDb = (DBInfo) initMap.get("dest");
        jobList = JSONArray.fromObject(initMap.get("jobList"));
        try{

            inConn = synchronousService.createConnection(srcDb);
            outConn = synchronousService.createConnection(destDb);
            if (inConn == null) {
                log.info("请检查源数据连接!");
                return;
            } else if (outConn == null) {
                log.info("请检查目标数据连接!");
                return;
            }

            for(int i=0;i<jobList.size();i++){
                JSONObject jsonObject = (JSONObject)jobList.get(i);
                JobInfo jobInfo = (JobInfo) JSONObject.toBean(jsonObject,JobInfo.class);
                long start = System.currentTimeMillis();
                String sql = synchronousService.assembleSQL(jobInfo.getSql(),inConn,jobInfo);
                log.info("--开始同步"+jobInfo.getTableName()+"---组装耗时------"+ (System.currentTimeMillis()-start)+"ms");
//                log.info("--SQL语句--"+sql);
                if (sql != null) {
                    long eStart = System.currentTimeMillis();
                    synchronousService.executeSQL(sql, outConn);
                    log.info("执行SQL语句耗时: " + (System.currentTimeMillis() - eStart) + "ms");
                }

            }

        } catch (SQLException e){
            log.error(e.getMessage());
        } finally {
            log.info("关闭源数据库连接");
            synchronousService.destoryConnection(inConn);
            log.info("关闭目标数据库连接");
            synchronousService.destoryConnection(outConn);
        }
    }

}
