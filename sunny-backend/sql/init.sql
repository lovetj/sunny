-- 初始化数据库
SET NAMES utf8mb4;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS sunny DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE sunny;

-- 商品分类表
CREATE TABLE IF NOT EXISTS `category` (
                                          `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                          `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `code` VARCHAR(50) NOT NULL COMMENT '分类编码',
    `icon` VARCHAR(255) DEFAULT NULL COMMENT '分类图标',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态 0禁用 1启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 商品表
CREATE TABLE IF NOT EXISTS `product` (
                                         `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                         `category_id` BIGINT NOT NULL COMMENT '分类ID',
                                         `name` VARCHAR(100) NOT NULL COMMENT '商品名称',
    `image` VARCHAR(255) DEFAULT NULL COMMENT '商品图片',
    `images` TEXT DEFAULT NULL COMMENT '商品图片集(JSON)',
    `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '价格',
    `unit` VARCHAR(20) DEFAULT '斤' COMMENT '单位',
    `stock` INT DEFAULT 999 COMMENT '库存',
    `description` TEXT DEFAULT NULL COMMENT '商品描述',
    `origin` VARCHAR(100) DEFAULT NULL COMMENT '产地',
    `sales` INT DEFAULT 0 COMMENT '销量',
    `status` TINYINT DEFAULT 1 COMMENT '状态 0下架 1上架',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
                                      `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                      `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `status` TINYINT DEFAULT 1 COMMENT '状态 0禁用 1正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 订单表
CREATE TABLE IF NOT EXISTS `order` (
                                       `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                       `order_no` VARCHAR(50) NOT NULL COMMENT '订单号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `total_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '总金额',
    `pay_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '实付金额',
    `status` TINYINT DEFAULT 0 COMMENT '状态 0待付款 1待发货 2待收货 3已完成 4已取消',
    `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人',
    `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货电话',
    `receiver_address` VARCHAR(255) NOT NULL COMMENT '收货地址',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 订单明细表
CREATE TABLE IF NOT EXISTS `order_item` (
                                            `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                            `order_id` BIGINT NOT NULL COMMENT '订单ID',
                                            `product_id` BIGINT NOT NULL COMMENT '商品ID',
                                            `product_name` VARCHAR(100) NOT NULL COMMENT '商品名称',
    `product_image` VARCHAR(255) DEFAULT NULL COMMENT '商品图片',
    `price` DECIMAL(10,2) NOT NULL COMMENT '单价',
    `quantity` INT NOT NULL COMMENT '数量',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '小计',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- 轮播图表
CREATE TABLE IF NOT EXISTS `banner` (
                                        `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                        `title` VARCHAR(100) DEFAULT NULL COMMENT '标题',
    `image` VARCHAR(255) NOT NULL COMMENT '图片',
    `link` VARCHAR(255) DEFAULT NULL COMMENT '跳转链接',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态 0禁用 1启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轮播图表';

-- 物流信息表
CREATE TABLE IF NOT EXISTS `logistics` (
                                           `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                           `title` VARCHAR(100) NOT NULL COMMENT '标题',
    `content` TEXT DEFAULT NULL COMMENT '内容',
    `type` TINYINT DEFAULT 1 COMMENT '类型 1进城 2下乡',
    `image` VARCHAR(255) DEFAULT NULL COMMENT '图片',
    `status` TINYINT DEFAULT 1 COMMENT '状态 0禁用 1启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流信息表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS `config` (
                                        `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                        `config_key` VARCHAR(50) NOT NULL COMMENT '配置键',
    `config_value` TEXT DEFAULT NULL COMMENT '配置值',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 管理员表
CREATE TABLE IF NOT EXISTS `admin` (
                                       `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                       `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像',
    `status` TINYINT DEFAULT 1 COMMENT '状态 0禁用 1正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 初始化分类数据
INSERT INTO `category` (`name`, `code`, `icon`, `sort`, `status`) VALUES
('干货', 'ganhuo', '/static/icons/ganhuo.png', 1, 1),
('蔬菜', 'shucai', '/static/icons/shucai.png', 2, 1),
('生态油', 'shengtaiyou', '/static/icons/oil.png', 3, 1),
('酒', 'jiu', '/static/icons/jiu.png', 4, 1),
('养殖', 'yangzhi', '/static/icons/yangzhi.png', 5, 1);

-- 初始化商品数据
INSERT INTO `product` (`category_id`, `name`, `price`, `unit`, `description`, `origin`, `status`, `sort`) VALUES
(1, '白辣椒', 25.00, '斤', '农家晒制白辣椒，香辣可口，是下饭的好伴侣', '大山村', 1, 1),
(1, '豆角干', 20.00, '斤', '精选新鲜豆角晒制，保留原汁原味', '大山村', 1, 2),
(1, '茄子干', 22.00, '斤', '传统工艺晒制，软糯香甜', '大山村', 1, 3),
(1, '笋干', 35.00, '斤', '高山竹笋晒制，鲜嫩爽口', '大山村', 1, 4),
(2, '辣椒', 8.00, '斤', '新鲜采摘，辣味十足', '大山村', 1, 1),
(2, '豆角', 6.00, '斤', '农家种植，新鲜嫩绿', '大山村', 1, 2),
(2, '生姜', 10.00, '斤', '老姜味道浓郁，适合烹饪', '大山村', 1, 3),
(3, '菜籽油', 15.00, '斤', '传统压榨，纯正香浓', '大山村', 1, 1),
(3, '茶油', 60.00, '斤', '高山茶籽压榨，营养丰富', '大山村', 1, 2),
(4, '米酒', 20.00, '斤', '农家自酿米酒，醇香甘甜', '大山村', 1, 1),
(4, '红薯酒', 25.00, '斤', '传统工艺酿造，口感独特', '大山村', 1, 2),
(5, '月子鸡', 150.00, '只', '散养土鸡，滋补佳品', '大山村', 1, 1),
(5, '土鸭', 120.00, '只', '农家散养，肉质鲜美', '大山村', 1, 2),
(5, '山羊肉', 45.00, '斤', '高山放养，肉质细嫩', '大山村', 1, 3),
(5, '黄牛肉', 55.00, '斤', '农家养殖，新鲜宰杀', '大山村', 1, 4);

-- 初始化轮播图数据
INSERT INTO `banner` (`title`, `image`, `link`, `sort`, `status`) VALUES
('新鲜农产品', '/static/banners/banner1.jpg', '/pages/product/list', 1, 1),
('大山里的味道', '/static/banners/banner2.jpg', '/pages/product/list', 2, 1),
('城乡物流', '/static/banners/banner3.jpg', '/pages/logistics/list', 3, 1);

-- 初始化系统配置
INSERT INTO `config` (`config_key`, `config_value`, `description`) VALUES
('wechat', 'sunny-farm', '微信号'),
('douyin', 'sunny-farm', '抖音号'),
('phone', '13800138000', '联系电话'),
('address', '某省某市某县大山村', '地址');

-- 初始化管理员账号 (密码: admin123)
INSERT INTO `admin` (`username`, `password`, `real_name`, `status`) VALUES
('admin', '$2a$10$EqKcp1WFKVQISheBxmXJGePJwJbvHfEFvEqJjGWQv2Mb6AqPQvWIi', '管理员', 1);