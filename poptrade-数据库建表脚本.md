# PopTrade 数据库建表脚本

## 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS poptrade
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE poptrade;
```

## 建表语句

### 1. user（用户表）

```sql
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username`    VARCHAR(50)  NOT NULL                COMMENT '用户名（登录账号）',
    `password`    VARCHAR(128) NOT NULL                COMMENT '密码（MD5加密）',
    `real_name`   VARCHAR(50)  DEFAULT NULL            COMMENT '真实姓名',
    `role`        TINYINT      NOT NULL DEFAULT 1      COMMENT '角色：0-管理员，1-普通顾客',
    `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

### 2. category（商品分类表）

```sql
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
    `id`            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `category_name` VARCHAR(50) NOT NULL                COMMENT '分类名称',
    `sort`          INT         NOT NULL DEFAULT 0      COMMENT '排序字段（升序），值越小越靠前',
    `create_time`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_category_name` (`category_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';
```

### 3. product（商品表）

```sql
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
    `id`           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `product_name` VARCHAR(100)  NOT NULL                COMMENT '商品名称',
    `price`        DECIMAL(10,2) NOT NULL                COMMENT '商品价格',
    `stock`        INT           NOT NULL DEFAULT 0      COMMENT '库存数量',
    `category_id`  BIGINT        NOT NULL                COMMENT '所属分类ID',
    `status`       TINYINT       NOT NULL DEFAULT 1      COMMENT '状态：0-下架，1-上架',
    `img`          VARCHAR(255)  DEFAULT NULL            COMMENT '商品图片URL',
    `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';
```

### 4. order（订单表）

```sql
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_no`    VARCHAR(32)   NOT NULL                COMMENT '订单编号（唯一，时间戳+随机数生成）',
    `user_id`     BIGINT        NOT NULL                COMMENT '下单用户ID',
    `total_price` DECIMAL(10,2) NOT NULL                COMMENT '订单总金额',
    `status`      TINYINT       NOT NULL DEFAULT 0      COMMENT '状态：0-待付款，1-已付款，2-已发货，3-已完成，4-已取消',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';
```

### 5. order_item（订单详情表）

```sql
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_id`      BIGINT        NOT NULL                COMMENT '所属订单ID',
    `product_id`    BIGINT        NOT NULL                COMMENT '商品ID',
    `product_num`   INT           NOT NULL                COMMENT '购买数量',
    `product_price` DECIMAL(10,2) NOT NULL                COMMENT '下单时的商品单价（价格快照）',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单详情表';
```

## 种子数据

```sql
-- 默认管理员账号：admin / admin123（BCrypt 加密）
INSERT INTO `user` (username, password, real_name, role, status) VALUES
('admin', '$2b$10$PAyxAU8hawLhbeJ61PbX/OGvi95w46vkQ.7mjMpfJumV4CwHt0gvq', '系统管理员', 0, 1);

-- 测试顾客账号：test / test123（BCrypt 加密）
INSERT INTO `user` (username, password, real_name, role, status) VALUES
('test', '$2b$10$KCNe370C4Ij6kl/blpNZ9uxRm0SFBbEXFpshH8nk0DeO0vCE5SGSO', '测试用户', 1, 1);

-- 示例分类
INSERT INTO `category` (category_name, sort) VALUES
('手机数码', 1),
('服装鞋帽', 2),
('食品饮料', 3),
('家居生活', 4);

-- 示例商品
INSERT INTO `product` (product_name, price, stock, category_id, status) VALUES
('iPhone 15', 5999.00, 100, 1, 1),
('华为Mate 60 Pro', 6999.00, 50, 1, 1),
('Nike运动鞋', 899.00, 200, 2, 1),
('纯棉T恤', 79.00, 500, 2, 1),
('可口可乐（24罐装）', 49.90, 1000, 3, 1),
('三只松鼠坚果礼盒', 128.00, 300, 3, 1);
```

## 表关系速查

```
category (1) ──── (N) product        →  category_id
user     (1) ──── (N) order          →  user_id
order    (1) ──── (N) order_item     →  order_id
product  (1) ──── (N) order_item     →  product_id
```
