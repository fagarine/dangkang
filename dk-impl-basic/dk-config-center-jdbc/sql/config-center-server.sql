CREATE TABLE `properties`
(
    `id` INT(11) NOT NULL AUTO_INCREMENT,
    `key` VARCHAR(50) NOT NULL,
    `value` VARCHAR(256) NOT NULL,
    `application` VARCHAR(64) NOT NULL DEFAULT 'application',
    `profile` VARCHAR(64) NOT NULL DEFAULT 'default',
    `label` VARCHAR(64) NOT NULL DEFAULT 'master',
    PRIMARY KEY (`id`)
) ENGINE=INNODB CHARSET=utf8 COLLATE=utf8_general_ci COMMENT='配置信息记录表';

CREATE TABLE `properties_history`
(
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `application` varchar(64) NOT NULL,
    `profile` varchar(64) NOT NULL,
    `label` varchar(64) NOT NULL,
    `version` int(10) NOT NULL,
    `content` text NOT NULL,
    `status` enum('RELEASED','ROLLED_BACK') NOT NULL,
    `operator` varchar(64) NOT NULL,
    `create_time` datetime NOT NULL,
    `update_time` datetime DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_general_ci COMMENT='配置信息修改记录';

CREATE TABLE `version_history`
(
    `name` VARCHAR(20) NOT NULL,
    `iterations` INT(10) NOT NULL COMMENT '版本总迭代次数',
    PRIMARY KEY (`name`)
) ENGINE=INNODB CHARSET=utf8 COLLATE=utf8_general_ci COMMENT='版本信息记录';