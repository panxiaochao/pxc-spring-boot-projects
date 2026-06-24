-- 钱塘区教育一体化综合服务平台 - 数据库表结构
-- 创建时间: 2026-06-24

-- 创建单位表
CREATE TABLE `unit` (
  `id` BIGINT NOT NULL COMMENT '单位ID',
  `org_id` BIGINT NOT NULL COMMENT '组织机构ID',
  `dwdm` VARCHAR(50) DEFAULT NULL COMMENT '单位代码',
  `dwmc` VARCHAR(200) NOT NULL COMMENT '单位名称',
  `dwywmc` VARCHAR(200) DEFAULT NULL COMMENT '单位英文名称',
  `dwdz` VARCHAR(500) DEFAULT NULL COMMENT '单位地址',
  `dwyzbm` VARCHAR(10) DEFAULT NULL COMMENT '单位邮政编码',
  `dwzgbmm` VARCHAR(50) DEFAULT NULL COMMENT '单位主管部门码',
  `dwjj` TEXT DEFAULT NULL COMMENT '单位简介',
  `tyshxydm` VARCHAR(50) DEFAULT NULL COMMENT '统一社会信用代码',
  `xzqhm` VARCHAR(50) DEFAULT NULL COMMENT '行政区划码',
  `last_sequence` BIGINT DEFAULT 0 COMMENT '数据同步序列号',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_org_id` (`org_id`),
  KEY `idx_dwdm` (`dwdm`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='单位信息表';

-- 创建学校表
CREATE TABLE `school` (
  `id` BIGINT NOT NULL COMMENT '学校ID',
  `org_id` BIGINT NOT NULL COMMENT '组织机构ID',
  `xxbsm` VARCHAR(50) DEFAULT NULL COMMENT '学校标识码',
  `xxbxlxm` VARCHAR(50) DEFAULT NULL COMMENT '办学类型码',
  `xxdm` VARCHAR(50) NOT NULL COMMENT '学校代码',
  `xxmc` VARCHAR(200) NOT NULL COMMENT '学校名称',
  `xxdz` VARCHAR(500) DEFAULT NULL COMMENT '学校地址',
  `xxjc` VARCHAR(100) DEFAULT NULL COMMENT '学校简称',
  `xxjj` TEXT DEFAULT NULL COMMENT '学校简介',
  `xxxzm` VARCHAR(50) DEFAULT NULL COMMENT '学校性质',
  `xxzt` VARCHAR(10) DEFAULT NULL COMMENT '学校状态',
  `jxny` VARCHAR(20) DEFAULT NULL COMMENT '建校年月',
  `lxdh` VARCHAR(50) DEFAULT NULL COMMENT '联系电话',
  `dzxx` VARCHAR(100) DEFAULT NULL COMMENT '电子信箱',
  `tyshxydm` VARCHAR(50) DEFAULT NULL COMMENT '统一社会信用代码',
  `xxyzbm` VARCHAR(10) DEFAULT NULL COMMENT '学校邮政编码',
  `xxzgbmm` VARCHAR(50) DEFAULT NULL COMMENT '学校主管部门码',
  `xzqhm` VARCHAR(50) DEFAULT NULL COMMENT '行政区划码',
  `zydz` VARCHAR(500) DEFAULT NULL COMMENT '主页地址',
  `last_sequence` BIGINT DEFAULT 0 COMMENT '数据同步序列号',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_org_id` (`org_id`),
  UNIQUE KEY `uk_xxdm` (`xxdm`),
  KEY `idx_xxbsm` (`xxbsm`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学校信息表';

-- 创建校区表
CREATE TABLE `campus` (
  `id` BIGINT NOT NULL COMMENT '校区ID',
  `org_id` BIGINT NOT NULL COMMENT '组织机构ID',
  `xqdm` VARCHAR(50) NOT NULL COMMENT '校区代码',
  `xqmc` VARCHAR(200) NOT NULL COMMENT '校区名称',
  `xqm` VARCHAR(50) DEFAULT NULL COMMENT '校区码',
  `xqzt` VARCHAR(10) DEFAULT NULL COMMENT '校区状态',
  `sfxnxq` TINYINT DEFAULT 0 COMMENT '是否虚拟校区(0:否,1:是)',
  `sfzxqm` TINYINT DEFAULT 0 COMMENT '是否主校区(0:主校,1:分校)',
  `xxdm` VARCHAR(50) NOT NULL COMMENT '学校代码',
  `xxmc` VARCHAR(200) DEFAULT NULL COMMENT '学校名称',
  `szzqzxs` VARCHAR(50) DEFAULT NULL COMMENT '省(自治区、直辖市)',
  `dsz` VARCHAR(50) DEFAULT NULL COMMENT '地(市、州)',
  `qqq` VARCHAR(50) DEFAULT NULL COMMENT '县(区、旗)',
  `xz` VARCHAR(50) DEFAULT NULL COMMENT '乡(镇)',
  `sq` VARCHAR(100) DEFAULT NULL COMMENT '社区',
  `x` VARCHAR(50) DEFAULT NULL COMMENT '经度',
  `y` VARCHAR(50) DEFAULT NULL COMMENT '纬度',
  `xqdz` VARCHAR(500) DEFAULT NULL COMMENT '校区地址',
  `jxny` VARCHAR(20) DEFAULT NULL COMMENT '建校年月',
  `ghgm_bj` INT DEFAULT NULL COMMENT '规划规模(班级数量)',
  `xzqhm` VARCHAR(100) DEFAULT NULL COMMENT '行政区划码集合',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_org_id` (`org_id`),
  UNIQUE KEY `uk_xqdm` (`xqdm`),
  KEY `idx_xxdm` (`xxdm`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='校区信息表';

-- 创建年级表
CREATE TABLE `grade` (
  `id` BIGINT NOT NULL COMMENT '年级ID',
  `org_id` BIGINT NOT NULL COMMENT '组织机构ID',
  `njdm` VARCHAR(50) NOT NULL COMMENT '年级代码',
  `njmc` VARCHAR(100) NOT NULL COMMENT '年级名称',
  `xqdm` VARCHAR(50) NOT NULL COMMENT '校区代码',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_org_id` (`org_id`),
  UNIQUE KEY `uk_njdm` (`njdm`),
  KEY `idx_xqdm` (`xqdm`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='年级信息表';

-- 创建班级表
CREATE TABLE `class` (
  `id` BIGINT NOT NULL COMMENT '班级ID',
  `org_id` BIGINT NOT NULL COMMENT '组织机构ID',
  `bjdm` VARCHAR(50) NOT NULL COMMENT '班级代码',
  `bj` VARCHAR(50) DEFAULT NULL COMMENT '班号',
  `bjmc` VARCHAR(100) NOT NULL COMMENT '班级名称',
  `njdm` VARCHAR(50) NOT NULL COMMENT '年级代码',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_org_id` (`org_id`),
  UNIQUE KEY `uk_bjdm` (`bjdm`),
  KEY `idx_njdm` (`njdm`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级信息表';
