# 分布式数据库

## 项目成员

- 房籽呈 [@House1993](https://github.com/House1993)
- 吴嘉晔 [@wujysh](https://github.com/wujysh)

## 项目说明

本次课程设计要求设计并实现一个有关旅游管理的分布式数据库，支持对航班、酒店、出租车等项目的添加、删除、修改、查询功能，
同时支持对客户和订单的添加、订购等功能。

数据库要求通过中心化的严格的两阶段锁机制和两阶段提交协议保证分布式事务的原子性、一致性、隔离性和持久性。

## 项目结构
- conf目录 —— 保存端口配置文件
- data目录 —— 包含运行时写入硬盘的数据
- src目录client包 —— 测试样例
- src目录entity包 —— 数据实体类
- src目录exception包 —— 异常类
- src目录lockmgr包 —— 锁管理
- src目录resource包 —— 资源管理器
- src目录transaction包 —— 事务管理器
- src目录workflow包 —— 流程控制器

## 运行流程
1. 运行事务管理器TM
2. 运行各个资源管理器RM
3. 运行流程控制器WC
4. 运行某个client