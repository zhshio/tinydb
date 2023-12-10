# Shio-TinyDB
TinyDB 是一个 Java 实现的简单的关系型数据库，部分原理参照自 MySQL、PostgreSQL 和 SQLite。实现了以下功能：

  数据的可靠性和数据恢复
  两段锁协议（2PL）实现可串行化调度
  MVCC
  两种事务隔离级别（读提交和可重复读）
  死锁处理
  简单的表和字段管理
  简陋的 SQL 解析（因为懒得写词法分析和自动机，就弄得比较简陋）
  基于 socket 的 server 和 client
