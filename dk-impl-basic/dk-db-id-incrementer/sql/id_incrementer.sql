CREATE TABLE `id_incrementer`
(
    `user_id` bigint(20) NOT NULL DEFAULT '0',
    `role_id` bigint(20) NOT NULL DEFAULT '0'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  COLLATE = utf8_general_ci COMMENT ='自增id表';
INSERT INTO id_incrementer
VALUES (0, 0);