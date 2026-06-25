-- =============================================
-- 学生和教职工相关表结构
-- 创建时间: 2026-06-25
-- 说明: 用于存储钱塘区教育一体化综合服务平台的学生和教职工信息
-- =============================================

-- =============================================
-- 学生相关表
-- =============================================

-- 1. 学生完整信息表 (对应 StudentVO + PhoneVO + IdCardVO)
CREATE TABLE IF NOT EXISTS `sync_student` (
    `id` BIGINT NOT NULL COMMENT '学生基本数据子类表ID',
    `org_id` BIGINT NOT NULL COMMENT '学校组织机构ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '学生的用户ID',
    `class_org_id` BIGINT DEFAULT NULL COMMENT '班级组织机构ID',
    `xm` VARCHAR(100) DEFAULT NULL COMMENT '姓名',
    `xbm` VARCHAR(10) DEFAULT NULL COMMENT '性别码',
    `xbmc` VARCHAR(20) DEFAULT NULL COMMENT '性别名称',
    `xh` VARCHAR(50) DEFAULT NULL COMMENT '学号',
    `xjh` VARCHAR(50) DEFAULT NULL COMMENT '学籍号',
    `rxny` VARCHAR(20) DEFAULT NULL COMMENT '入学年月',
    `xsdqztm` VARCHAR(10) DEFAULT NULL COMMENT '学生当前状态码',
    `xsdqztmc` VARCHAR(50) DEFAULT NULL COMMENT '学生当前状态名称',
    `sjzk` INT DEFAULT NULL COMMENT '生籍情况',
    `sjzkmc` VARCHAR(50) DEFAULT NULL COMMENT '生籍情况名称',
    `last_sequence` BIGINT DEFAULT NULL COMMENT '数据同步序列号',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `sfzjh` VARCHAR(30) DEFAULT NULL COMMENT '身份证号',
    `sfzjlxm` VARCHAR(10) DEFAULT NULL COMMENT '身份证件类型码',
    `sfzjlxmc` VARCHAR(50) DEFAULT NULL COMMENT '身份证件类型名称',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_org_id` (`org_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_class_org_id` (`class_org_id`),
    KEY `idx_xh` (`xh`),
    KEY `idx_xjh` (`xjh`),
    KEY `idx_sfzjh` (`sfzjh`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生完整信息表';

-- =============================================
-- 教职工相关表
-- =============================================

-- 2. 教职工基本信息表 (对应 JzgVO + PhoneVO + IdCardVO)
CREATE TABLE IF NOT EXISTS `sync_teacher` (
    `id` BIGINT NOT NULL COMMENT '教职工基本数据子类表ID',
    `org_id` BIGINT NOT NULL COMMENT '学校组织机构ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
    `gh` VARCHAR(50) DEFAULT NULL COMMENT '工号',
    `xm` VARCHAR(100) DEFAULT NULL COMMENT '姓名',
    `xbm` VARCHAR(10) DEFAULT NULL COMMENT '性别码',
    `xbmc` VARCHAR(20) DEFAULT NULL COMMENT '性别名称',
    `dqztm` VARCHAR(10) DEFAULT NULL COMMENT '当前状态码',
    `dqztmc` VARCHAR(50) DEFAULT NULL COMMENT '当前状态名称',
    `jzglb` VARCHAR(20) DEFAULT NULL COMMENT '教职工类别',
    `jzglbmc` VARCHAR(50) DEFAULT NULL COMMENT '教职工类别名称',
    `last_sequence` BIGINT DEFAULT NULL COMMENT '数据同步序列号',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `sfzjh` VARCHAR(30) DEFAULT NULL COMMENT '身份证号',
    `sfzjlxm` VARCHAR(10) DEFAULT NULL COMMENT '身份证件类型码',
    `sfzjlxmc` VARCHAR(50) DEFAULT NULL COMMENT '身份证件类型名称',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_org_id` (`org_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_gh` (`gh`),
    KEY `idx_sfzjh` (`sfzjh`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教职工基本信息表';

-- 3. 教职工详细信息表 (对应 JzgInfoVO)
CREATE TABLE IF NOT EXISTS `sync_teacher_info` (
    `id` BIGINT NOT NULL COMMENT '教职工基本数据子类表ID，与sync_teacher.id关联',
    `org_id` BIGINT NOT NULL COMMENT '学校组织机构ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
    `gh` VARCHAR(50) DEFAULT NULL COMMENT '工号',
    `bzlbm` VARCHAR(20) DEFAULT NULL COMMENT '编制类别码',
    `bzlbmc` VARCHAR(50) DEFAULT NULL COMMENT '编制类别名称',
    `cjny` VARCHAR(20) DEFAULT NULL COMMENT '从教年月',
    `csdm` BIGINT DEFAULT NULL COMMENT '出生地码',
    `csrq` VARCHAR(20) DEFAULT NULL COMMENT '出生日期',
    `dabh` VARCHAR(50) DEFAULT NULL COMMENT '档案编号',
    `dqztm` VARCHAR(10) DEFAULT NULL COMMENT '当前状态码',
    `dqztmc` VARCHAR(50) DEFAULT NULL COMMENT '当前状态名称',
    `dzxx` VARCHAR(100) DEFAULT NULL COMMENT '电子信箱',
    `gatqwm` VARCHAR(10) DEFAULT NULL COMMENT '港澳台侨外码',
    `gatqwmc` VARCHAR(50) DEFAULT NULL COMMENT '港澳台侨外名称',
    `gjdqm` VARCHAR(20) DEFAULT NULL COMMENT '国家/地区码',
    `gjdqmc` VARCHAR(50) DEFAULT NULL COMMENT '国家/地区名称',
    `hdxwm` VARCHAR(20) DEFAULT NULL COMMENT '获得学位码',
    `hdxwmc` VARCHAR(50) DEFAULT NULL COMMENT '获得学位码名称',
    `hdzgxldyxhjg` VARCHAR(200) DEFAULT NULL COMMENT '获得最高学历的院校或机构',
    `hdzgxwdyxhjg` VARCHAR(200) DEFAULT NULL COMMENT '获得最高学位的院校或机构',
    `jg` VARCHAR(100) DEFAULT NULL COMMENT '籍贯',
    `jzglb` VARCHAR(20) DEFAULT NULL COMMENT '教职工类别',
    `jzglbmc` VARCHAR(50) DEFAULT NULL COMMENT '教职工类别名称',
    `lxny` VARCHAR(20) DEFAULT NULL COMMENT '来校年月',
    `mzm` VARCHAR(20) DEFAULT NULL COMMENT '民族码',
    `mzmc` VARCHAR(50) DEFAULT NULL COMMENT '民族名称',
    `rjxk` VARCHAR(50) DEFAULT NULL COMMENT '任教学科',
    `rjxkmc` VARCHAR(100) DEFAULT NULL COMMENT '任教学科名称',
    `sfwjry` VARCHAR(10) DEFAULT NULL COMMENT '是否外籍人员',
    `sfwjrymc` VARCHAR(20) DEFAULT NULL COMMENT '是否外籍人员名称',
    `sfzb` INT DEFAULT NULL COMMENT '是否在编',
    `sfzbmc` VARCHAR(20) DEFAULT NULL COMMENT '是否在编名称',
    `xbm` VARCHAR(10) DEFAULT NULL COMMENT '性别码',
    `xbmc` VARCHAR(20) DEFAULT NULL COMMENT '性别名称',
    `xm` VARCHAR(100) DEFAULT NULL COMMENT '姓名',
    `xmpy` VARCHAR(100) DEFAULT NULL COMMENT '姓名拼音',
    `ywxm` VARCHAR(100) DEFAULT NULL COMMENT '英文姓名',
    `zgxwmc` VARCHAR(50) DEFAULT NULL COMMENT '最高学位名称',
    `zp` TEXT DEFAULT NULL COMMENT '照片',
    `zyrkxd` VARCHAR(20) DEFAULT NULL COMMENT '主要任课学段',
    `zyrkxdmc` VARCHAR(50) DEFAULT NULL COMMENT '主要任课学段名称',
    `zzmmm` VARCHAR(20) DEFAULT NULL COMMENT '政治面貌码',
    `zzmmmc` VARCHAR(50) DEFAULT NULL COMMENT '政治面貌名称',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_org_id` (`org_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_gh` (`gh`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教职工详细信息表';
