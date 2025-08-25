-- 创建数据库
create database if not exists yuchuang;

-- 使用数据库
use yuchuang;

-- 创建用户表
create table if not exists user
(
    id            bigint primary key auto_increment comment '主键',
    user_name     varchar(20)   not null comment '用户名',
    user_account  varchar(20)   not null comment '账号',
    user_password varchar(128)  not null comment '密码',
    user_role     varchar(20)   not null comment '用户角色：user/admin',
    user_avatar   varchar(1024) null comment '用户头像',
    user_profile  varchar(512)  null comment '用户简介',
    create_time   datetime default CURRENT_TIMESTAMP comment '创建时间',
    edit_time     datetime default CURRENT_TIMESTAMP comment '编辑时间',
    update_time   datetime default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete     int comment '逻辑删除',
    unique key uk_userAccount (user_account),
    index idx_name (user_name)
) comment '用户表';

-- 应用表
create table app
(
    id            bigint auto_increment comment 'id' primary key,
    app_name      varchar(256)                       null comment '应用名称',
    cover         varchar(512)                       null comment '应用封面',
    init_prompt   text                               null comment '应用初始化的 prompt',
    code_gen_type varchar(64)                        null comment '代码生成类型（枚举）',
    deploy_key    varchar(64)                        null comment '部署标识',
    deployed_time datetime                           null comment '部署时间',
    priority      int      default 0                 not null comment '优先级',
    user_id       bigint                             not null comment '创建用户id',
    edit_time     datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    create_time   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete     tinyint  default 0                 not null comment '是否删除',
    UNIQUE KEY uk_deployKey (deploy_key), -- 确保部署标识唯一
    INDEX idx_appName (app_name),         -- 提升基于应用名称的查询性能
    INDEX idx_userId (user_id)            -- 提升基于用户 ID 的查询性能
) comment '应用' collate = utf8mb4_unicode_ci; -- 设置编码为 utf8mb4_unicode_ci, 兼容中文

-- 对话历史表
create table if not exists chat_history
(
    id          bigint primary key auto_increment comment 'id',
    message     longtext               not null comment '消息',
    message_type varchar(32)        not null comment 'user/ai',
    app_id      bigint             not null comment '应用id',
    user_id     bigint             not null comment '创建用户id',
    create_time datetime default CURRENT_TIMESTAMP comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete   tinyint  default 0 not null comment '是否删除',
    INDEX idx_appId (app_id),
    INDEX idx_createTime (create_time),
    INDEX idx_appId_createTime (app_id, create_time)
) comment '对话历史表' collate = utf8mb4_unicode_ci;

