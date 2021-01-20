# MySQL数据同步
>> 主要通过定时器来同步一些基础信息表，不建议同步大量数据


resources里面的xml可以配置源数据库和目标数据库，jobs里面配置同步表的信息

1. 表名tablename
2. 查询的数据sql
3. 目标库 主键destTableKey
4. 目标库 字段destTableFields
5. 目标库 更新字段destTableUpdata