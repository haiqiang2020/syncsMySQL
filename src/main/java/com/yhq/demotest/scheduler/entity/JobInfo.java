package com.yhq.demotest.scheduler.entity;

import lombok.Data;

@Data
public class JobInfo {

    private String tableName;

    private String sql;

    private String destTableKey;

    private String destTableFields;

    private String destTableUpdata;
}
