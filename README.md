# MySQL数据同步
> 在分库分表和多数据源的情况下，存在跨库操作，为避免跨库的一种解决思路,同步一些基础数据，数据字段，人员信息等表...
>> 主要通过定时器来同步一些基础信息表，不建议同步大量数据


resources里面的xml可以配置源数据库和目标数据库，jobs里面配置同步表的信息

1. 表名tablename
2. 查询的数据sql
3. 目标库 主键destTableKey
4. 目标库 字段destTableFields
5. 目标库 更新字段destTableUpdata
