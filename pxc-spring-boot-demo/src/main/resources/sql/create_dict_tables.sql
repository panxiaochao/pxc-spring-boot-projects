-- =============================================
-- 数据字典相关表结构
-- 创建时间: 2026-06-25
-- 说明: 用于存储钱塘区教育一体化综合服务平台的数据字典信息
-- =============================================

-- 1. 数据字典类型表 (对应 DictTypeVO)
CREATE TABLE IF NOT EXISTS `sync_dict_type` (
    `id` BIGINT NOT NULL COMMENT '数据字典类型数据子类表ID',
    `dict_type` VARCHAR(100) NOT NULL COMMENT '数据字典类型编码',
    `dict_name` VARCHAR(200) DEFAULT NULL COMMENT '数据字典类型名称',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dict_type` (`dict_type`),
    KEY `idx_dict_name` (`dict_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据字典类型表';

-- 2. 数据字典代码表 (对应 DictCodeVO)
CREATE TABLE IF NOT EXISTS `sync_dict_code` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `dict_type_id` BIGINT NOT NULL COMMENT '数据字典类型数据子类表ID',
    `dict_type` VARCHAR(100) NOT NULL COMMENT '数据字典类型编码',
    `dm` VARCHAR(50) NOT NULL COMMENT '数据字典代码集代码',
    `mc` VARCHAR(200) DEFAULT NULL COMMENT '数据字典代码集代码名称',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_dict_type_id` (`dict_type_id`),
    KEY `idx_dict_type` (`dict_type`),
    KEY `idx_dm` (`dm`),
    UNIQUE KEY `uk_dict_type_dm` (`dict_type`, `dm`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据字典代码表';
