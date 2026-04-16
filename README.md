## 20260416-微服务DEMO编写

### 涉及项目

| 项目                 | 分支                   | 变更          | 负责人 | 备注           |
| -------------------- | ---------------------- | ------------- | ------ | -------------- |
| springcloud-demo-kai | dev/20260416-demo1-tzk | 数据库脚本DDL | TZK    | 微服务DEMO编写 |

确认列表

1. readme中包含项目要求的JDK版本说明。

   √，在系统架构中说明

2. 是否能build成功相关文件。

   √，可以，依次启动 DiscoveryApplication、UaaApplication、ProductApplication、GatewayApplication 服务。

3. 是否能自动启动数据库并创建相关数据库。

   目前需要手动创建数据库，如果使用docker进行部署，可以做到部署就自动创建

4. 相关服务是否正确启动。

   √

5. 通过readme.md中提供的相关CURL命令，是否能得到预期的输出。

   √，请看测试命令

## 1.系统架构

项目包含以下服务：

- **Gateway (网关)**: 对外统一入口，运行在 `http://localhost:7573`
- **UAA (用户认证服务)**: 处理登录、Token签发及OAuth2，内部端口 `8082`，通过网关 `/uaa/**` 访问
- **Product (产品服务)**: 提供产品的增删改查 API，内部端口 `8081`，通过网关 `/product/**` 访问
- **Discovery (注册中心)**: Eureka 服务注册与发现，运行在 `http://localhost:8761`

技术栈版本：

JDK 17

SpringBoot 3.2.4

SpringCloud 2023.0.1

MySQL 8

## 2.角色与权限

内置三种角色：

- `USER`: 只能查看产品列表。
- `EDITOR`: 可以查看、添加、修改、删除产品（拥有USER权限）。
- `PRODUCT_ADMIN`: 可以查看、添加、修改、删除产品（拥有EDITOR及USER权限）。

## 3.测试账号

账号/密码

**普通用户组：**

- `user_1` / `user_1` (USER)
- `editor_1` / `editor_1` (EDITOR)
- `adm_1` / `adm_1` (PRODUCT_ADMIN)

**LDAP 模拟组：**

- `ldap_user_1` / `ldap_user_1` (USER)
- `ldap_editor_1` / `ldap_editor_1` (EDITOR)
- `ldap_adm_1` / `ldap_adm_1` (PRODUCT_ADMIN)

提供两种登陆方式，LDAP登录和普通用户登录。(docker部署了LDAP，目前可以测试)

## 4.CURL 测试命令

使用postman进行测试：

获取 Token 并设置环境变量:

```bash
export TOKEN=$(curl -s --location 'http://localhost:7573/uaa/token' \
--header 'Content-Type: application/json' \
--data '{
  "grantType": "password",
  "username": "editor_1",
  "password": "editor_1"
}' | grep -o '"access_token":"[^"]*' | grep -o '[^"]*$')
echo "Token is: $TOKEN"
```

### 登录/获取 Token

```bash
curl --location 'http://localhost:7573/uaa/token' \
--header 'Content-Type: application/json' \
--data '{
  "grantType": "password",
  "username": "editor_1",
  "password": "editor_1"
}'
```

测试结果

```bash
{
    "access_token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlZGl0b3JfMSIsInJvbGVzIjoiRURJVE9SIiwiaWF0IjoxNzc2MzM4MTA1LCJleHAiOjE3NzY0MjQ1MDV9.vEJFQqdoZDZClFylBSZcyAhFfdM6MBqw7uJs7COFM8o",
    "token_type": "bearer",
    "expires_in": 86400
}
```



### 添加产品:

```bash
curl --location 'http://localhost:7573/product' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlZGl0b3JfMSIsInJvbGVzIjoiRURJVE9SIiwiaWF0IjoxNzc2MzM4MTA1LCJleHAiOjE3NzY0MjQ1MDV9.vEJFQqdoZDZClFylBSZcyAhFfdM6MBqw7uJs7COFM8o' \
--header 'Content-Type: application/json' \
--data '{"name": "CURL Product"}'
```

测试结果

```bash
{
    "id": 2,
    "name": "CURL Product"
}
```



### 修改id为2的产品:

```bash
curl --location --request PUT 'http://localhost:7573/product/2' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlZGl0b3JfMSIsInJvbGVzIjoiRURJVE9SIiwiaWF0IjoxNzc2MzM4MTA1LCJleHAiOjE3NzY0MjQ1MDV9.vEJFQqdoZDZClFylBSZcyAhFfdM6MBqw7uJs7COFM8o' \
--header 'Content-Type: application/json' \
--data '{"name": "CURL Updated Product"}'
```

测试结果

```bash
{
    "id": 2,
    "name": "CURL Updated Product"
}
```

### 删除id为2的产品:

```bash
curl --location --request DELETE 'http://localhost:7573/product/2' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlZGl0b3JfMSIsInJvbGVzIjoiRURJVE9SIiwiaWF0IjoxNzc2MzM4MTA1LCJleHAiOjE3NzY0MjQ1MDV9.vEJFQqdoZDZClFylBSZcyAhFfdM6MBqw7uJs7COFM8o'
```



## 附件

数据库脚本 docs/init.sql

```sql
-- 微服务 DEMO 数据库初始化脚本
-- 初始数据将由 UaaApplication 启动时的 InitConfig 自动插入，此处无需手动 INSERT
-- 1. 认证中心数据库 (uaa_db)
CREATE DATABASE IF NOT EXISTS `uaa_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `uaa_db`;
-- 创建用户表
CREATE TABLE IF NOT EXISTS `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `username` varchar(255) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '加密密码',
  `email` varchar(255) DEFAULT NULL COMMENT '邮箱',
  `roles` varchar(255) DEFAULT NULL COMMENT '角色列表(多个角色用逗号分隔)',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户权限表';

-- 2. 产品中心数据库 (product_db)
CREATE DATABASE IF NOT EXISTS `product_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `product_db`;
-- 创建产品表
CREATE TABLE IF NOT EXISTS `product` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `name` varchar(255) DEFAULT NULL COMMENT '产品名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产品信息表';

```

LDAP测试数据 init.Idif

```bash
# 定义基础的组织单元 (Organizational Unit)
dn: ou=users,dc=example,dc=com
objectClass: organizationalUnit
ou: users

# 添加 普通用户: ldap_user_1
dn: uid=ldap_user_1,ou=users,dc=example,dc=com
objectClass: inetOrgPerson
objectClass: top
uid: ldap_user_1
cn: ldap_user_1
sn: user_1
userPassword: ldap_user_1

# 添加 EDITOR 用户: ldap_editor_1
dn: uid=ldap_editor_1,ou=users,dc=example,dc=com
objectClass: inetOrgPerson
objectClass: top
uid: ldap_editor_1
cn: ldap_editor_1
sn: editor_1
userPassword: ldap_editor_1

# 添加 PRODUCT_ADMIN 用户: ldap_adm_1
dn: uid=ldap_adm_1,ou=users,dc=example,dc=com
objectClass: inetOrgPerson
objectClass: top
uid: ldap_adm_1
cn: ldap_adm_1
sn: adm_1
userPassword: ldap_adm_1

```



### 注意

1.依次启动 DiscoveryApplication、UaaApplication、ProductApplication、GatewayApplication 服务。

2.我使用了本地的MySQL环境，需要初始化表结构